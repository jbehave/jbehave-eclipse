package steps.domain.account;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.model.ExamplesTable;

public class AccountSteps {

    @Given("an account {model |}named '$accountModelKey'")
    public void createAccountModel(//
            @Named("accountModelKey") String accountModelKey) {
    }

    @Given("The '$fieldKey' in account {model |}'$accountModelKey' is '$fieldValue'")
    public void setAccountModelProperty(//
            @Named("fieldKey") String fieldKey, //
            @Named("accountModelKey") String accountModelKey, //
            @Named("fieldValue") String fieldValue) {
    }

    @Given("The '$fieldKey' in account {model |}'$accountModelKey' is a random string")
    public void setAccountModelRandomProperty(//
            @Named("fieldKey") String fieldKey, //
            @Named("accountModelKey") String accountModelKey) {
    }

    @Given("The '$fieldKey' in account model '$accountModelKey' is '$fieldValue' taken from settings")
    public void setAccountModelPropertyFromSettings(//
            @Named("fieldKey") String fieldKey, //
            @Named("accountModelKey") String accountModelKey, //
            @Named("fieldValue") String fieldValue) {
    }

    @Given("The account kind of account model '$accountModelKey' is '$kindKey'")
    public void setAccountModelKind(//
            @Named("accountModelKey") String accountModelKey, //
            @Named("kindKey") String kindKey) {
    }
    
    @Given("a new account named '$accountKey' with the properties exactly as follows $propertiesTable")
    public void createAnAccount(@Named("accountKey") String accountKey,
            @Named("propertiesTable") ExamplesTable propertiesTable) {
    }

    /**
     * Create a new account with the specified properties. Account is then stored
     * in the current content with the provided key.
     * 
     * @param accountKey key to access the account from the context
     * @param propertiesTable key/value pairs of properties.
     */
    @Given("a new account named '$accountKey' with the following properties (properties not set will be completed) $propertiesTable")
    public void createAnAccountAndCompleteEmptyFields(@Named("accountKey") String accountKey,
            @Named("propertiesTable") ExamplesTable propertiesTable) {
    }

    @Given("an account named '$accountKey' created from model '$accountModelKey'")
    public void createAnAccountFromModel(@Named("accountKey") String accountKey,
            @Named("accountModelKey") String accountModelKey) {
    }
}
