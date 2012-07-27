package steps.domain.request;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class CreateRequestSteps {

    @Given("'$requestNb' new requests named '$requestListKey' from customer '$customerKey' created by API")
    public void createRequest(//
            @Named("requestNb") int requestNb, //
            @Named("requestListKey") String requestListKey, //
            @Named("customerKey") String customerKey) {
    }

    @Given("a single request named '$requestKey' created by API from customer '$customerKey'")
    public void createOneRequest(//
            @Named("requestKey") String requestKey, //
            @Named("customerKey") String customerKey) {
    }

}
