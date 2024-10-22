package org.example;

import org.example.exceptions.GithubUnauthorizedToken;
import org.example.exceptions.GithubUserDoesNotExistException;
import org.example.exceptions.GithubUserDoesNotHaveAccessToRepo;

import java.io.IOException;

public class GithubLastCommonCommitsFinderFactory implements LastCommonCommitsFinderFactory {

    @Override
    public LastCommonCommitsFinder create(String owner, String repo, String token) throws IOException, GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken {
        return new GithubLastCommonCommitsFinder(owner, repo, token);
    }
}

