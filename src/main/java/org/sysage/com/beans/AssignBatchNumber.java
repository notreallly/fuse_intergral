package org.sysage.com.beans;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class AssignBatchNumber implements Processor {

	private int currentBatchNumber = 0;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO set a header attribute named batchNumber and use the getBatchNumber
		// method to calculate the value
		exchange.getIn().setHeader("batchNumber", getBatchNumber());
	}

	private synchronized int getBatchNumber() {
		return ++currentBatchNumber;
	}
}
