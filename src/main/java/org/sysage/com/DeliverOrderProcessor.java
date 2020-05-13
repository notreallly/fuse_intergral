package org.sysage.com;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.sysage.com.model.Order;

public class DeliverOrderProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		Order order = exchange.getIn().getBody(Order.class);
		order.setDelivered(true);
	}

}
