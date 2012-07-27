package steps.domain.ui;

import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class DelegationListPageSteps { 


	@Then("Delegations demands page is displayed")
	public void iSeeDelegationsDemandsPage() {
	}

	@When("Agent clicks on request '$requestOffset' in Delegations demands list")
	public void openDelegationDemandByOrder(
			@Named("delegationDemandOffset") String delegationDemandOffset) {
	}
}
