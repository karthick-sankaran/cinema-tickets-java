package uk.gov.dwp.uc.pairtest;

import java.util.Objects;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * Implementation of TicketService that enforces all business rules: - Max 25
 * tickets - At least one Adult ticket if any Child/Infant ticket is purchased -
 * Infants are free and do not require seats - Correct total cost and seat
 * reservation calculations
 */
public class TicketServiceImpl implements TicketService {
	private static final int MAXIMUM_TICKETS = 25; // Maximum allowed tickets per purchase
	private static final int CHILD_TICKET_PRICE = 15; // Price per child ticket (£15)
	private static final int ADULT_TICKET_PRICE = 25; // Price per adult ticket (£25)

	private final TicketPaymentService ticketPaymentService;
	private final SeatReservationService seatReservationService;

	/**
	 * Constructor to initialize the TicketServiceImpl with required services.
	 *
	 * @param paymentService     The payment processing service.
	 * @param reservationService The seat reservation service.
	 * @throws NullPointerException if either service is null.
	 */
	public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
		this.ticketPaymentService = Objects.requireNonNull(ticketPaymentService, "Payment service cannot be null");
		this.seatReservationService = Objects.requireNonNull(seatReservationService,
				"Reservation service cannot be null");
	}

	/**
	 * Processes the purchase of tickets for a given account.
	 *
	 * @param accountId      The ID of the account making the purchase.
	 * @param ticketRequests The list of ticket type requests.
	 * @throws InvalidPurchaseException if input validation fails.
	 */
	@Override
	public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
			throws InvalidPurchaseException {

		// Validate the AccountId
		verifyAccount(accountId);

		// Validate the Ticket Request
		verifyTicketRequests(ticketTypeRequests);

		// Declare variables
		int totalTickets = 0;
		int totalCost = 0;// Total payment amount
		int totalSeats = 0; // Total seats required (without infants)
		int adultCount = 0;// Count of adult tickets
		int infantCount = 0; // Count of Infant tickets

		// Loop through each ticket request and calculate total cost and seat count
		for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
			// Validate Single ticket Request
			verifySingleTicketRequest(ticketTypeRequest);
			
			int ticketsPerRequest = ticketTypeRequest.getNoOfTickets();
			totalTickets += ticketsPerRequest;
			switch (ticketTypeRequest.getTicketType()) {
			case ADULT -> {
				adultCount += ticketsPerRequest;
				totalCost += ADULT_TICKET_PRICE * ticketsPerRequest;
				totalSeats += ticketsPerRequest;
			}
			case CHILD -> {
				totalSeats += ticketsPerRequest;
				totalCost += CHILD_TICKET_PRICE * ticketsPerRequest;
			}
			case INFANT -> {
				// Total seats required (without infants)
				infantCount += ticketsPerRequest;
			}
			}
		}

		// Verify Total Tickets
		verifyTotalTickets(totalTickets);

		// Verify Adult and Infant Count
		verifyAdultCount(adultCount, infantCount);

		// Process payment and seat reservation through external services
		ticketPaymentService.makePayment(accountId, totalCost);
		seatReservationService.reserveSeat(accountId, totalSeats);

	}

	/**
	 * Verify the given account ID is valid or not.
	 *
	 * @param accountId The account ID to validate.
	 * @throws InvalidPurchaseException if the account ID is invalid.
	 */
	private void verifyAccount(Long accountId) {
		if (accountId == null || accountId <= 0) {
			throw new InvalidPurchaseException("Account ID must be a positive number");
		}
	}

	/**
	 * Verify the adultTickets to ensure at least one adult ticket is purchased and
	 * also ensures one infant per adult.
	 *
	 * @param adultCount  Adult Count to validate.
	 * @param infantCount Infant Count to validate.
	 * @throws InvalidPurchaseException if the adult count is less than zero.
	 */
	private void verifyAdultCount(int adultCount, int infantCount) {
		if (adultCount == 0) {
			throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without an Adult ticket");
		}
		if (infantCount > adultCount) {
			throw new InvalidPurchaseException("Each infant must have an accompanying adult lap");
		}

	}

	/**
	 * verify the ticket requests.
	 *
	 * @param ticketRequests The array of ticket requests.
	 * @throws InvalidPurchaseException if the request is null or empty.
	 */
	private void verifyTicketRequests(TicketTypeRequest... ticketRequests) {

		if (ticketRequests == null || ticketRequests.length == 0) {
			throw new InvalidPurchaseException("Ticket request list cannot be null or empty");
		}
	}

	/**
	 * verify single ticket requests.
	 *
	 * @param ticketRequests The array of ticket requests.
	 * @throws InvalidPurchaseException if the request is null.
	 */
	private void verifySingleTicketRequest(TicketTypeRequest ticketRequest) {

		if (ticketRequest == null) {
			throw new InvalidPurchaseException("Individual ticket request cannot be null");
		}

	}

	/**
	 * verify the total ticket count.
	 *
	 * @param totalTickets Total tickets in the request.
	 * @throws InvalidPurchaseException if the request is invalid.
	 */
	private void verifyTotalTickets(int totalTickets) {

		if (totalTickets > MAXIMUM_TICKETS) {
			throw new InvalidPurchaseException(
					"You cannot purchase more than " + MAXIMUM_TICKETS + " tickets at a time");
		}
	}
}
