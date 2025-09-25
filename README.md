# cinema-tickets-java
# Objective
The service:
- Calculates the total cost of a ticket purchase.
- Reserves the correct number of seats.
- Enforces all business rules and constraints described below.
---
## Business Rules

| Ticket Type | Price | Seat Required |
|-------------|-------|---------------|
| **Adult**   | £25  | Yes |
| **Child**   | £15  | Yes |
| **Infant**  | £0   | No (sits on an adult’s lap) |

- Maximum **25 tickets** can be purchased in one transaction.
- **At least one Adult** ticket is required if Child or Infant tickets are purchased.
- **One Infant per Adult** (each infant must have an accompanying adult lap).
- Payment is processed via the provided `TicketPaymentService`.
- Seat reservations are processed via the provided `SeatReservationService`.
---
## Key Classes

- **`TicketServiceImpl`**  
  Implements `TicketService` and contains all business logic:
  - Validates input (account ID, ticket requests).
  - Ensures business rules (adult requirement, max tickets, one infant per adult).
  - Calculates total payment and seats.
  - Calls external services for payment and seat reservation.

- **`TicketTypeRequest`**  
  Immutable object representing a ticket request for a given type and quantity.

- **`InvalidPurchaseException`**  
  Custom runtime exception thrown when validation fails.
---
## Design Decisions

- **Immutability:**  
  `TicketTypeRequest` is a `final` class with `final` fields and no setters.

- **Validation First:**  
  Account and ticket requests are validated before any payment or reservation calls.

- **Single Responsibility:**  
  Helper methods like `verifyAccount`, `verifyAdultCount` keep logic clean and readable.

- **External Services:**  
  `TicketPaymentService` and `SeatReservationService` are external and assumed reliable.  
  They are used as black boxes with no internal modification.

---
## Test Coverage Highlights

| Scenario | Expected Result |
|----------|-----------------|
| Adult-only purchase | ✅ Success |
| Adult + Child + Infant | ✅ Correct cost and seats |
| Child-only purchase | ❌ Fails |
| More infants than adults | ❌ Fails |
| >25 tickets | ❌ Fails |
| Null/empty requests | ❌ Fails |
| Invalid account ID | ❌ Fails |




---
