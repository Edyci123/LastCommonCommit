package org.example;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GithubLastCommonCommitsFinder implements LastCommonCommitsFinder {

    private final String owner;
    private final String repo;
    private final String token;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GithubLastCommonCommitsFinder(String owner, String repo, String token) {
        this.owner = owner;
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

    private List<String> fetchCommits(String branch) throws IOException {
        String url = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s", owner, repo, branch);
        HttpURLConnection connection = createConnection(url);

        // Read the response
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

    private HttpURLConnection createConnection(String urlString) throws IOException {
        try {
            URI uri = new URI(urlString);
            URL apiUrl = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (token != null && !token.isEmpty()) {
                connection.setRequestProperty("Authorization", "token " + token);
            }

            return connection;
        } catch (Exception e) {
            throw new IOException("Failed to create connection", e);
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

