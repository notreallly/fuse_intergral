package fuse_intergral;

import java.util.concurrent.TimeUnit;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ErrorHandlingTest extends CamelTestSupport {
	
	@Produce(uri = "file:orders/incoming")
	private ProducerTemplate fileOrders;
	@Produce(uri = "direct:auditing")
	private ProducerTemplate directAuditing;
	
	@EndpointInject(uri = "mock:MA")
	private MockEndpoint mockMa;
	@EndpointInject(uri = "mock:AK")
	private MockEndpoint mockAk;
	
	String maStateContent = "<order>\n" + 
		    "	<orderId>1</orderId>\n" + 
		    "	<orderDate>2016-12-10T12:01:00-05:00</orderDate>\n" + 
		    "	<totalInvoice>MA</totalInvoice>\n" + 
		    "	<customer>\n" + 
		    "		<shippingAddress>\n" ; 
	
	@Test
	  public void testFileWithMARoute() throws Exception {
//The anonymous class must override the configure method and intercept request 
//sent to the file:orders/output/MA endpoint to the mock:MA endpoint
	    AdviceWithRouteBuilder mockRoute = new AdviceWithRouteBuilder() {
	      @Override
	      public void configure() throws Exception {
	        interceptSendToEndpoint("file:orders/output/MA")
	          .skipSendToOriginalEndpoint()
	      .to("mock:MA");
	      }
	    };
//Invoke the adviceWith method to update the errorProcess route using the new route definition.
	    context.getRouteDefinition("errorProcess").adviceWith(context, mockRoute);
//Start the context to use the updated route definition.
	    context.start();
//you need to indicate Camel how many messages the route processes and 
//how much time the route must wait until a message is received
	    NotifyBuilder builder = new NotifyBuilder(context).whenDone(1).create();
	    builder.matches(2, TimeUnit.SECONDS);
//Send an Exchange instance in the fileOrders attribute using the sendBodyAndHeader method. 
//Use the maStateContent attribute as the Exchange body.
	    fileOrders.sendBodyAndHeader(maStateContent,Exchange.FILE_NAME, "file.xml");
//Use the mockMa injected attribute to check whether a single message was received by the endpoint.
	    mockMa.expectedMessageCount(1);
//Invoke the assertion method to inspect that the expectations were met.	    
	    assertMockEndpointsSatisfied();
//Invoke the stop method from the context inherited attribute.
	    context.stop();
	    
	}
}
