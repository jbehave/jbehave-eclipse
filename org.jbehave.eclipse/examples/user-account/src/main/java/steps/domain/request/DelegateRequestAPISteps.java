package steps.domain.request;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;

public class DelegateRequestAPISteps {

    @Given("'$requestsKey' forwarded to '$accountKey' by '$supervisorKey' using API")
    public void forwardRequestsToAgent(//
            @Named("requestsKey") String requestsKey, //
            @Named("accountKey") String accountKey, //
            @Named("supervisorKey") String supervisorKey) {
    }

    @Given("'$requestKey' is forwarded to '$accountKey' by '$supervisorKey' using API")
    public void forwardRequestToAgent(//
            @Named("requestKey") String requestKey, //
            @Named("accountKey")  String accountKey, //
            @Named("supervisorKey") String supervisorKey) {
    }

}
