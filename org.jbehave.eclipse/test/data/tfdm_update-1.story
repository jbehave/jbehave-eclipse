Given an inactive direct message Twitter feed named 'feed1112215'
And a Twitter account with name 'account1112215', '1112215', '1112215'

!-- Check that we can update Twitter feed's properties--!

When administrator edits Twitter feed with name 'feed1112215'
And administrator changes Twitter feed's active to 'false'
And administrator saves current Twitter feed
And administrator edits Twitter feed with name 'feed1112215'
Then administrator sees Twitter feed disabled

When administrator edits Twitter feed with name 'feed1112215'
And administrator changes Twitter feed's schedule to '6 * 1 * * ?'
And administrator saves current Twitter feed
And administrator edits Twitter feed with name 'feed1112215'
Then administrator sees Twitter feed's schedule is '6 * 1 * * ?'

When administrator edits Twitter feed with name 'feed1112215'
And administrator sets Twitter feed's account to 'account1112215'
And administrator saves current Twitter feed
And administrator edits Twitter feed with name 'feed1112215'
Then administrator sees Twitter feed's account is 'account1112215'

When administrator edits Twitter feed with name 'feed1112215'
When administrator changes Twitter feed's name to 'modifiedfeed1112215'
And administrator saves current Twitter feed
Then list of feeds contains 'modifiedfeed1112215'
And list of feeds doesn't contain 'feed1112215'
When administrator edits Twitter feed with name 'modifiedfeed1112215'
Then administrator sees Twitter feed's name is 'modifiedfeed1112215'

!-- Check that we can update Twitter feed's name to one that had exist in the past--!

When administrator edits Twitter feed with name 'modifiedfeed1112215'
And administrator changes Twitter feed's name to 'feed1112215'
!--
And administrator saves current Twitter feed
Then list of feeds contains 'feed1112215'
And list of feeds doesn't contain 'modifiedfeed1112215'

When administrator edits Twitter feed with name 'feed1112215'
And administrator sets Twitter feed's search keyword 'newKeyword'
And administrator saves current Twitter feed
And administrator edits Twitter feed with name 'feed1112215'
Then administrator sees Twitter feed's search keyword is 'newKeyword'

When administrator edits Twitter feed with name 'feed1112215'
And administrator sets Twitter feed's query type to 'hometimeline'
Then administrator saves current Twitter feed and gets Feed query type update error

When administrator edits Twitter feed with name 'feed1112215'
And administrator sets Twitter feed's user monitor to 'userName'
Then administrator saves current Twitter feed and gets Feed query type update error
