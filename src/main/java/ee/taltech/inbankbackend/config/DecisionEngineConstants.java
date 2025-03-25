package ee.taltech.inbankbackend.config;

/**
 * Holds all necessary constants for the decision engine.
 */
public class DecisionEngineConstants {
    public static final Integer MINIMUM_LOAN_AMOUNT = 2000;
    public static final Integer MAXIMUM_LOAN_AMOUNT = 10000;
    public static final Integer MAXIMUM_LOAN_PERIOD = 48;
    public static final Integer MINIMUM_LOAN_PERIOD = 12;
    public static final Integer SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final Integer SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final Integer SEGMENT_3_CREDIT_MODIFIER = 1000;
    public static final int CHANGE_AMOUNT_BY_100 = 100;
    public static final int CEILING_OF_FIRST_SEGMENT = 2500;
    public static final int CEILING_OF_SECOND_SEGMENT = 5000;
    public static final double BASELINE_FOR_LOAN_APPROVAL = 0.1;
    public static final int CEILING_OF_THIRD_SEGMENT = 7500;
    public static final String INVALID_PERSONAL_ID_CODE = "Invalid personal ID code!";
    public static final String INVALID_LOAN_AMOUNT = "Invalid loan amount!";
    public static final String INVALID_LOAN_PERIOD = "Invalid loan period!";
    public static final String APPLICANT_HAS_DEBT = "Applicant has debt!";
    public static final String AN_UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred";
    public static final String NO_VALID_LOAN_FOUND_FOR_THE_PROVIDED_PARAMETERS = "No valid loan found for the" +
            " provided parameters.";
    public static final String INVALID_AGE_ERROR = "Age doesn't match requirements for this country!";
    public static final double SCORE_DIVISOR = 10.0;
    public static final int MINIMUM_AGE = 21;
    public static final int ESTONIA_MAXIMUM = 80;
    public static final int LATVIA_MAXIMUM = 75;
    public static final int LITHUANIA_MAXIMUM = 85;
}
