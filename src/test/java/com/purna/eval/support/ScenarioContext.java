package com.purna.eval.support;


import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ScenarioScope
@NoArgsConstructor
public class ScenarioContext {

    private String username;
    private String author;
    private String bookTitle;
    private int availCopies;
    private Response response;
    private String dueDate;
    private String borrowDate;
    private String returnDate;

}
