package steps.domain.ui;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class WelcomePageSteps { //

    @Then("'$accountKey' is logged")
    public void iSeeTheLogout(@Named("accountKey") String accountKey) {
    }

    @Then("'$requestNb' request is present in '$listKey' list")
    public void checkRequestNbInList(
            @Named("requestNb") String requestNb, 
            @Named("listKey") String listKey) {
    }
    
    
    @When("Agent clicks on '$listKey' list in home page")
    public void goToList(@Named("listKey")String listKey) {
    }
    
    @When("Agent clicks on '$menuKey' menu in home page")
    public void goToMenu(@Named("menuKey") String menuKey) {
    }
       
}
