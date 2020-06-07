package org.sysage.com.beans;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.sysage.com.model.Order;

public class OrderBean {
	
	public Order processOrder(Exchange exchange, Order order) {
		//TODO Set filename associated with this order.
		order.setFilename(exchange.getIn().getHeader("CamelFileName").toString());
		//TODO Set order state to **
		order.setState("**");
		return order;
	}
	
	public String generateRandomFileName() {
		return UUID.randomUUID().toString();
	}

}
