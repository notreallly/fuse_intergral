package org.sysage.com.model;

import java.io.Serializable;
import java.util.Date;

public class Reservation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Integer id;
	
	private Integer catalogItemId;
	
	private Integer quantity;
	
	private Date reservationDate;

	public Integer getCatalogItemId() {
		return catalogItemId;
	}

	public void setCatalogItemId(Integer catalogItemId) {
		this.catalogItemId = catalogItemId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Date getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(Date resevationDate) {
		this.reservationDate = resevationDate;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + id + ", catalogItemId=" + catalogItemId + ", quantity=" + quantity
				+ ", reservationDate=" + reservationDate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((catalogItemId == null) ? 0 : catalogItemId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
		result = prime * result + ((reservationDate == null) ? 0 : reservationDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reservation other = (Reservation) obj;
		if (catalogItemId == null) {
			if (other.catalogItemId != null)
				return false;
		} else if (!catalogItemId.equals(other.catalogItemId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		if (reservationDate == null) {
			if (other.reservationDate != null)
				return false;
		} else if (!reservationDate.equals(other.reservationDate))
			return false;
		return true;
	}

}
