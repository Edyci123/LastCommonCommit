package org.example.GithubFinder;

import java.io.IOException;
import java.util.*;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.LastCommonCommitsFinder;
import org.example.exceptions.*;
import org.example.utils.CacheUtil;
import org.example.utils.GithubUtils;

public class GithubLastCommonCommitsFinder implements LastCommonCommitsFinder {

    private final String owner;
    private final String repo;
    private final String token;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CacheUtil cacheUtil;

    public GithubLastCommonCommitsFinder(String owner, String repo, String token) throws GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken, GithubRequestTimeoutException, GithubConnectionException {
        if (!GithubUtils.checkUserExistsByUsername(owner, token)) {
            throw new GithubUserDoesNotExistException(owner);
        }
        this.owner = owner;
        if (!GithubUtils.checkUserHasRepo(owner, repo, token)) {
            throw new GithubUserDoesNotHaveAccessToRepo(owner, repo);
        }
        this.repo = repo;
        this.token = token;
        this.cacheUtil = new CacheUtil();
    }

    @Override
    public Collection<String> findLastCommonCommits(String branchA, String branchB) throws GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        List<String> commitsA = fetchCacheCommits(branchA);
        List<String> commitsB = fetchCacheCommits(branchB);

        Set<String> commonCommits = new HashSet<>(commitsA);
        commonCommits.retainAll(commitsB);

        Collection<String> res = findLastCommon(commonCommits, commitsA, commitsB);
        if (res.isEmpty()) {
            int page = 1;
            Collection<String> lastCommonCommits;
            List<String> commitsPageA = fetchCommits(branchA, page);
            List<String> commitsPageB = fetchCommits(branchB, page);
            commitsA = new ArrayList<>(commitsPageA);
            commitsB = new ArrayList<>(commitsPageB);

            commonCommits = new HashSet<>(commitsPageA);
            commonCommits.retainAll(commitsPageB);

            lastCommonCommits = findLastCommon(commonCommits, commitsPageA, commitsPageB);

            while ((!commitsPageA.isEmpty() || !commitsPageB.isEmpty()) && lastCommonCommits.isEmpty()) {
                page++;

                if (!commitsPageA.isEmpty()) {
                    commitsA.addAll(commitsPageA);
                    commitsPageA = fetchCommits(branchA, page);
                }
                if (!commitsPageB.isEmpty()) {
                    commitsB.addAll(commitsPageB);
                    commitsPageB = fetchCommits(branchB, page);
                }

                commonCommits = new HashSet<>(commitsA);
                commonCommits.retainAll(commitsB);

                lastCommonCommits = findLastCommon(commonCommits, commitsA, commitsB);
            }

            System.out.println("No common commits found in cache!");

            try {
                cacheUtil.put(owner, repo, branchA, commitsA);
                cacheUtil.put(owner, repo, branchB, commitsB);
            } catch (Exception e) {
                System.err.println("Error saving commits to cache: " + e.getMessage());
            }


            cacheUtil.close();
            return lastCommonCommits;
        }

        cacheUtil.close();
        return res;
    }

    public List<String> fetchCacheCommits(String branch) {
        return cacheUtil.get(owner, repo, branch);
    }

    public List<String> fetchCommits(String branch, int page) throws GithubUserDoesNotHaveAccessToRepo, GithubConnectionException, GithubRequestTimeoutException {
        List<String> commits = new ArrayList<>();
        String url = String.format("https://api.github.com/repos/%s/%s/commits?sha=%s&page=%d", owner, repo, branch, page);
        HttpURLConnection connection = null;

        try {
            connection = GithubUtils.createConnection(url, token);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND || connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new GithubUserDoesNotHaveAccessToRepo(owner, repo);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                JsonNode jsonResponse = objectMapper.readTree(reader);
                if (jsonResponse.isEmpty()) {
                    return commits;
                }
                for (JsonNode commit : jsonResponse) {
                    String sha = commit.get("sha").asText();
                    commits.add(sha);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            throw new GithubRequestTimeoutException("Request timed out while fetching commits for branch " + branch, e);
        } catch (IOException e) {
            throw new GithubConnectionException("Connection failed while fetching commits for branch " + branch, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return commits;
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

    public void setCacheUtil(CacheUtil cacheUtil) {
        this.cacheUtil = cacheUtil;
    }
}
