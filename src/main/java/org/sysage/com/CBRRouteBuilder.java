package org.sysage.com;

import org.apache.camel.builder.RouteBuilder;

public class CBRRouteBuilder extends RouteBuilder{
	
	final private static String XPATH_VENDOR_NAME ="/order/orderItems/orderItem/orderItemPublisherName/text()";
	final private static String XPATH_ORDERID ="/order/orderId/text()";
	
	@Override
	public void configure() throws Exception {
		from("file:orders/incoming?include=order.*xml")
		  .setHeader("orderId", xpath(XPATH_ORDERID))
		  .setHeader("vendorName",xpath(XPATH_VENDOR_NAME))
		  .process(new TestProcessor())
		  .filter(simple("${header.skipOrder} == null"))
		  .choice()
		    .when(simple("${header.vendorName} == 'ABC Company'"))
		      .log("sending order ${header.orderId} to folder abc")
			.to("file:orders/outgoing/abc")
		    .when(simple("${header.vendorName} == 'ORly'"))
		      .log("sending order ${header.orderId} to folder orly")
		      .to("file:orders/outgoing/orly")
		    .when(simple("${header.vendorName} == 'Namming'"))
		      .log("sending order ${header.orderId} to folder namming")
		      .to("file:orders/outgoing/namming")
		    .otherwise()
		      .log("Unknown vendor");
	}
}
