package com.purna.eval;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = EvalAutomation.class)
public class CucumberSpringConfiguration {

}
