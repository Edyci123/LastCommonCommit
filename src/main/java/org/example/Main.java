package org.example;

import java.io.IOException;
import java.util.Collection;

public class Main {
    public static void main(String[] args) {
        try {
            LastCommonCommitsFinderFactory factory = new GithubLastCommonCommitsFinderFactory();
            LastCommonCommitsFinder finder = factory.create("owner", "repo", "your-personal-access-token");

            Collection<String> commonCommits = finder.findLastCommonCommits("main", "dev");

            System.out.println("Last common commits:");
            for (String commit : commonCommits) {
                System.out.println(commit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}