package org.example;

import org.example.GithubFinder.GithubLastCommonCommitsFinderFactory;
import org.example.exceptions.GithubUnauthorizedToken;
import org.example.exceptions.GithubUserDoesNotExistException;
import org.example.exceptions.GithubUserDoesNotHaveAccessToRepo;

import java.io.IOException;
import java.util.Collection;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You have to specify OWNER REPO ACCESS_TOKEN in this order as args");
            return;
        }
        try {
            LastCommonCommitsFinderFactory factory = new GithubLastCommonCommitsFinderFactory();
            String owner = args[0];
            String repo = args[1];
            String token = args[2];
            LastCommonCommitsFinder finder = factory.create(owner, repo, token);

            Collection<String> commonCommits = finder.findLastCommonCommits("main", "branchA");

            System.out.println("Last common commits:");
            for (String commit : commonCommits) {
                System.out.println(commit);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        } catch (GithubUserDoesNotExistException | GithubUserDoesNotHaveAccessToRepo | GithubUnauthorizedToken e) {
            System.out.println(e.getMessage());
        }
    }
}