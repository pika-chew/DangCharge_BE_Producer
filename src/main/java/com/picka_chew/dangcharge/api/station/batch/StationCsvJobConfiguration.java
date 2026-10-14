package com.picka_chew.dangcharge.api.station.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Profile;

import com.picka_chew.dangcharge.api.station.port.StationCsvCollector;

@Configuration
@EnableBatchProcessing
@Profile("station-csv-batch")
public class StationCsvJobConfiguration {

	@Bean
	public Job stationCsvJob(JobRepository jobRepository, Step stationCsvStep) {
		return new JobBuilder("stationCsvJob", jobRepository).start(stationCsvStep).build();
	}

	@Bean
	public Step stationCsvStep(JobRepository jobRepository, StationCsvCollector collector) {
		return new StepBuilder("stationCsvStep", jobRepository)
				.tasklet((contribution, chunkContext) -> {
					int count = collector.collect();
					contribution.incrementWriteCount(count);
					return RepeatStatus.FINISHED;
				})
				.build();
	}

}
