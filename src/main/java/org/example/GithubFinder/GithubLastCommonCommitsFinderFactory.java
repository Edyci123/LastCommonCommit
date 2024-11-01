package org.example.GithubFinder;

import org.example.LastCommonCommitsFinder;
import org.example.LastCommonCommitsFinderFactory;
import org.example.exceptions.*;

import java.io.IOException;

public class GithubLastCommonCommitsFinderFactory implements LastCommonCommitsFinderFactory {

    @Override
    public LastCommonCommitsFinder create(String owner, String repo, String token) throws IOException, GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken, GithubRequestTimeoutException, GithubConnectionException {
        return new GithubLastCommonCommitsFinder(owner, repo, token);
    }
}

