package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;
import java.time.Period;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 48 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount   Requested loan amount
     * @param loanPeriod   Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod, String country)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException {

        try {
            verifyInputs(personalCode, loanAmount, loanPeriod, country);
        } catch (Exception e) {
            return new Decision(null, null, e.getMessage());
        }

        creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException(DecisionEngineConstants.APPLICANT_HAS_DEBT);
        }

        double initialScore = calculateCreditScore(loanAmount.intValue(), loanPeriod);

        if (initialScore >= DecisionEngineConstants.BASELINE_FOR_LOAN_APPROVAL) {
            return handleApprovedRequest(loanAmount.intValue(), loanPeriod);
        } else {
            return handleRejectedRequest(loanAmount.intValue(), loanPeriod);
        }
    }

    /**
     * Handles the case where the initial loan request is approved.
     * Tries increasing the loan amount (by increments of 100) until the score drops below the approval baseline.
     * Returns the highest approved amount for the given loan period.
     *
     * @param startingAmount The initially approved loan amount
     * @param period         The requested loan period
     * @return Decision object with the maximum approvable loan amount for the given period
     */
    private Decision handleApprovedRequest(int startingAmount, int period) {
        for (int amount = startingAmount + DecisionEngineConstants.CHANGE_AMOUNT_BY_100;
             amount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT;
             amount += DecisionEngineConstants.CHANGE_AMOUNT_BY_100) {

            double score = calculateCreditScore(amount, period);
            if (score < DecisionEngineConstants.BASELINE_FOR_LOAN_APPROVAL) {
                // last approvable loan for given period.
                return new Decision(amount - DecisionEngineConstants.CHANGE_AMOUNT_BY_100,
                        period, null);
            }
        }
        // if ceiling was not found then return the max amount.
        return new Decision(DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT, period, null);
    }


    /**
     * Handles the case where the initial request is rejected.
     * Tries decreasing the loan amount until a valid one is found.
     * If none is found, the method tries increasing the loan period and repeating the process.
     *
     * @param startingAmount Initial requested amount
     * @param startingPeriod Initial requested period
     * @return A Decision object if a valid loan is found
     * @throws NoValidLoanException If no loan can be approved even after fallback attempts
     */
    private Decision handleRejectedRequest(int startingAmount, int startingPeriod) throws NoValidLoanException {
        // Try decreasing amount with original period
        Decision result = tryFindValidAmount(startingPeriod,
                startingAmount - DecisionEngineConstants.CHANGE_AMOUNT_BY_100);
        if (result != null) return result;

        // Try longer periods with max amount
        for (int period = startingPeriod + 1; period <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD; period++) {
            result = tryFindValidAmount(period, DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT);
            if (result != null) return result;
        }

        // Cannot be tested at the moment because of constraints and hard coded credit scores.
        throw new NoValidLoanException(DecisionEngineConstants.NO_VALID_LOAN_FOUND_FOR_THE_PROVIDED_PARAMETERS);
    }

    /**
     * Tries to find the highest valid loan amount for a given period.
     * Starts from the given amount and decreases by 100 until the minimum loan amount is reached.
     * Returns the first amount that passes the approval score.
     *
     * @param period      Loan period in months
     * @param startAmount Starting amount to evaluate
     * @return Decision object if a valid loan is found, otherwise null
     */
    private Decision tryFindValidAmount(int period, int startAmount) {
        for (int amount = Math.min(startAmount, DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT);
             amount >= DecisionEngineConstants.MINIMUM_LOAN_AMOUNT;
             amount -= DecisionEngineConstants.CHANGE_AMOUNT_BY_100) {

            double score = calculateCreditScore(amount, period);
            if (score >= DecisionEngineConstants.BASELINE_FOR_LOAN_APPROVAL) {
                return new Decision(amount, period, null);
            }
        }
        return null;
    }

    /**
     * Calculates the credit score based on the given loan amount and period.
     * Formula: ((creditModifier / loanAmount) * loanPeriod) / 10
     *
     * @param loanAmount Loan amount to be evaluated
     * @param loanPeriod Loan period in months
     * @return The credit score as a decimal number
     * */
    private double calculateCreditScore(int loanAmount, int loanPeriod) {
        return ((double) creditModifier / loanAmount) * loanPeriod / DecisionEngineConstants.SCORE_DIVISOR;
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < DecisionEngineConstants.CEILING_OF_FIRST_SEGMENT) {
            return 0;
        } else if (segment < DecisionEngineConstants.CEILING_OF_SECOND_SEGMENT) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < DecisionEngineConstants.CEILING_OF_THIRD_SEGMENT) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount   Requested loan amount
     * @param loanPeriod   Requested loan period
     * @param country
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException   If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException   If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod, String country)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            PersonalCodeException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE);
        }
        if (!checkAge(personalCode, country)) {
            throw new InvalidAgeException(DecisionEngineConstants.INVALID_AGE_ERROR);
        }

        if (!(DecisionEngineConstants.MINIMUM_LOAN_AMOUNT <= loanAmount)
                || !(loanAmount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException(DecisionEngineConstants.INVALID_LOAN_AMOUNT);
        }
        if (!(DecisionEngineConstants.MINIMUM_LOAN_PERIOD <= loanPeriod)
                || !(loanPeriod <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException(DecisionEngineConstants.INVALID_LOAN_PERIOD);
        }
    }

    /**
     * Checks if the applicant's age is valid based on their personal code.
     * The age must be at least the minimum required age (21), and not older than the country-specific maximum:
     * - Estonia: 80
     * - Latvia: 75
     * - Lithuania: 85
     *
     * @param personalCode The user's personal ID code.
     * @param country The selected country ("estonia", "latvia", or "lithuania").
     * @return true if the age is within the valid range, false otherwise.
     * @throws PersonalCodeException If the personal code is invalid.
     */
    private boolean checkAge(String personalCode, String country) throws PersonalCodeException {
        try {
            Period age = parser.getAge(personalCode);
            int years = age.getYears();

            int maxAge;

            switch (country.toLowerCase()) {
                case "estonia":
                    maxAge = DecisionEngineConstants.ESTONIA_MAXIMUM;
                    break;
                case "latvia":
                    maxAge = DecisionEngineConstants.LATVIA_MAXIMUM;
                    break;
                case "lithuania":
                    maxAge = DecisionEngineConstants.LITHUANIA_MAXIMUM;
                    break;
                default:
                    return false;
            }

            return years >= DecisionEngineConstants.MINIMUM_AGE && years <= maxAge;

        } catch (PersonalCodeException e) {
            throw new PersonalCodeException(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE);
        }
    }
}
