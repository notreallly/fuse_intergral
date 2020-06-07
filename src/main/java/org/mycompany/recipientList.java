package org.mycompany;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class recipientList extends RouteBuilder{
	
	final private static String XPATH_VENDOR_NAME ="/order/orderItems/orderItem/orderItemPublisherName/text()";
	final private static String XPATH_ORDERID ="/order/orderId/text()";
	
	@Override
	public void configure() throws Exception {
	//	from("file:orders/incoming?include=order.*xml")
	//	.recipientList("mock:direct:start,mock:direct:foo,mock:log:foo");
	//	from("direct:start")
	//	.recipientList("mock:log:foo,mock:direct:start,mock:direct:foo").body().cacheSize(100);
		
		from("file:orders/incoming?include=order.*xml")
	    .recipientList(header("recipients"), ",")
	    .aggregationStrategy(new AggregationStrategy() {
	            public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
	                if (oldExchange == null) {
	                    return newExchange;
	                }

	                String body = oldExchange.getIn().getBody(String.class);
	                oldExchange.getIn().setBody(body + newExchange.getIn().getBody(String.class));
	                return oldExchange;
	            }
	        })
	        .parallelProcessing().timeout(250)
	    // use end to indicate end of recipientList clause
	    .end()
	    .to("mock:result");

	from("direct:foo").delay(500).to("mock:direct:foo").setBody(constant("Hello World"));

	from("direct:start").to("mock:direct:start").setBody(constant("Hello World"));

	//from("log:foo").to("mock:log:foo").setBody(constant("Bye World"));
		
	}
}