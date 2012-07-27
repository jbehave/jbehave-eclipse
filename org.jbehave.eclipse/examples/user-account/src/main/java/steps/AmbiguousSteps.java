package steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class AmbiguousSteps {

	@Given("noop $X")
	public void noop(@Named("X") String X) {
	}

	@Given(value = "noop <$X>", priority = 1)
	public void noop2(@Named("X") String X) {
	}
	
	@Given(value = "noop #$X#", priority = 1)
	public void noop3(@Named("X") String X) {
	}
}
