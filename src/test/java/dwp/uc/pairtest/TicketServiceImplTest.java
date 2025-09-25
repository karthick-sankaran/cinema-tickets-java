package dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TicketServiceImpl}.
 * <p>
 * Uses Mockito to verify interactions with external services
 * (TicketPaymentService and SeatReservationService).
 */
class TicketServiceImplTest {

	private TicketPaymentService paymentService;
	private SeatReservationService seatService;
	private TicketServiceImpl ticketService;

	/**
	 * Sets up the mocks and creates a new TicketServiceImpl instance before each
	 * test runs.
	 */
	@BeforeEach
	void setUp() {
		paymentService = Mockito.mock(TicketPaymentService.class);
		seatService = Mockito.mock(SeatReservationService.class);
		ticketService = new TicketServiceImpl(paymentService, seatService);
	}

	/**
	 * Verifies that purchasing only adult tickets with a valid account ID succeeds
	 * and triggers correct payment and seat reservation calls.
	 */
	@Test
	void purchaseTickets_adultOnly() {
		TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

		assertDoesNotThrow(() -> ticketService.purchaseTickets(1L, adultTicketRequest));

		verify(paymentService).makePayment(1L, 2 * 25);
		verify(seatService).reserveSeat(1L, 2);
	}

	/**
	 * Verifies that a valid mix of adult, child, and infant tickets calculates the
	 * correct cost and reserves the correct number of seats.
	 */
	@Test
	void purchaseTickets_adultChildInfant() {
		TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
		TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
		TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

		ticketService.purchaseTickets(99L, adultTicketRequest, childTicketRequest, infantTicketRequest);

		verify(paymentService).makePayment(99L, (1 * 25) + (2 * 15));
		verify(seatService).reserveSeat(99L, 1 + 2);
	}

	/**
	 * Verifies that attempting to purchase only child tickets (no adult) results in an InvalidPurchaseException.
	 */
	@Test
	void purchaseTickets_childOnly_shouldFail() {
		TicketTypeRequest childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, childTicketRequest));
	}

	/**
	 * Verifies that purchasing more infants than adults violates the "one infant per adult" rule and fails.
	 */
	@Test
	void purchaseTickets_moreInfantsThanAdults_shouldFail() {
		TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
		TicketTypeRequest infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(10L, adultTicketRequest, infantTicketRequest));
	}

	/**
	 * Verifies that attempting to purchase more than 25 tickets in a single
	 * transaction fails with an InvalidPurchaseException.
	 */
	@Test
	void purchaseTickets_moreThan25Tickets_shouldFail() {
		TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, adultTicketRequest));
	}

	/**
	 * Verifies that a null account ID is considered invalid and causes the purchase
	 * to fail.
	 */
	@Test
	void purchaseTickets_invalidAccount_shouldFail() {
		TicketTypeRequest adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(null, adultTicketRequest));
	}

	/**
	 * Verifies that providing no ticket requests at all results in an
	 * InvalidPurchaseException.
	 */
	@Test
	void purchaseTickets_emptyTicketRequests_shouldFail() {
		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L));
	}

	/**
	 * Verifies that passing a null element inside the array of ticket requests
	 * results in an InvalidPurchaseException.
	 */
	@Test
	void purchaseTickets_nullTicketRequest_shouldFail() {
		assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, (TicketTypeRequest) null));
	}
}
