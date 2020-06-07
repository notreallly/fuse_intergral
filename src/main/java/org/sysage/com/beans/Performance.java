package org.sysage.com.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.camel.Exchange;

public class Performance {
	private ConcurrentMap<Integer, Map<String, Long>> stats = new ConcurrentHashMap<Integer, Map<String, Long>>(20);
	
	public void start(Exchange exchange) {
		Integer batchNumber = getBatchNumber(exchange);
		stats.put(batchNumber, new HashMap<String, Long>(2));
		stats.get(batchNumber).put("start", System.currentTimeMillis());
	}
	
	public void stop(Exchange exchange) {
		Integer batchNumber = getBatchNumber(exchange);
		Map<String, Long> values = stats.get(batchNumber);
		if (values == null)
			throw new RuntimeException("Starting stats for batch " + batchNumber + " are missing.");
		values.put("stop", System.currentTimeMillis());
	}
	
	public void dumpStats() {
		for (Entry<Integer, Map<String, Long>> batch : stats.entrySet()) {
			System.out.println(String.format("Batch %d processed in %d ms.", batch.getKey(), batch.getValue().get("stop") - batch.getValue().get("start")));
		}
	}
	
	private Integer getBatchNumber(Exchange exchange) {
		return exchange.getIn().getHeader("batchNumber", Integer.class);
	}
}
