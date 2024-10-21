package com.purna.eval.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPOJOBuilder(withPrefix = "")
public class Book {

    @JsonProperty("title")
    private String title;
    @JsonProperty("author")
    private String author;
    @JsonProperty("isbn")
    private String isbn;
    @JsonProperty("category")
    private String category;
    @JsonProperty("availableCopies")
    private Integer availableCopies;

}
