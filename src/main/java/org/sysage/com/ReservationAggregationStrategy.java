package org.sysage.com;

import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.sysage.com.model.OrderItem;
import org.sysage.com.model.Reservation;

public class ReservationAggregationStrategy implements AggregationStrategy {

	@Override
	public Exchange aggregate(Exchange oldEx, Exchange newEx) {
		OrderItem newBody = newEx.getIn().getBody(OrderItem.class);
		Reservation reservation = null;
		if (oldEx == null) {
			reservation = new Reservation();
			reservation.setReservationDate(new Date());
			reservation.setQuantity(newBody.getQuantity());
			newEx.getIn().setBody(reservation);
			return newEx;
		} else {
			reservation = oldEx.getIn().getBody(Reservation.class);
			reservation.setQuantity(reservation.getQuantity() + newBody.getQuantity());
			return oldEx;
		}
	}

}
