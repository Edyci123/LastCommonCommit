package org.example;

import java.io.IOException;
import java.util.*;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exceptions.GithubUnauthorizedToken;
import org.example.exceptions.GithubUserDoesNotExistException;
import org.example.exceptions.GithubUserDoesNotHaveAccessToRepo;

public class GithubLastCommonCommitsFinder implements LastCommonCommitsFinder {

    private final String owner;
    private final String repo;
    private final String token;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GithubLastCommonCommitsFinder(String owner, String repo, String token) throws IOException, GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken {
        if (!GithubUtils.checkUserExistsByUsername(owner, token)) {
            throw new GithubUserDoesNotExistException(owner);
        }
        this.owner = owner;
        if (!GithubUtils.checkUserHasRepo(owner, repo, token)) {
            throw new GithubUserDoesNotHaveAccessToRepo(owner, repo);
        }
        this.repo = repo;
        this.token = token;
    }

    @Override
    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException {
        List<String> commitsA = fetchCommits(branchA);
        List<String> commitsB = fetchCommits(branchB);

        Set<String> commonCommits = new HashSet<>(commitsA);
        commonCommits.retainAll(commitsB);

        return findLastCommon(commonCommits, commitsA, commitsB);
    }

    public List<String> fetchCommits(String branch) throws IOException {
        String url = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s", owner, repo, branch);
        HttpURLConnection connection = GithubUtils.createConnection(url, token);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            JsonNode jsonResponse = objectMapper.readTree(reader);
            List<String> commits = new ArrayList<>();
            for (JsonNode commit : jsonResponse) {
                String sha = commit.get("sha").asText();
                commits.add(sha);
            }
            return commits;
        }
    }

    private Collection<String> findLastCommon(Set<String> commonCommits, List<String> commitsA, List<String> commitsB) {
        Set<String> lastCommonCommits = new HashSet<>();
        for (String commit : commitsA) {
            if (commonCommits.contains(commit)) {
                lastCommonCommits.add(commit);
                break;
            }
        }
        for (String commit : commitsB) {
            if (commonCommits.contains(commit)) {
                lastCommonCommits.add(commit);
                break;
            }
        }
        return lastCommonCommits;
    }
}

