package steps;

import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;

public class UtilSteps {
    @When("waiting for synchronization '$seconds' seconds")
    public void waiting(@Named("seconds") int seconds) {
    }
    
    @AfterStories
    public void tearDown() {
    }
}
