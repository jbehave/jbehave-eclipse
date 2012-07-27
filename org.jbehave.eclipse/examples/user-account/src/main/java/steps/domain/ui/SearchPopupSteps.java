package steps.domain.ui;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class SearchPopupSteps {

    @Then("Search pop up is displayed")
    public void waitForPopup() {
    }

    @When("Agent fills '$accountKey' lastName in LastName field")
    public void createHelpDemand(//
            @Named("accountKey") String accountKey) {
    }
    
    @When ("Agent clicks on search button")
    public void clickOnsearch() {
    }
    
    @Then("No agent are found")
    public void noAgent() {
    }

    @When ("agent closes popup")
    public void closePopup (){
    }
    
}
