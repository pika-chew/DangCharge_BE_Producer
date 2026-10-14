package com.picka_chew.dangcharge.api.station.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.picka_chew.dangcharge.api.station.port.StationCsvCollector;

class StationCsvJobConfigurationTest {

	@Test
	void jobRunsCollectorOnceAndRecordsCount() throws Exception {
		AtomicInteger calls = new AtomicInteger();
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.getEnvironment().setActiveProfiles("station-csv-batch");
			context.registerBean(StationCsvCollector.class, () -> () -> {
				calls.incrementAndGet();
				return 7;
			});
			context.register(StationCsvJobConfiguration.class);
			context.refresh();

			JobExecution execution = context.getBean(JobOperator.class).start(
					context.getBean("stationCsvJob", Job.class),
					new JobParametersBuilder().addLong("run.id", 1L).toJobParameters());

			assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
			assertThat(execution.getStepExecutions()).singleElement().satisfies(step ->
					assertThat(step.getWriteCount()).isEqualTo(7));
			assertThat(calls.get()).isEqualTo(1);
		}
	}

}
