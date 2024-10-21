package com.purna.eval.support;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;
import com.purna.eval.config.Props;
import com.purna.eval.config.SysPropNames;
import com.purna.eval.utils.RestUtils;
import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Component
@ScenarioScope
public class LibraryApiUtils {

    @Autowired
    Props props;

    @Autowired
    RestUtils restUtils;

    @Autowired
    ScenarioContext sContext;

    private String authToken;

    public String getAuthToken() {
        if (authToken == null) {
            logInForNewToken();
        } else {
            DecodedJWT decodedJWT = JWT.decode(authToken);
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt.before(new Date())) {
                log.info("User auth token has expired at {}. Getting new token", expiresAt);
                logInForNewToken();
            }
        }
        return authToken;
    }

    public Response searchBooksByAuthor(String authorName) {
        String uri = getUri(props.getProperty("searchByAuthorPath")) + authorName;
        return restUtils.sendGetRequest(uri, getHeaders());
    }

    public Response searchBooksByTitle(String bookTitle) {
        String uri = getUri(props.getProperty("searchByTitlePath")) + bookTitle;
        return restUtils.sendGetRequest(uri, getHeaders());
    }

    public Response searchBookAvailByTitle(String bookTitle) {
        String uri = getUri(props.getProperty("availByTitlePath"));
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("title", bookTitle);
        return restUtils.sendGetRequest(uri, getHeaders(), queryParams);
    }

    public Response searchAllBooks() {
        String uri = getUri(props.getProperty("searchAllPath"));
        return restUtils.sendGetRequest(uri, getHeaders());
    }

    public Response borrowBook(String bookTitle, String username) {
        String uri = getUri(props.getProperty("borrowPath"));
        JSONObject payload = getBorrowRetPayload(bookTitle, username);
        return restUtils.sendPostRequest(uri, getHeadersWithAuth(), payload);
    }

    public Response returnBook(String bookTitle, String username) {
        String uri = getUri(props.getProperty("returnPath"));
        JSONObject payload = getBorrowRetPayload(bookTitle, username);
        return restUtils.sendPostRequest(uri, getHeadersWithAuth(), payload);
    }

    public Response borrowingHistory(String username) {
        String uri = getUri(props.getProperty("borrowHistoryPath"));
        JSONObject payload = getBorrowRetPayload("", username);
        payload.remove("title");
        return restUtils.sendPostRequest(uri, getHeadersWithAuth(), payload);
    }

    private void logInForNewToken() {
        String uri = getUri(props.getProperty("loginPath"));
        String username = props.getProperty("username");
        if (StringUtils.isNotBlank(System.getProperty(SysPropNames.USERNAME.label))) {
            username = System.getProperty(SysPropNames.USERNAME.label);
        }
        String password = props.getProperty("password");
        if (StringUtils.isNotBlank(System.getProperty(SysPropNames.PASSWORD.label))) {
            password = System.getProperty(SysPropNames.PASSWORD.label);
        }
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("password", password);
        Response response = restUtils.sendPostRequest(uri, getHeaders(), payload);
        assertThat(response.getStatusCode()).isEqualTo(200);
        authToken = JsonPath.read(response.asString(), "$.token");
        sContext.setUsername(username);
    }

    private String getUri(String apiPath) {
        return props.getProperty("apiHost") + apiPath;
    }

    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private HashMap<String, String> getHeadersWithAuth() {
        HashMap<String, String> headers = getHeaders();
        String authToken = getAuthToken();
        headers.put("Authorization", "Bearer " + authToken);
        return headers;
    }

    private JSONObject getBorrowRetPayload(String bookTitle, String username) {
        JSONObject payload = new JSONObject();
        payload.put("title", bookTitle);
        payload.put("username", username);
        return payload;
    }

}
