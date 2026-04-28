package bdd;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/bdd")
public class CucumberTest {
}