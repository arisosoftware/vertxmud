package com.arisosoftware.vertbench;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class StopWatchInfo {

	public long StartTimeMillis;
	public long Duration;
	public String Message;

	static DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
			.withZone(ZoneId.systemDefault());

	static DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("mm:ss.SSS").withZone(ZoneId.systemDefault());

	public StopWatchInfo() {
		Start();
	}

	public void Start() {
		this.StartTimeMillis = Instant.now().toEpochMilli();
	}

	public void Stop() {
		long currentTimeMillis = Instant.now().toEpochMilli();
		this.Duration = currentTimeMillis - this.StartTimeMillis;
	}

	public String Report() {
		Instant start = Instant.ofEpochMilli(StartTimeMillis);
		Instant duration = Instant.ofEpochMilli(Duration);

		return String.format("%s From %s Usage %s ", this.Message, formatter1.format(start),
				formatter2.format(duration));
	}
}
