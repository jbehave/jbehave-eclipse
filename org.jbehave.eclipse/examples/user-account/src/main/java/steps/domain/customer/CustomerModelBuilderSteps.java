package steps.domain.customer;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class CustomerModelBuilderSteps {

    @Given("a customer model named '$customerModelKey'")
    public void createCustomerModel(//
            @Named("customerModelKey") String customerModelKey) {
    }

    @Given("The '$fieldKey' in customer model '$customerModelKey' is '$fieldValue'")
    public void setCustomerModelProperty(//
            @Named("fieldKey") String fieldKey, //
            @Named("customerModelKey") String customerModelKey, //
            @Named("fieldValue") String fieldValue) {
    }
    
    
    @Given("The '$fieldKey' in customer model '$customerModelKey' is a random string of '$size' characters")
    public void setCustomerModelRandomProperty(//
            @Named("fieldKey") String fieldKey, //
            @Named("customerModelKey") String customerModelKey, //
            @Named("size") String size) {
    }
    
    @Given("The '$fieldKey' in customer model '$customerModelKey' is a random string")
    public void setCustomerModelRandomProperty(//
            @Named("fieldKey") String fieldKey, //
            @Named("customerModelKey") String customerModelKey) {
    }
    
    @Given("The Email in customer model '$customerModelKey' is a random '$validity' Email")
    public void setcustomerModelEmail(//
            @Named("customerModelKey") String customerModelKey, //
            @Named("validity") String validity) {
    }

    @Given("The '$fieldKey' in customer model '$customerModelKey' is '$fieldValue' taken from settings")
    public void setCustomerModelPropertyFromSettings(//
            @Named("fieldKey") String fieldKey, //
            @Named("customerModelKey") String customerModelKey, //
            @Named("fieldValue") String fieldValue) {
    }
}
