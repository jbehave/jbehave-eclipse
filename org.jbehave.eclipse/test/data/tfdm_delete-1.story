!-- Check deletion of non used Twitter feed--!

Given an inactive direct message Twitter feed named 'feed1112213'

When administrator edits Twitter feed with name 'feed1112213'
And administrator deletes current Twitter feed
Then list of feeds doesn't contain 'feed1112213'
