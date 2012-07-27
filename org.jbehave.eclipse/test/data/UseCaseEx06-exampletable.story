Given a new account named 'networkAgent' with the following properties (properties not set will be completed) 
|key|value|
|Login|networkAgentLogin|
|Password|networkAgentPassword|

!-- Test login using a bad password !
When agent displays Login page
When agent fill in the login filed 'networkAgentLogin' and in the password field 'BadPassword'
When agent clicks on Login button
Then agent see incorrect login error message
