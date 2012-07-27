package steps.domain.ui;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class OpenRequestPageSteps { //

	@Then("View a request page is displayed")
	public void iSeeViewRequestPage() {
	}
	
	@When ("Agent clicks on '$button' button")
	public void clickOn(
	        @Named("button") String button) {
	}
	
	@Then ("'$section' section is displayed")
	public void checkSection(
            @Named("section") String section) {
    }
	
	@When ("Agent clicks on '$button' lookupButton")
    public void lookup(
            @Named("button") String button) {
    }
	
	@Then ("popup is closed")
	public void selectMainWindow() {
	}
}
