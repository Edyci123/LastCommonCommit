package org.example;

import java.io.IOException;
import java.util.Collection;

public interface LastCommonCommitsFinder {

    /**
     * Finds SHAs of the last commits that are reachable from both
     * branchA and branchB.
     *
     * @param branchA   branch name (e.g., "main")
     * @param branchB   branch name (e.g., "dev")
     * @return  a collection of SHAs of the last common commits
     * @throws IOException  if any error occurs during the process
     */
    Collection<String> findLastCommonCommits(String branchA, String branchB) throws IOException;

}

