Etant donné que a new account named 'networkAgent' with the following properties (properties not set will be completed)
|key|value|
|Login|networkAgentLogin|
|Password|networkAgentPassword|
!-- Test login using a bad password !

Quand agent displays Login page
Quand agent fill in the login filed 'networkAgentLogin' and in the password field 'BadPassword'
Quand agent clicks on Login button
Alors agent see incorrect login error message