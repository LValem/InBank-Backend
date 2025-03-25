package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    @InjectMocks
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12);
        assertEquals(2000, decision.getLoanAmount());
        assertEquals(20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12);

        assertEquals(10000, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";

        Decision decision = decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooLow() {
        Long tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooHigh() {
        Long tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooShort() {
        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L,
                tooShortLoanPeriod);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooLong() {
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L,
                tooLongLoanPeriod);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        NoValidLoanException exception = assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 12));

        assertEquals(DecisionEngineConstants.APPLICANT_HAS_DEBT, exception.getMessage());
    }
}
