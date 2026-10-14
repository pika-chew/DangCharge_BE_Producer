package com.picka_chew.dangcharge.api.station.port;

import java.io.IOException;

/** Downloads the complete station installation snapshot and publishes its CSV. */
public interface StationCsvCollector {

	int collect() throws IOException, InterruptedException;

}
