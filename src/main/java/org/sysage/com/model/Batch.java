package org.sysage.com.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Batch implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private int number;
	
	@XmlAttribute
	private BigDecimal total = new BigDecimal(0);
	
	@XmlAttribute
	private Date date = new Date();
	
	@XmlElementWrapper(name = "orders")
	@XmlElement(name = "order")
	private List<Order> orders = new ArrayList<Order>(10);

	public Batch() {
	}

	public Batch(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void addOrder(Order order) {
		this.orders.add(order);
		this.total = this.total.add(order.getExtendedAmount());
	}
}
