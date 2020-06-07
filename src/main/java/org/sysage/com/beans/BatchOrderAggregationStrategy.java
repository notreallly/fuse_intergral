package org.sysage.com.beans;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.sysage.com.model.Batch;
import org.sysage.com.model.Order;

public class BatchOrderAggregationStrategy implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		// TODO if this is the first aggregate execution, then
		if (oldExchange == null) {
			// TODO create a new batch instance with the value
			// obtained from batchNumber (Header attribute)
			Batch batch = new Batch((int) newExchange.getIn().getHeader("batchNumber"));
			// TODO add the order obtained from the body
			batch.addOrder(newExchange.getIn().getBody(Order.class));
			// TODO Add the order to the batch
			newExchange.getIn().setBody(batch);
			return newExchange;
		}
		// TODO otherwise get the batch stored in the oldExchange
		Batch batch = oldExchange.getIn().getBody(Batch.class);
		// TODO get the order from the newExchange
		Order order = newExchange.getIn().getBody(Order.class);
		// TODO add the order to the Batch instance
		batch.addOrder(order);
		// TODO store the batch to the oldExchange body
		oldExchange.getIn().setBody(batch);
		// TODO return the oldExchange
		return oldExchange;
	}

}
