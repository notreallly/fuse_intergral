package org.mycompany;

import org.apache.camel.builder.RouteBuilder;

public class ErrorHandlingRouteBuilder extends RouteBuilder {
@Override
public void configure() throws Exception {
//provide an error handler that captures any exception raised
//and send them to the orders/problems directory
	errorHandler(
		deadLetterChannel("file:orders/problems")
		.disableRedelivery()
		    );
	
    from("file:orders/incoming")
      .routeId("errorProcess")
      .choice()
        .when(
          xpath("/order/customer/shippingAddress/state/text() = 'AK'"))
          .to("file:orders/output/AK")
        .when(
          xpath("/order/customer/shippingAddress/state/text() = 'MA'"))
          .to("file:orders/output/MA")
        .otherwise()
    .to("direct:auditing");

//exception raised in the route whose starting endpoint is 
//direct:auditing and send them to the orders/trash directory.
from("direct:auditing")
  .routeId("auditing")
  .doTry()
    .process(new ValueHeaderProcessor())
    .to("file:orders/root/dest")
  .doCatch(NumberFormatException.class)
  	.to("file:orders/trash")
  	.endDoTry();
}
}