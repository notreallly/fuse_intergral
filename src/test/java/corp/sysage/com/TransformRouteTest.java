package corp.sysage.com;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.sysage.com.TransformRouteBuilder;
import org.sysage.com.model.Order;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(CamelSpringRunner.class)
@ContextConfiguration(locations = { "/spring/camel-context.xml" })
@UseAdviceWith(true)
@Transactional(transactionManager = "transactionManager")
public class TransformRouteTest {

	private static final String TEST_FILE_DIR = "C:\\Users\\stevewu\\workspace\\fuse_intergral\\ordersJSON";

	@Autowired
	public CamelContext camelContext;

	@PersistenceContext
	private EntityManager em;

	@Before
	public void setup() throws IOException, InterruptedException {
		clearOutput();
		clearStaleData();
	}

	@Test
	@DirtiesContext
	public void test() throws Exception {
		createNewOrder(TEST_FILE_DIR + "order1.json");
		createNewOrder(TEST_FILE_DIR + "order2.json");
		TestTransaction.flagForCommit();
		TestTransaction.end();

		camelContext.start();

		Thread.sleep(10000);
		TestTransaction.start();
		createNewOrder(TEST_FILE_DIR + "order1.json");
		createNewOrder(TEST_FILE_DIR + "order2.json");
		createNewOrder(TEST_FILE_DIR + "order3.json");
		TestTransaction.flagForCommit();
		TestTransaction.end();
		Thread.sleep(10000);

		File resultsDir = new File(TransformRouteBuilder.OUTPUT_FOLDER);
		int catalogitemIds = resultsDir.list().length;
		assertEquals(3, catalogitemIds);

		for (File subFolder : resultsDir.listFiles()) {
			assertEquals(2, subFolder.list().length);
		}

		Query q = em.createNamedQuery("getUndeliveredOrders", Order.class);
		assertEquals(0, q.getResultList().size());
	}

	private void clearStaleData() throws InterruptedException {
		Query q = em.createQuery("from Order o", Order.class);
		List<Order> orders = (List<Order>) q.getResultList();
		for (Order o : orders) {
			em.remove(o);
		}
		em.flush();
	}

	private void createNewOrder(String path) throws IOException {
		byte[] jsonData = Files.readAllBytes(Paths.get(path));
		ObjectMapper om = new ObjectMapper();
		Order order = om.readValue(jsonData, Order.class);
		em.persist(order);
		em.flush();
	}

	private void clearOutput() throws IOException {
		CamelTestSupport.deleteDirectory(TransformRouteBuilder.OUTPUT_FOLDER);
	}

}
