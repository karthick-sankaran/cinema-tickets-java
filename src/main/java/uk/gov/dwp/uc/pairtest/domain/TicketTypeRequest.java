package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

	private final int noOfTickets;
	private final Type type;

	public TicketTypeRequest(Type type, int noOfTickets) {
		if (type == null) {
			throw new IllegalArgumentException("Ticket type cannot be null");
		}
		if (noOfTickets <= 0) {
			throw new IllegalArgumentException("Each ticket request must have at least 1 ticket");
		}
		this.type = type;
		this.noOfTickets = noOfTickets;
	}

	public int getNoOfTickets() {
		return noOfTickets;
	}

	public Type getTicketType() {
		return type;
	}

	public enum Type {
		ADULT, CHILD, INFANT
	}

}
