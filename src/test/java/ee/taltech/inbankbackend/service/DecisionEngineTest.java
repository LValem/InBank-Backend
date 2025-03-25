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
    private String estonia;
    private String latvia;
    private String lithuania;
    private String underagePersonalCode;
    private String overagePersonalCodeEstonia;
    private String overagePersonalCodeLatvia;
    private String overagePersonalCodeLithuania;
    @BeforeEach
    void setUp() {
        debtorPersonalCode = "37605030299";
        segment1PersonalCode = "50307172740";
        segment2PersonalCode = "38411266610";
        segment3PersonalCode = "35006069515";
        estonia = "estonia";
        latvia = "latvia";
        lithuania = "lithuania";
        underagePersonalCode = "50802085080";
        overagePersonalCodeEstonia = "34403255473";
        overagePersonalCodeLatvia = "34903254088";
        overagePersonalCodeLithuania = "33903255845";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 4000L, 12, estonia));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L, 12,
                estonia);
        assertEquals(2000, decision.getLoanAmount());
        assertEquals(20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 4000L, 12,
                estonia);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, 4000L, 12,
                estonia);

        assertEquals(10000, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";

        Decision decision = decisionEngine.calculateApprovedLoan(invalidPersonalCode, 4000L, 12,
                latvia);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooLow() {
        Long tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount, 12,
                lithuania);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooHigh() {
        Long tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount, 12,
                estonia);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooShort() {
        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L,
                tooShortLoanPeriod, latvia);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooLong() {
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, 4000L,
                tooLongLoanPeriod, lithuania);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, 2000L, 12,
                estonia);
        assertEquals(3600, decision.getLoanAmount());
        assertEquals(12, decision.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        NoValidLoanException exception = assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, 10000L, 12,
                        estonia));

        assertEquals(DecisionEngineConstants.APPLICANT_HAS_DEBT, exception.getMessage());
    }

    @Test
    void testValidLoanFoundInEstonia() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                2000L, 12, estonia);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testValidLoanFoundInLatvia() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                2000L, 12, latvia);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testValidLoanFoundInLithuania() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                2000L, 12, lithuania);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testUnderageApplicant() {
        Decision decision = decisionEngine.calculateApprovedLoan(underagePersonalCode,
                4000L, 12, estonia);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantEstonia() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeEstonia,
                4000L, 12, estonia);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantLatvia() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLatvia,
                4000L, 12, latvia);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantLithuania() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLithuania,
                4000L, 12, lithuania);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testNotValidCountry() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLithuania,
                4000L, 12, "Finland");
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }
}
