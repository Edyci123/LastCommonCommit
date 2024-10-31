package org.example.utils;

import org.example.exceptions.GithubUnauthorizedToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class GithubUtils {

    public static HttpURLConnection
     createConnection(String urlString, String token) throws IOException {
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

    public static boolean checkUserExistsByUsername(String username, String token) throws IOException, GithubUnauthorizedToken {
        String url = "https://api.github.com/users/" + username;
        HttpURLConnection connection = createConnection(url, token);

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            return true;
        } else if (responseCode == 401) {
            throw new GithubUnauthorizedToken();
        } else {
            return false;
        }

    }

    public static boolean checkUserHasRepo(String username, String repo, String token) throws IOException, GithubUnauthorizedToken {
        String url = String.format("https://api.github.com/repos/%s/%s", username, repo);
        HttpURLConnection connection = createConnection(url, token);

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            return true;
        } else if (responseCode == 401) {
            throw new GithubUnauthorizedToken();
        } else {
            return false;
        }
    }

}
