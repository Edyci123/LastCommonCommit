# LastCommonCommit

In this project I implemented a Last Common Commit finder for GitHub, using the [GitHubAPI](https://docs.github.com/en/rest).

I used the HttpClient from java.net.
For caching, I used Redis with the **redisson** library.

The solution works like this:
- 
- first it creates a finder, that takes as arguments the owner, the repo and the token
- it then checks if the user exists, if the user has access to the repo and throws errors accordingly 
- after this you can call a function with the name of the 2 branches
- the algorithm will try to first get the data from the cache, and then get the last common commit,
but if doesn't find anything, it will try to call the GitHub API.

Caching was a bit tricky. In this implementation, 
I would first try to get the last common commits, 
by following the next steps:
1. I will try to get the commits history from both branches, 
then compare to see if I have any common commits.
2. If one of the lists is empty I will always run the API calls, 
save them into the cache, and return the last commit.

Here is where I got the dilemma:

In the case when the lists are not empty, when I check the cache, they have no common commits,
but in reality they have been merged, so I would have to get the new latest commit(this case is valid
for different cases like orphaned branches, merges that happen in the last moment).

So I had the decision of either running the api calls again when I found no common commits, 
or run them again only when the arrays with the history of the commits are empty.

I choose to go with the one that ran the fetch algorithm to get the commit history of each
when there were no matches for maximum correctness.

As an optimization I made the algorithm gather pages of commits, until it found the first common one,
as a branch may have lots of commits, and it would be a waste to get all of them everytime.

I still have some ideas and I hadn't enough time to implement them too, they fix the existing code, 
and optimize some features either for correctness or for efficiency.

Ideas for the future:
-
- Save the timestamp for the latest commit in the cache, and check everytime if the branch was updated in the meanwhile,
by comparing the saved timestamp with the last change timestamp on the branch.
- Display also the names of the branches available to choose from,
displaying them in the terminal and let you choose the preferred branches, 
making the process more interactive.
- Add GitHub OAuth and display the data more beautifully