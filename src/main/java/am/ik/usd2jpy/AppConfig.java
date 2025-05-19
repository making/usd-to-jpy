package am.ik.usd2jpy;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
public class AppConfig {

	@Bean
	public RestClientCustomizer restClientCustomizer(Logbook logbook) {
		return builder -> builder.defaultStatusHandler(__ -> true, (req, res) -> {
		}).requestInterceptor(new LogbookClientHttpRequestInterceptor(logbook));
	}

}
