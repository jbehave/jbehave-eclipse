package steps;
import org.jbehave.core.annotations.*;
public class MySteps{
	@Then("the traders activity is: 
|name|trades|
|Larry|<trades>|
|Moe|1000|
|Curly|2000|")
	@Pending
	public void ThenTheTradersActivityIsnametradesLarrytradesMoe1000Curly2000(){
		 //TODO 
	}
}