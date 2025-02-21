package com.purna.eval.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;


@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,value = "com.purna.eval")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,value = "@error")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/reports/library-api-test-reports.html")
public class ExecuteApiFeatures {

}
