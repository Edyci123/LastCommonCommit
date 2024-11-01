package org.example.utils;

import org.example.exceptions.GithubUnauthorizedToken;
import org.example.exceptions.GithubConnectionException;
import org.example.exceptions.GithubRequestTimeoutException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GithubUtils {

    public static HttpURLConnection createConnection(String urlString, String token) throws IOException {
        try {
            URI uri = new URI(urlString);
            URL apiUrl = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (token != null && !token.isEmpty()) {
                connection.setRequestProperty("Authorization", "token " + token);
            }

            return connection;
        } catch (Exception e) {
            throw new IOException("Failed to create connection", e);
        }
    }

    public static boolean checkUserExistsByUsername(String username, String token) throws GithubUnauthorizedToken, GithubConnectionException, GithubRequestTimeoutException {
        String url = "https://api.github.com/users/" + username;

        try {
            HttpURLConnection connection = createConnection(url, token);
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return true;
            } else if (responseCode == 401) {
                throw new GithubUnauthorizedToken();
            } else {
                return false;
            }
        } catch (java.net.SocketTimeoutException e) {
            throw new GithubRequestTimeoutException("Request timed out while checking user existence: " + username, e);
        } catch (IOException e) {
            throw new GithubConnectionException("Connection failed while checking user existence: " + username, e);
        }
    }

    public static boolean checkUserHasRepo(String username, String repo, String token) throws GithubUnauthorizedToken, GithubConnectionException, GithubRequestTimeoutException {
        String url = String.format("https://api.github.com/repos/%s/%s", username, repo);

        try {
            HttpURLConnection connection = createConnection(url, token);
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                return true;
            } else if (responseCode == 401) {
                throw new GithubUnauthorizedToken();
            } else {
                return false;
            }
        } catch (java.net.SocketTimeoutException e) {
            throw new GithubRequestTimeoutException("Request timed out while checking repository existence for user: " + username + " and repo: " + repo, e);
        } catch (IOException e) {
            throw new GithubConnectionException("Connection failed while checking repository existence for user: " + username + " and repo: " + repo, e);
        }
    }
}
