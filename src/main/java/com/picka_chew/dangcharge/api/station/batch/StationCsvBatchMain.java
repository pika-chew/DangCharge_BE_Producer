package com.picka_chew.dangcharge.api.station.batch;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

/** One-shot entry point for a station CSV Spring Batch job. */
public final class StationCsvBatchMain {

	private StationCsvBatchMain() {
	}

	public static void main(String[] args) throws Exception {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.getEnvironment().setActiveProfiles("station-csv-batch");
			loadLocalEnv(context);
			context.register(StationCsvJobConfiguration.class);
			context.scan("com.picka_chew.dangcharge.infra.kepco.batch");
			context.refresh();

			JobExecution execution = context.getBean(JobOperator.class).start(
					context.getBean("stationCsvJob", Job.class),
					new JobParametersBuilder().addLong("run.id", System.currentTimeMillis()).toJobParameters());
			if (execution.getStatus() != BatchStatus.COMPLETED) {
				throw new IllegalStateException("충전소 CSV 배치 실패: " + execution.getStatus(),
						execution.getFailureExceptions().isEmpty() ? null : execution.getFailureExceptions().get(0));
			}
		}
	}

	private static void loadLocalEnv(AnnotationConfigApplicationContext context) throws IOException {
		Path envFile = Path.of(".env");
		if (!Files.isRegularFile(envFile)) {
			return;
		}
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key).trim();
			if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\""))
					|| (value.startsWith("'") && value.endsWith("'")))) {
				value = value.substring(1, value.length() - 1);
			}
			properties.setProperty(key, value);
		}
		context.getEnvironment().getPropertySources().addLast(new PropertiesPropertySource("dotenv", properties));
	}

}
