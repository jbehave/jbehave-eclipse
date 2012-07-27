Narrative:
In order to be more communicative
As a story writer
I want to explain the use of And steps and also show that I can use keywords in scenario title and comments

Scenario: An initial And step should be marked as pending as there is not previous step
!-- What is this And of?  JBehave treats as pending
And the wind blows (PENDING)
!-- Look Ma' - I can also use keywords in scenario title and step comments!
 
Scenario: And steps should match the previous step type
Given the wind blows
!-- This And is equivalent to another Given
And the wind blows
When the wind blows
!-- This And is equivalent to another When
And the wind blows

Given a user named "Bob"
When 'Bob' clicks on the 'login' button
Then the 'password' field becomes 'red'



Given a 5 by 5 game
When I toggle the cell at (2, 3)
Then the grid should look like
.....
.....
.....
..X..
.....
When I toggle the cell at (2, 4)
Then the grid should look like
.....
.....
.....
..X..
..X..
When I toggle the cell at (2, 3)
Then the grid should look like
.....
.....
.....
.....
..X..