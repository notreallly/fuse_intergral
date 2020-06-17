package org.sysage.com;

import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.BrowsableEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OrderRouteTest extends CamelSpringTestSupport {

	@Produce(uri = "file:/tmp/data/orders1")
	private ProducerTemplate orders1Template;
	@Produce(uri = "file:/tmp/data/orders2")
	private ProducerTemplate orders2Template;
	@EndpointInject(uri = "file:/tmp/orders")
	private BrowsableEndpoint ordersEndpoint;
	@EndpointInject(uri = "file:/tmp/audit/large")
	private BrowsableEndpoint largeAuditEndpoint;
	@EndpointInject(uri = "file:/tmp/audit/small")
	private BrowsableEndpoint smallAuditEndpoint;
	@EndpointInject(uri = "mock:order")
	MockEndpoint orderEndpoint;

	@Before
	public void setup() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		CamelTestSupport.deleteDirectory("/tmp/orders");
		CamelTestSupport.deleteDirectory("/tmp/audit");
		verifyEndpointsFound();
	}

	@Test
	@DirtiesContext
	public void testFileProcessed() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).whenDone(1).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");

		if (!notify.matches(3, TimeUnit.SECONDS)) {
			Assert.fail("3 orders should have been processed");
		}
	}

	@Test
	@DirtiesContext
	public void testOrdersProcessed() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).wereSentTo("file:/tmp/orders*").whenDone(3).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");

		if (!notify.matches(3, TimeUnit.SECONDS)) {
			Assert.fail("3 orders should have been processed");
		}

		Assert.assertEquals("Expecting 3 orders", 3, orderEndpoint.getExchanges().size());

		int found = 0;
		for (int i = 0; i < XML_ORDER_1.length; i++) {
			boolean bFound = false;
			for (Exchange e : orderEndpoint.getExchanges()) {
				String body = e.getIn().getBody(String.class);
				if (XML_ORDER_1[i].equals(body)) {
					++found;
					bFound = true;
					break;
				}
			}
			if (!bFound)
				System.out.println("Missing order: \n" + XML_ORDER_1[i]);
		}
		Assert.assertEquals("Missing XML orders", XML_ORDER_1.length, found);
	}

	@Test
	@DirtiesContext
	public void testMaskedOurField() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).wereSentTo("file:/tmp/orders*").whenDone(1).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");

		if (!notify.matches(3, TimeUnit.SECONDS)) {
			Assert.fail("3 orders should have been processed");
		}

		Assert.assertTrue("Expecting 1 or more orders", orderEndpoint.getExchanges().size() >= 1);
		String body = orderEndpoint.getExchanges().get(0).getIn().getBody(String.class);
		Assert.assertTrue("State field is not masked", body.contains("state=\"**\""));
	}

	@Test
	@DirtiesContext
	public void testAuditProcessed() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).wereSentTo("file:/tmp/audit/large").whenDone(1).and()
				.wereSentTo("file:/tmp/audit/small").whenDone(1).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");
		orders2Template.sendBodyAndHeader(CSV_ORDER_2, "CamelFileName", "orders-v1-A2.csv");

		if (!notify.matches(5, TimeUnit.SECONDS)) {
			Assert.fail("2 batches should have been processed");
		}

		Assert.assertEquals("Missing large audit file", 1, largeAuditEndpoint.getExchanges().size());
		String largeBatch = largeAuditEndpoint.getExchanges().get(0).getIn().getBody(String.class);
		Assert.assertTrue("Large batch amount should be $129", largeBatch.contains("total=\"129.00\""));
		Assert.assertEquals("Missing small audit file", 1, smallAuditEndpoint.getExchanges().size());
		String smallBatch = smallAuditEndpoint.getExchanges().get(0).getIn().getBody(String.class);
		Assert.assertTrue("Small batch amount should be $57", smallBatch.contains("total=\"57.00\""));
	}

	@Test
	@DirtiesContext
	public void testAuditFileNameFormat() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).wereSentTo("file:/tmp/audit/small").whenDone(1).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");

		if (!notify.matches(5, TimeUnit.SECONDS)) {
			Assert.fail("1 small batche should have been processed");
		}

		Assert.assertEquals("Missing small audit file", 1, smallAuditEndpoint.getExchanges().size());
		LocalDateTime datetime = LocalDateTime.now();
		String fileNameRegex = String.format("order-audit-%02d%02d%02d[0-9]{6}B[0-9]*.xml", datetime.getYear() - 2000,
				datetime.getMonthValue(), datetime.getDayOfMonth());
		String fileName = smallAuditEndpoint.getExchanges().get(0).getIn().getHeader("CamelFileName").toString();
		Assert.assertTrue("Audit file name is in wrong format, found: " + fileName, fileName.matches(fileNameRegex));
	}

	@Test
	@DirtiesContext
	public void testAuditFileContent() throws Exception {
		NotifyBuilder notify = new NotifyBuilder(context).wereSentTo("file:/tmp/audit/small").whenDone(1).create();
		context.start();
		orders1Template.sendBodyAndHeader(CSV_ORDER_1, "CamelFileName", "orders-v1-A1.csv");

		if (!notify.matches(5, TimeUnit.SECONDS)) {
			Assert.fail("1 small batche should have been processed");
		}

		Assert.assertEquals("Missing small audit file", 1, smallAuditEndpoint.getExchanges().size());

		Document document = smallAuditEndpoint.getExchanges().get(0).getIn().getBody(Document.class);
		Assert.assertNotNull("XML document could not be parsed", document);

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		try {
			XPathExpression xpathExpr = xpath.compile("/batch");
			Node batchNode = (Node) xpathExpr.evaluate(document, XPathConstants.NODE);
			Assert.assertNotNull("Batch XML tag not found in document", batchNode);

			NamedNodeMap batchAttrs = batchNode.getAttributes();
			Assert.assertNotNull("Batch number attribute is missing", batchAttrs.getNamedItem("number"));
			Assert.assertNotNull("Total attribute is missing", batchAttrs.getNamedItem("total"));
			Assert.assertNotNull("Date attribute is missing", batchAttrs.getNamedItem("date"));

			NodeList orders = (NodeList) xpath.compile("/batch/orders/order").evaluate(document,
					XPathConstants.NODESET);
			Assert.assertNotNull("Order elements not found in document: /batch/orders/order", orders);
			Assert.assertEquals("3 orders in batch expected", 3, orders.getLength());

			NamedNodeMap orderAttrs = orders.item(0).getAttributes();
			Assert.assertNotNull("Order name attribute missing", orderAttrs.getNamedItem("name"));
			Assert.assertNotNull("Order date attribute missing", orderAttrs.getNamedItem("orderDate"));
			Assert.assertNotNull("Order street attribute missing", orderAttrs.getNamedItem("street"));
			Assert.assertNotNull("Order state attribute missing", orderAttrs.getNamedItem("state"));
			Assert.assertNotNull("Order extendedAmount attribute missing", orderAttrs.getNamedItem("extendedAmount"));
			Assert.assertNotNull("Order filename attribute missing", orderAttrs.getNamedItem("filename"));
			
		} catch (XPathExpressionException e) {
			Assert.fail("Invalid test\n" + e.getMessage());
		}
	}

	private void verifyEndpointsFound() {
		Assert.assertNotNull("Check /tmp/audit/large endpoint", largeAuditEndpoint);
	}

	private final static String CSV_ORDER_1 = "John Doe,11/10/16,123 Easy St,AS,14.0\n"
			+ "Amy Smith,10/31/16,2 Knightdown Ave,CA,28.0\n"
			+ "Terry Jones,11/02/16,2 Baker St,OR,15.0";

	private final static String CSV_ORDER_2 = "Mary Doe,11/10/16,123 Easy St,AS,42.0\n"
			+ "Paul Smith,10/31/16,123 Easy St,AS,42.0\n"
			+ "Mike Jones,11/02/16,2 Baker St,OR,45.00";

	private final static String[] XML_ORDER_1 = new String[] {
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<order name=\"John Doe\" orderDate=\"2016-11-10T00:00:00-05:00\" street=\"123 Easy St\" state=\"**\" extendedAmount=\"14.00\"/>",
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<order name=\"Amy Smith\" orderDate=\"2016-10-31T00:00:00-04:00\" street=\"2 Knightdown Ave\" state=\"**\" extendedAmount=\"28.00\"/>",
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<order name=\"Terry Jones\" orderDate=\"2016-11-02T00:00:00-04:00\" street=\"2 Baker St\" state=\"**\" extendedAmount=\"15.00\"/>"
	};

	@Override
	protected AbstractApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext("/spring/camel-context.xml");
	}

	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

}
