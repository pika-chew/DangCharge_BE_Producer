package com.picka_chew.dangcharge.infra.kepco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.station.port.StationDataReader;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import lombok.extern.slf4j.Slf4j;

/**
 * scripts/get_ev_data.py가 한전 설치현황 API에서 받아 저장한 CSV를 읽는다.
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

	private final Path csvPath;

	public KepcoStationCsvReader(@Value("${dangcharge.station-import.csv-path}") Path csvPath) {
		this.csvPath = csvPath;
	}

	@Override
	public List<StationSnapshot> read() {
		if (!Files.isRegularFile(csvPath)) {
			throw new IllegalStateException("충전소 CSV가 없습니다. scripts/get_ev_data.py를 먼저 실행하세요: "
					+ csvPath.toAbsolutePath());
		}
		Set<String> unknownLabels = new HashSet<>();
		List<StationSnapshot> snapshots = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
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
			throw new UncheckedIOException("충전소 CSV를 읽지 못했습니다: " + csvPath.toAbsolutePath(), e);
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
