package org.example;

public class GithubLastCommonCommitsFinderFactory implements LastCommonCommitsFinderFactory {

    @Override
    public LastCommonCommitsFinder create(String owner, String repo, String token) {
        return new GithubLastCommonCommitsFinder(owner, repo, token);
    }
}

