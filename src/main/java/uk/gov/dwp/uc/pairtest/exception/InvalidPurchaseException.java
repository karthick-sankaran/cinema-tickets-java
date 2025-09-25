package uk.gov.dwp.uc.pairtest.exception;

@SuppressWarnings("serial")
public class InvalidPurchaseException extends RuntimeException {
	public InvalidPurchaseException(String message) {
		super(message);
	}
}
