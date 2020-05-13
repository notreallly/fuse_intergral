package org.sysage.com;

import org.apache.camel.builder.RouteBuilder;

public class TransformRouteBuilder extends RouteBuilder {

	public static String OUTPUT_FOLDER = "C:\\Users\\stevewu\\workspace\\camel_test";

	public static Integer BATCH_TIMEOUT = 10000;

	@Override
	public void configure() throws Exception {
		// add jpa consumer
		from("jpa:org.sysage.com.model.Order?persistenceUnit=mysql" 
				+ "&consumeDelete=false"
				+ "&consumer.namedQuery=getUndeliveredOrders" 
				+ "&consumer.delay=" + BATCH_TIMEOUT
				+ "&consumeLockEntity=false")
		// add wire tap to second route
		.wireTap("direct:updateOrder")
		// marshal order to XML with JAXB
		.marshal().jaxb()
		// split the order into individual order items
		.split(xpath("order/orderItems/orderItem"))
		// aggregate the order items based on their catalog item ID
		.aggregate(xpath("orderItem/catalogItem/id"),
			    new ReservationAggregationStrategy())
			    .completionInterval(BATCH_TIMEOUT)
			    .completeAllOnStop()
		// log the reservation XML to the console
		.log("${body}")
		// add file producer
		.setHeader("CatalogItemId", xpath("/reservation/catalogItemId/text()"))
		.to("file:" + OUTPUT_FOLDER + "?fileName=${header.CatalogItemId}/"
								+ "reservation-${date:now:yyyy-MM-dd_HH-mm-ss}.xml");
		// add second route to update order in the database
		from("direct:updateOrder")
		.process(new DeliverOrderProcessor())
		.to("jpa:com.redhat.training.jb421.model.Order?persistenceUnit=mysql");
	}

}
