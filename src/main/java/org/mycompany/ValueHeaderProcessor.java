package org.mycompany;

import java.math.BigDecimal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.xml.XPathBuilder;

public class ValueHeaderProcessor implements Processor{
	@Override
	public void process(Exchange exchange) throws Exception{
		XPathBuilder xpath = XPathBuilder.xpath("/order/totalInvoice/text()");
		String value = xpath.evaluate(exchange, String.class);
		exchange.getIn().setHeader("totalInvoice",new BigDecimal(value));
	}
}
