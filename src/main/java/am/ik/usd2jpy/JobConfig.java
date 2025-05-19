package am.ik.usd2jpy;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
public class JobConfig {

	private final JobRepository jobRepository;

	public JobConfig(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}

	@Bean
	public Step usdToJpyStep(Tasklet usdToJpyTasklet, PlatformTransactionManager transactionManager) {
		return new StepBuilder("UsdToJpy", jobRepository).tasklet(usdToJpyTasklet, transactionManager).build();
	}

	@Bean
	public Job usdToJpyJob(Step usdToJpyStep) {
		return new JobBuilder("UsdToJpy", jobRepository).start(usdToJpyStep).build();
	}

}
