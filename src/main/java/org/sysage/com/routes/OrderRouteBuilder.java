package org.sysage.com.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.BindyType;
import org.sysage.com.beans.BatchOrderAggregationStrategy;
import org.sysage.com.model.Order;

public class OrderRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		//TODO read classes from the com.redhat.training.model package for JAXB
		JaxbDataFormat df = new JaxbDataFormat("org.sysage.com.model");
		// TODO Read only orders with the following file name: orders-v1-.*.csv"
		from("file:/tmp/data/orders1?include=orders-v1-.*.csv")
			.routeId("OrderRoute1")
			.threads(5)
			.to("direct:process");
		
		from("direct:process")
			.routeId("CommonProcessing")
			.onCompletion()
				.bean("performance", "stop")
			.end()
			//TODO Call the processor named assignBatch configured in camel-context.xml
			.process("assignBatch")
			.bean("performance", "start")
			//TODO log each batch number to the console
			.log("Processing file: ${header.CamelFileName} in Batch ${header.batchNumber}")
			//TODO Convert each exchange to a Order object using Bindy
			.unmarshal().bindy(BindyType.Csv, Order.class)
			//TODO split the list of orders to a single order
			.split(body()).streaming()
			//TODO Call order bean to update the order state and add the origin file name
			.bean("order")
			//TODO use a wiretap for auditing purposes
			.wireTap("seda:audit")
			//TODO Convert to XML using JAXB
			.marshal(df)
			.to("file:/tmp/orders?fileName=order-${bean:order.generateRandomFileName}.xml")
			;
		
		from("seda:audit")
			.routeId("Audit")
			//TODO Aggregate orders based on the batchNumber provided by the assignBatch processor
			.aggregate(header("batchNumber"), new BatchOrderAggregationStrategy())
			//TODO aggregate all the order after 2 seconds.
			.completionTimeout(2000)
			.bean("batch")
			.choice()
			.when(xpath("number(/batch/@total) > 100"))
			  //TODO if the total batch is over US$ 100 than save to /tmp/audit/large
			  .to("file:/tmp/audit/large")
			.otherwise()
			  //TODO if the total batch is under US$ 100 than save to /tmp/audit/small
			  .to("file:/tmp/audit/small")
			.end()
			;

	}

}
