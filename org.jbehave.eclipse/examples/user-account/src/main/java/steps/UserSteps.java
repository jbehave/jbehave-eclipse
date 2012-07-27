package steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 */
public class UserSteps {

	@Given("a user named $username")
	public void user(String name) {
	}
	
	@When("user credits is $amount dollars")
	public void credits(int amount) {
	}

	@When("user clicks on $button button")
	public void clicks(String name) {
	}
	
	@Then("the page title must be $title")
	public void titleContent(String title) {
	}
	
	@Then("the page title must be displayed in $colorAlias")
	public void titleColor(String colorAlias) {
	}

}
