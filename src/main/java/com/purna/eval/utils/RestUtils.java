package com.purna.eval.utils;

import com.purna.eval.config.SysPropNames;
import io.cucumber.spring.ScenarioScope;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

import static io.restassured.RestAssured.given;

@Slf4j
@Component
public class RestUtils implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        init();
    }

    public Response sendGetRequest(String uri, HashMap<String, String> headers) {
        return given().
                headers(headers).
                when().
                get(uri).
                then().
                extract().response();
    }

    public Response sendGetRequest(String uri, HashMap<String, String> headers, HashMap<String, String> queryParams) {
        return given().
                headers(headers).
                queryParams(queryParams).
                when().
                get(uri).
                then().
                extract().response();
    }

    public Response sendPostRequest(String uri, HashMap<String, String> headers, Object body) {
        return given().
                headers(headers).
                body(body).
                when().
                post(uri).
                then().
                extract().response();
    }

    private void init() {
        String logApiProp = System.getProperty(SysPropNames.LOG_API_CALLS.label);
        if (StringUtils.isBlank(logApiProp) || !StringUtils.equals(logApiProp.toLowerCase(), "false")) {
            String logFileName = System.getProperty("user.dir") + File.separator +
                    "target" + File.separator + "api-request-response.log";
            OutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(logFileName);
            } catch (FileNotFoundException e) {
                log.warn("API request logging is not possible");
            }
            assert fileOutputStream != null;
            PrintStream stream = new PrintStream(fileOutputStream, true);
            RestAssured.filters(RequestLoggingFilter.logRequestTo(stream), ResponseLoggingFilter.logResponseTo(stream));
        }
    }

}
