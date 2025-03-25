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

    private static final long LOAN_AMOUNT_4000 = 4000L;
    private static final int LOAN_PERIOD_12 = 12;
    private static final long LOAN_AMOUNT_2000 = 2000L;
    private static final long LOAN_AMOUNT_10000 = 10000L;
    private static final int EXPECTED_3600 = 3600;
    private static final int EXPECTED_2000 = 2000;
    private static final int EXPECTED_PERIOD_20 = 20;
    private static final int EXPECTED_PERIOD_12 = 12;
    private static final int EXPECTED_10000 = 10000;
    @InjectMocks
    private DecisionEngine decisionEngine;
    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;
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
        underagePersonalCode = "50802085080";
        overagePersonalCodeEstonia = "34403255473";
        overagePersonalCodeLatvia = "34903254088";
        overagePersonalCodeLithuania = "33903255845";
    }

    @Test
    void testDebtorPersonalCode() {
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, LOAN_AMOUNT_4000, LOAN_PERIOD_12,
                        Countries.ESTONIA));
    }

    @Test
    void testSegment1PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, LOAN_AMOUNT_4000,
                LOAN_PERIOD_12, Countries.ESTONIA);
        assertEquals(EXPECTED_2000, decision.getLoanAmount());
        assertEquals(EXPECTED_PERIOD_20, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, LOAN_AMOUNT_4000,
                LOAN_PERIOD_12, Countries.ESTONIA);
        assertEquals(EXPECTED_3600, decision.getLoanAmount());
        assertEquals(LOAN_PERIOD_12, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode, LOAN_AMOUNT_4000,
                LOAN_PERIOD_12, Countries.ESTONIA);

        assertEquals(EXPECTED_10000, decision.getLoanAmount());
        assertEquals(EXPECTED_PERIOD_12, decision.getLoanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        String invalidPersonalCode = "12345678901";

        Decision decision = decisionEngine.calculateApprovedLoan(invalidPersonalCode, LOAN_AMOUNT_4000,
                LOAN_PERIOD_12, Countries.LATVIA);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooLow() {
        Long tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooLowLoanAmount,
                LOAN_PERIOD_12, Countries.LITHUANIA);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanAmountTooHigh() {
        Long tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1L;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, tooHighLoanAmount,
                LOAN_PERIOD_12, Countries.ESTONIA);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_AMOUNT, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooShort() {
        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, LOAN_AMOUNT_4000,
                tooShortLoanPeriod, Countries.LATVIA);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testInvalidLoanPeriodTooLong() {
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        Decision decision = decisionEngine.calculateApprovedLoan(segment1PersonalCode, LOAN_AMOUNT_4000,
                tooLongLoanPeriod, Countries.LITHUANIA);
        assertEquals(DecisionEngineConstants.INVALID_LOAN_PERIOD, decision.getErrorMessage());
    }

    @Test
    void testFindSuitableLoanPeriod() throws InvalidLoanPeriodException, NoValidLoanException,
            InvalidPersonalCodeException, InvalidLoanAmountException {
        Decision decision = decisionEngine.calculateApprovedLoan(segment2PersonalCode, LOAN_AMOUNT_2000,
                LOAN_PERIOD_12, Countries.ESTONIA);
        assertEquals(EXPECTED_3600, decision.getLoanAmount());
        assertEquals(EXPECTED_PERIOD_12, decision.getLoanPeriod());
    }

    @Test
    void testNoValidLoanFound() {
        NoValidLoanException exception = assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(debtorPersonalCode, LOAN_AMOUNT_10000,
                        LOAN_PERIOD_12, Countries.ESTONIA));

        assertEquals(DecisionEngineConstants.APPLICANT_HAS_DEBT, exception.getMessage());
    }

    @Test
    void testValidLoanFoundInEstonia() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                LOAN_AMOUNT_2000, LOAN_PERIOD_12, Countries.ESTONIA);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testValidLoanFoundInLatvia() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                LOAN_AMOUNT_2000, LOAN_PERIOD_12, Countries.LATVIA);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testValidLoanFoundInLithuania() {
        Decision decision = decisionEngine.calculateApprovedLoan(segment3PersonalCode,
                LOAN_AMOUNT_2000, LOAN_PERIOD_12, Countries.LITHUANIA);

        assertNotNull(decision.getLoanAmount());
        assertNotNull(decision.getLoanPeriod());
        assertNull(decision.getErrorMessage());
    }

    @Test
    void testUnderageApplicant() {
        Decision decision = decisionEngine.calculateApprovedLoan(underagePersonalCode,
                LOAN_AMOUNT_4000, LOAN_PERIOD_12, Countries.ESTONIA);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantEstonia() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeEstonia,
                LOAN_AMOUNT_4000, LOAN_PERIOD_12, Countries.ESTONIA);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantLatvia() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLatvia,
                LOAN_AMOUNT_4000, LOAN_PERIOD_12, Countries.LATVIA);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testOverageApplicantLithuania() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLithuania,
                LOAN_AMOUNT_4000, LOAN_PERIOD_12, Countries.LITHUANIA);

        assertNull(decision.getLoanAmount());
        assertNull(decision.getLoanPeriod());
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }

    @Test
    void testNotValidCountry() {
        Decision decision = decisionEngine.calculateApprovedLoan(overagePersonalCodeLithuania,
                LOAN_AMOUNT_4000, LOAN_PERIOD_12, Countries.FINLAND);
        assertEquals(DecisionEngineConstants.INVALID_AGE_ERROR, decision.getErrorMessage());
    }
}
