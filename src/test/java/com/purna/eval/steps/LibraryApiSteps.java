package com.purna.eval.steps;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.purna.eval.model.Book;
import com.purna.eval.support.LibraryApiUtils;
import com.purna.eval.support.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class LibraryApiSteps {

    @Autowired
    LibraryApiUtils libraryApiUtils;

    @Autowired
    ScenarioContext sContext;

    private final ObjectMapper mapper = new ObjectMapper();

    @Given("the user obtains a valid authorisation token upon successful login")
    public void validateUserLoginAndToken() {
        String authToken = libraryApiUtils.getAuthToken();
        DecodedJWT decodedJWT = JWT.decode(authToken);
        Date expiry = decodedJWT.getExpiresAt();
        log.info("User auth token is valid till {}", expiry);
        // Verify that token expiry is in the future and it is valid for 60 minutes
        assertThat(expiry).isAfter(new Date());
        assertThat(expiry.toInstant().minusSeconds(3600)).isEqualTo(decodedJWT.getIssuedAtAsInstant());
    }

    @When("the user searches for book titles by author {string}")
    public void requestSearchBooksByAuthor(String authorName) {
        Response response = libraryApiUtils.searchBooksByAuthor(authorName);
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
        sContext.setAuthor(authorName);
    }

    @Then("API should return list of books of above author with their availability")
    public void validateSearchBooksByAuthor() {
        Book[] bookList;
        try {
            bookList = mapper.readValue(sContext.getResponse().asString(), Book[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("API response structure is not as expected" + e);
        }
        Book bookToBorrow = Arrays.stream(bookList).filter(b -> b.getAvailableCopies() > 0).findAny().orElse(null);
        if (bookToBorrow == null) {
            throw new RuntimeException("Titles of author " + sContext.getAuthor() + " are not available to borrow");
        }
        sContext.setBookTitle(bookToBorrow.getTitle());
        sContext.setAvailCopies(bookToBorrow.getAvailableCopies());
        log.info("Selected book title is {} with available copies of {}",
                bookToBorrow.getTitle(), bookToBorrow.getAvailableCopies());
    }

    @When("^the user borrows .* with valid auth token$")
    public void requestBorrowBook() {
        Response response = libraryApiUtils.borrowBook(sContext.getBookTitle(), sContext.getUsername());
        assertThat(response.getStatusCode()).isIn(201, 400);
        sContext.setResponse(response);
    }

    @Then("API should confirm the borrowing with the details including due date")
    public void validateBookBorrowResponse() {
        String borrowResp = sContext.getResponse().asString();
        String message = JsonPath.read(borrowResp, "$.message");
        HashMap<String, String> borrowTxn = JsonPath.read(borrowResp, "$.newTransactionJson");
        assertThat(message).isEqualTo("Book borrowed successfully");
        assertThat(borrowTxn.get("username")).isEqualTo(sContext.getUsername());
        assertThat(borrowTxn.get("title")).isEqualTo(sContext.getBookTitle());
        try {
            Date dueDate = DateUtils.parseDateStrictly(borrowTxn.get("dueDate"), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            assertThat(dueDate).isAfter(new Date());
            sContext.setDueDate(borrowTxn.get("dueDate"));
            sContext.setBorrowDate(borrowTxn.get("borrowDate"));
        } catch (ParseException e) {
            throw new RuntimeException("Due date format in borrow book response is not as expected");
        }
    }

    @When("the user searches the borrowed book by title")
    public void requestSearchBookByTitle() {
        Response response = libraryApiUtils.searchBooksByTitle(sContext.getBookTitle());
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
    }

    @Then("API should return the book details with reduced number of available copies")
    public void validateSearchByTileResponse() {
        String responseStr = sContext.getResponse().asString();
        HashMap<String, Object> bookObj = JsonPath.read(responseStr, "$.[0]");
        assertThat(bookObj.get("title")).isEqualTo(sContext.getBookTitle());
        assertThat(bookObj.get("author")).isEqualTo(sContext.getAuthor());
        int availCopies = (int) bookObj.get("availableCopies");
        assertThat(availCopies).isLessThan(sContext.getAvailCopies());
        sContext.setAvailCopies(availCopies);
        log.info("Number of available copies after borrowing: {}", availCopies);
    }

    @When("the user returns the above book with valid auth token")
    public void requestReturnBook() {
        Response response = libraryApiUtils.returnBook(sContext.getBookTitle(), sContext.getUsername());
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
    }

    @Then("API should confirm the return with the details including return date")
    public void validateBookReturnResponse() {
        String retResponse = sContext.getResponse().asString();
        String message = JsonPath.read(retResponse, "$.message");
        HashMap<String, String> borrowTxn = JsonPath.read(retResponse, "$.transactionJson");
        assertThat(message).isEqualTo("Book returned successfully");
        assertThat(borrowTxn.get("username")).isEqualTo(sContext.getUsername());
        assertThat(borrowTxn.get("title")).isEqualTo(sContext.getBookTitle());
        assertThat(borrowTxn.get("dueDate")).isEqualTo(sContext.getDueDate());
        assertThat(borrowTxn.get("borrowDate")).isEqualTo(sContext.getBorrowDate());
        try {
            Date returnDate = DateUtils.parseDateStrictly(borrowTxn.get("returnDate"),
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            assertThat(returnDate).isInSameDayAs(new Date());
            sContext.setReturnDate(borrowTxn.get("returnDate"));
        } catch (ParseException e) {
            throw new RuntimeException("Return date format in return book response is not as expected");
        }
    }

    @When("the user searches the borrowed book's availability by title")
    public void apiRequestBookAvailabilityByTitle() {
        Response response = libraryApiUtils.searchBookAvailByTitle(sContext.getBookTitle());
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
    }

    @Then("API should return the book details with increased number of available copies")
    public void validateSearchAvailabilityByTileResponse() {
        String responseStr = sContext.getResponse().asString();
        HashMap<String, Object> bookObj = JsonPath.read(responseStr, "$");
        assertThat(bookObj.get("title")).isEqualTo(sContext.getBookTitle());
        int availCopies = (int) bookObj.get("availableCopies");
        assertThat(availCopies).isGreaterThan(sContext.getAvailCopies());
        sContext.setAvailCopies(availCopies);
        log.info("Number of available copies after return: {}", availCopies);
    }

    @When("the user views their borrowing history with valid auth token")
    public void apiRequestBorrowingHistory() {
        Response response = libraryApiUtils.borrowingHistory(sContext.getUsername());
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
    }

    @Then("API should return the borrowing details including dates of above book as one of the history items")
    public void validateBorrowingHistoryResponse() {
        String responseStr = sContext.getResponse().asString();
        String jpString = "$.[?(@.borrowDate=='" + sContext.getBorrowDate() + "')]";
        List<HashMap<String, Object>> histItems = JsonPath.read(responseStr, jpString);
        HashMap<String, Object> histItem = histItems.getFirst();
        assertThat(histItem.get("username")).isEqualTo(sContext.getUsername());
        assertThat(histItem.get("title")).isEqualTo(sContext.getBookTitle());
        assertThat(histItem.get("dueDate")).isEqualTo(sContext.getDueDate());
        assertThat(histItem.get("borrowDate")).isEqualTo(sContext.getBorrowDate());
        assertThat(histItem.get("returnDate")).isEqualTo(sContext.getReturnDate());
    }

    @When("the user searches for all book titles")
    public void requestSearchAllBooks() {
        Response response = libraryApiUtils.searchAllBooks();
        assertThat(response.getStatusCode()).isEqualTo(200);
        sContext.setResponse(response);
    }

    @Then("the user selects a book that has no available copies")
    public void selectBookWithZeroAvailability() {
        Book[] bookList;
        try {
            bookList = mapper.readValue(sContext.getResponse().asString(), Book[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("API response structure is not as expected" + e);
        }
        Book selectBook = Arrays.stream(bookList).filter(b -> b.getAvailableCopies() == 0).findAny().orElse(null);
        if (selectBook == null) {
            throw new RuntimeException("No books present with zero availability");
        }
        sContext.setBookTitle(selectBook.getTitle());
    }

    @Then("API should return an error message to the caller")
    public void validateErrorResponse() {
        String responseStr = sContext.getResponse().asString();
        String message = JsonPath.read(responseStr, "$.error");
        assertThat(message).isEqualTo("Book not available");
    }

    @Then("API response of borrowing history should not include above book")
    public void validateBorrowingHistoryNegativeResponse() {
        String responseStr = sContext.getResponse().asString();
        String jpString = "$.[?(@.title=='" + sContext.getBookTitle() + "')]";
        List<HashMap<String, Object>> histItems = JsonPath.read(responseStr, jpString);
        assertThat(histItems).isEmpty();
    }

}
