package steps.domain.account;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class LoginSteps {

    @Given("the login '$login' and a password '$password'")
    public void loginIsSettedToContext(//
            @Named("login") String login, //
            @Named("password") String password) {
    }

    @When("agent fill in the login filed '$login' and in the password field '$password'")
    public void setLoginAndPassword(//
            @Named("login") String login, //
            @Named("password") String password) {
    }

    /**
     * Log agent with the specified name.
     * @param accountKey name of the agent in the
     *    current context.
     */
    @When("account named '$accountKey' is logged")
    public void logAccountToApplication(//
            @Named("accountKey") String accountKey) {
    }

    /**
     * Agent clicks on the <b>login</b> button.
     */
    @When("I log to the application")
    @Alias("agent clicks on Login button")
    public void logToApplication() {
    }

    @Then("I confirm that I am logged")
    @Alias("agent see the application home page")
    public void iSeeTheLogout() {
    }
    
    @When("I confirm that I am logged")
    @Alias("agent see the application home page")
    public void iConfirmImLogged() {
    }

    @When("agent displays Login page")
    public void displayLoginPage() {
    }

    @Then("I see login incorrect")
    @Alias("agent see incorrect login error message")
    public void iSeeLoginIncorrect() {
    }
}
