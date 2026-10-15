package com.picka_chew.dangcharge.infra.kepco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.station.port.StationDataReader;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import lombok.extern.slf4j.Slf4j;

/**
 * 한전 설치현황 CSV를 읽는다. 기본은 JAR에 함께 들어간 classpath:data/ev_charge_stations.csv이고,
 * dangcharge.station-import.csv-path(STATION_IMPORT_CSV_PATH)로 다른 파일을 지정할 수 있다.
 * 경로는 "classpath:", "file:" 접두사를 붙이거나 일반 파일 경로로 쓴다.
 * 컬럼: metro, city, stnPlace, stnAddr, rapidCnt, slowCnt, carType
 */
@Slf4j
@Component
public class KepcoStationCsvReader implements StationDataReader {

	private static final int BOM = 0xFEFF;
	private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
			.setHeader()
			.setSkipHeaderRecord(true)
			.get();

	private final Resource csv;

	@Autowired
	public KepcoStationCsvReader(@Value("${dangcharge.station-import.csv-path}") String location,
			ResourceLoader resourceLoader) {
		this(resolve(location, resourceLoader));
	}

	KepcoStationCsvReader(Path path) {
		this(new FileSystemResource(path));
	}

	KepcoStationCsvReader(Resource csv) {
		this.csv = csv;
	}

	private static Resource resolve(String location, ResourceLoader resourceLoader) {
		if (location.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX) || location.startsWith("file:")) {
			return resourceLoader.getResource(location);
		}
		return new FileSystemResource(location);
	}

	@Override
	public List<StationSnapshot> read() {
		if (!csv.exists()) {
			throw new IllegalStateException("충전소 CSV가 없습니다: " + csv.getDescription());
		}
		Set<String> unknownLabels = new HashSet<>();
		List<StationSnapshot> snapshots = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8))) {
			skipBom(reader);
			try (CSVParser parser = FORMAT.parse(reader)) {
				for (CSVRecord record : parser) {
					snapshots.add(new StationSnapshot(
							record.get("metro").trim(),
							record.get("city").trim(),
							record.get("stnPlace").trim(),
							record.get("stnAddr").trim(),
							Integer.parseInt(record.get("rapidCnt").trim()),
							Integer.parseInt(record.get("slowCnt").trim()),
							parseModels(record.get("carType"), unknownLabels)));
				}
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("충전소 CSV를 읽지 못했습니다: " + csv.getDescription(), e);
		}
		if (!unknownLabels.isEmpty()) {
			log.warn("선택지에 없는 차종이라 건너뜀: {}", unknownLabels);
		}
		return snapshots;
	}

	private static void skipBom(BufferedReader reader) throws IOException {
		reader.mark(1);
		if (reader.read() != BOM) {
			reader.reset();
		}
	}

	private static Set<VehicleModel> parseModels(String carType, Set<String> unknownLabels) {
		Set<VehicleModel> models = EnumSet.noneOf(VehicleModel.class);
		for (String label : carType.split(",")) {
			if (label.isBlank()) {
				continue;
			}
			VehicleModel.fromLabel(label).ifPresentOrElse(models::add, () -> unknownLabels.add(label.trim()));
		}
		return models;
	}

}
