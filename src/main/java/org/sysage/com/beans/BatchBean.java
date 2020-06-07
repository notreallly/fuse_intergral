package org.sysage.com.beans;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.camel.Exchange;
import org.apache.camel.language.XPath;

public class BatchBean {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

	public void setFileName(Exchange exchange, @XPath("/batch/@date") String date,
			@XPath("/batch/@number") String batchNumber) {
		ZonedDateTime batchDate = ZonedDateTime.parse(date);
		// TODO set the CamelFileName attribute from the header to "order-audit-" +
		// batchDate.format(formatter) + "B" + batchNumber + ".xml"
		exchange.getIn().setHeader("CamelFileName",
				"order-audit-" + batchDate.format(formatter) + "B" + batchNumber + ".xml");
	}

}
