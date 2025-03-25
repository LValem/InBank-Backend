package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when the requested loan amount is invalid.
 */
public class InvalidLoanAmountException extends RuntimeException {

    public InvalidLoanAmountException(String message) {
        super(message);
    }
}
