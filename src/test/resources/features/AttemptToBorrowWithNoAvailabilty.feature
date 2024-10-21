Feature: Search all books and unsuccessful borrowing attempt of a book that is not available

  @api @error
  Scenario: User searches for all books and attempts to borrow a book with no availability
    Given the user obtains a valid authorisation token upon successful login
    When the user searches for all book titles
    Then the user selects a book that has no available copies
    When the user borrows above unavailable book with valid auth token
    Then API should return an error message to the caller
    When the user views their borrowing history with valid auth token
    Then API response of borrowing history should not include above book
