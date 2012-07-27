package steps.domain.askforhelp;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class CreateAnHelpDemandSteps {

    @When("I open the first delegation")
    public void openDelegation() {
    }

    @When("I create the help demand to agent : '$agentLastName' with comment : '$comment'")
    public void createHelpDemand(String agentLastName, String comment) {
    }

    @Then("The ask for help is sent without problem")
    public void checkActionMessage() {
    }

    @Then("an error message '$errorMessage' is displayed")
    public void checkErrorMessage(String errorMessage) {
    }
}
