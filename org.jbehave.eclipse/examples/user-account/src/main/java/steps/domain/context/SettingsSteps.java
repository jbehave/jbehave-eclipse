package steps.domain.context;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;

public class SettingsSteps {

    @Given("an overriden value '$propertyValue' for key '$propertyKey'")
    public void defineSettingsProperty(//
            @Named("propertyKey")  String propertyKey, //
            @Named("propertyValue")  String propertyValue) {
    }

    @Then("the values of the properties '$propertyKeyActual' and '$propertyKeyExpected' are equals")
    public void sameValue(//
            @Named("propertyKeyActual") String propertyKeyActual, //
            @Named("propertyKeyExpected") String propertyKeyExpected) {
    }
}
