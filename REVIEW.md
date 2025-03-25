This review covers the initial implementation of the loan decision engine backend (`TICKET-101`)
and explains, how and why the fixes were implemented.

**What Was Good in the Original Implementation**
Before diving into the fixes and improvements, it’s important to acknowledge the strengths of the original
implementation. The codebase showed a solid foundation, particularly in the following areas:

- Separation of Concerns: The backend was clearly separated into layers, the decision logic was isolated in a service
  class (DecisionEngine), and request handling was done via a REST controller. This structure made it easier to
  understand and extend.
- Use of Constants: A dedicated constants file (DecisionEngineConstants) was already in place, showing awareness of
  maintainability and avoiding magic numbers.
- Domain-Specific Exceptions: Custom exception classes were defined for different validation failures, which is a clean
  and extensible approach. 
- Input Validation: There was input validation logic present for personal code, loan amount, and loan period, helping
  ensure API robustness from the start.

These provided a solid baseline and made the refactoring process much smoother. The improvements outlined below were
focused on aligning the implementation with the task requirements, simplifying the logic, and improving testability
and maintainability.

---

## Issues Identified in Original Code

### 1. Logic not implemented as intended

**Task description:**
The idea of the decision engine is to determine what would be the maximum sum, regardless of the
person requested loan amount. For example if a person applies for €4000,-, but we determine that
we would approve a larger sum then the result should be the maximum sum which we would
approve.
Also, in reverse, if a person applies for €4000,- and we would not approve it then we want to return
the largest sum which we would approve, for example €2500,-,. If a suitable loan amount is not
found within the selected period, the decision engine should also try to find a new suitable period.

**Problem:**  
The decision engine did not follow the assignment requirement to calculate the **maximum approvable loan**. Instead,
it only checked if the provided amount was approvable and increased the loan period when it wasn't — missing
the core algorithm.

**How I understood the Task:**
- So reading the task I understood that when a person for example applies for 4000 for 12 Months but is actually 
  eligible for 4500 then we would return a loan for 4500 for 12 Months.
- Now if the loan is not approved then we start decreasing the amount until the loan gets approved. For example applied 
  for 4000 for 12 Months but is only eligible for 3000 for 12 Months.
- Finally, when no loan amount is approved we start increasing the loan period and check if credit score becomes 
  high enough for loan approval.

---

### 2. Monolithic Method

**Problem:**  
All logic was implemented in a single large method (`calculateApprovedLoan`), making it hard to follow, test, or extend.

**Fix:**  
Refactored the logic into clean, helper methods:
- `handleApprovedRequest(...)`
- `handleRejectedRequest(...)`
- `tryFindValidAmount(...)`
- `calculateCreditScore(...)`

---

### 3. Code Style, Small Fixes & Maintainability Enhancements

**Problem:**
While a constants class (DecisionEngineConstants) was already in use for some shared values, many other hardcoded
numbers and repeated strings were scattered throughout the codebase. Additionally, POJOs like DecisionRequest and
DecisionResponse used manual getters/setters, and the exception classes were verbose and extended Throwable,
which is not the best practice in java.
These inconsistencies made the code harder to read, maintain, and scale.

**Fix:**
**Several small but impactful improvements were made to bring consistency and clarity across the codebase:**

- Most of the magic numbers and repeated strings (like "Invalid loan amount!" or 0.1) were extracted into constants
  in the DecisionEngineConstants file.
- Replaced verbose getter/setter code with Lombok’s @Data annotation in DTO classes (DecisionRequest, DecisionResponse)
  to reduce boilerplate.
- Refactored all custom exceptions (InvalidLoanAmountException, InvalidLoanPeriodException, etc.):
- Changed inheritance from Throwable to RuntimeException for better exception handling in Spring Boot.
- Added more tests and refactored some of the existing tests.
- Removed redundant message/cause field declarations and manual overrides of getMessage(), etc.
- Improved overall code style: ensured consistent formatting, added missing newlines at the end of classes, and
  maintained line length within recommended limits for better readability.

---

### 4. FrontEnd Code Review

**Continues in the frontend repo**
https://github.com/LValem/InBank-Frontend

---