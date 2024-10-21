Feature: Search book by an author, borrow, return and view borrowing history using Library Management System API

  @api
  Scenario: User searches for a book by author, borrows, returns it and views their borrowing history
    Given the user obtains a valid authorisation token upon successful login
    When the user searches for book titles by author 'Jan & Stan Berenstain'
    Then API should return list of books of above author with their availability
    When the user borrows one of the available books with valid auth token
    Then API should confirm the borrowing with the details including due date
    When the user searches the borrowed book by title
    Then API should return the book details with reduced number of available copies
    When the user returns the above book with valid auth token
    Then API should confirm the return with the details including return date
    When the user searches the borrowed book's availability by title
    Then API should return the book details with increased number of available copies
    When the user views their borrowing history with valid auth token
    Then API should return the borrowing details including dates of above book as one of the history items
