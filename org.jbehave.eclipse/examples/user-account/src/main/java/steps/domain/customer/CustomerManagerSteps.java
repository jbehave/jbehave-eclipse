package steps.domain.customer;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class CustomerManagerSteps {

    @Given("a customer named '$customerKey' created from model '$predefinedCustomer'")
    public void createCustomer(//
            @Named("customerKey") String customerKey,//
            @Named("predefinedCustomer") String predefinedCustomer) throws Exception {
    }
}
