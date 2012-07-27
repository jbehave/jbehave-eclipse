Meta:
@step What


Narrative:
In order to Test story
As a plugin developer
I want to have a full story


Given a new account named 'networkAgent' with the following properties (properties not set will be completed) 
|key|value|
!-- Comment inside an example table
|Login|networkAgentLogin|
|Password|networkAgentPassword|

!-- Test login using a bad password !
When agent displays Login page
When agent fill in the login filed 'networkAgentLogin' and in the password field 'BadPassword'
When agent clicks on Login button
Then agent see incorrect login error message

!-- Test login using a correct password !
When agent fill in the login filed 'networkAgentLogin' and in the password field 'networkAgentPassword'
When agent clicks on Login button
Then agent see the application home page

Examples:
|key|value|
|-- Comment inside an example table
|Login|networkAgentLogin|
|Password|networkAgentPassword|
