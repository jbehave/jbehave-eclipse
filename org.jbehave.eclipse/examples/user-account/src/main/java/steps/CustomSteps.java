package steps;

import org.jbehave.core.annotations.Given;

public class CustomSteps {

	@Given("a step with a custom parameter prefix %prefix")
	public void parameterPrefix(String prefix) {
	}
	

}
