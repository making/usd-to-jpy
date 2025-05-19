package am.ik.usd2jpy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@StepScope
public class UsdToJpyTasklet implements Tasklet {

	private final Path baseDir;

	private final LocalDate startDate;

	private final LocalDate endDate;

	private final RestClient restClient;

	private final Logger log = LoggerFactory.getLogger(UsdToJpyTasklet.class);

	private final DateTimeFormatter yymmddFormatter = DateTimeFormatter.ofPattern("uuMMdd");

	private final DateTimeFormatter yyyyFormatter = DateTimeFormatter.ofPattern("uuuu");

	private final DateTimeFormatter mmFormatter = DateTimeFormatter.ofPattern("MM");

	private final DateTimeFormatter ddFormatter = DateTimeFormatter.ofPattern("dd");

	public UsdToJpyTasklet(@Value("#{jobParameters['baseDir']}") Path baseDir,
			@Value("#{jobParameters['startDate']}") LocalDate startDate,
			@Value("#{jobParameters['endDate']}") LocalDate endDate, RestClient.Builder restClientBuilder) {
		this.baseDir = Objects.requireNonNullElseGet(baseDir, () -> Paths.get("."));
		this.startDate = Objects.requireNonNull(startDate);
		this.endDate = Objects.requireNonNull(endDate);
		this.restClient = restClientBuilder.baseUrl("https://www.murc-kawasesouba.jp/fx/past").build();
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		log.info("Starting currency rate batch with date range: {} to {}", this.startDate, this.endDate);
		LocalDate currentDate = this.startDate;
		while (!currentDate.isAfter(this.endDate)) {
			LocalDate date = currentDate;
			processSingleDate(currentDate).ifPresent(jpy -> {
				String yyyy = date.format(yyyyFormatter);
				String mm = date.format(mmFormatter);
				String dd = date.format(ddFormatter);
				Path dirPath = baseDir.resolve(Paths.get(yyyy, mm, dd));
				log.info("Write files in {}", dirPath);
				try {
					Files.createDirectories(dirPath);
					Files.write(dirPath.resolve("TTS"), jpy.tts.toString().getBytes());
					Files.write(dirPath.resolve("TTB"), jpy.ttb.toString().getBytes());
					Files.write(dirPath.resolve("TTM"), jpy.ttm.toString().getBytes());
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			currentDate = currentDate.plusDays(1);
		}
		return RepeatStatus.FINISHED;
	}

	private Optional<Jpy> processSingleDate(LocalDate date) {
		String yymmdd = date.format(yymmddFormatter);
		ResponseEntity<String> response = this.restClient.get()
			.uri("/index.php?id={id}", yymmdd)
			.retrieve()
			.toEntity(String.class);
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (response.getStatusCode().is2xxSuccessful()) {
			String content = response.getBody();
			String usdContent = extractUsdContent(content);
			if (usdContent == null || usdContent.isEmpty()) {
				log.warn("No USD data found for {}", yymmdd);
				return Optional.empty();
			}
			List<BigDecimal> rateValues = extractRateValues(usdContent);
			if (rateValues.size() < 2) {
				log.warn("Not enough rate values found for {}", yymmdd);
				return Optional.empty();
			}
			BigDecimal tts = rateValues.get(0);
			BigDecimal ttb = rateValues.get(1);
			BigDecimal ttm = tts.add(ttb).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
			return Optional.of(new Jpy(tts, ttb, ttm));
		}
		else if (response.getStatusCode().is3xxRedirection()) {
			log.info("No USD data is not available for {}, use the day before", yymmdd);
			LocalDate previousDate = date.minusDays(1);
			return processSingleDate(previousDate);
		}
		log.warn("Unexpected response code {}", response.getStatusCode());
		return Optional.empty();
	}

	private String extractUsdContent(String content) {
		Pattern pattern = Pattern.compile("USD.*?t_right\">([\\d.]+).*?t_right\">([\\d.]+)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(0);
		}
		return null;
	}

	private List<BigDecimal> extractRateValues(String content) {
		List<BigDecimal> values = new ArrayList<>();
		Pattern pattern = Pattern.compile("t_right\">([\\d.]+)");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find() && values.size() < 2) {
			try {
				BigDecimal value = new BigDecimal(matcher.group(1));
				values.add(value);
			}
			catch (NumberFormatException ignored) {
			}
		}
		return values;
	}

	record Jpy(BigDecimal tts, BigDecimal ttb, BigDecimal ttm) {
	}

}