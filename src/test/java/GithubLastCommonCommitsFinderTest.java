import org.example.GithubLastCommonCommitsFinder;
import org.example.GithubUtils;
import org.example.exceptions.GithubUnauthorizedToken;
import org.example.exceptions.GithubUserDoesNotExistException;
import org.example.exceptions.GithubUserDoesNotHaveAccessToRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GithubLastCommonCommitsFinderTest {

    private static final String OWNER = "exampleOwner";
    private static final String REPO = "exampleRepo";
    private static final String TOKEN = "exampleToken";

    private GithubLastCommonCommitsFinder finder;

    @BeforeEach
    public void setup() throws IOException, GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken {
        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(anyString(), anyString())).thenReturn(true);
            mockedUtils.when(() -> GithubUtils.checkUserHasRepo(anyString(), anyString(), anyString())).thenReturn(true);

            finder = spy(new GithubLastCommonCommitsFinder(OWNER, REPO, TOKEN));
        }
    }

    @Test
    public void testFindLastCommonCommits_CommonExists() throws IOException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commonCommit");
        List<String> commitsBranchB = Arrays.asList("commitB1", "commonCommit", "commitB2");

        doReturn(commitsBranchA).when(finder).fetchCommits("branchA");
        doReturn(commitsBranchB).when(finder).fetchCommits("branchB");

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertEquals(1, result.size());
        assertEquals("commonCommit", result.iterator().next());
    }

    @Test
    public void testFindLastCommonCommits_NoCommonCommits() throws IOException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commitA3");
        List<String> commitsBranchB = Arrays.asList("commitB1", "commitB2", "commitB3");

        doReturn(commitsBranchA).when(finder).fetchCommits("branchA");
        doReturn(commitsBranchB).when(finder).fetchCommits("branchB");

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindLastCommonCommits_OneBranchEmpty() throws IOException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commitA3");
        List<String> commitsBranchB = Collections.emptyList();

        doReturn(commitsBranchA).when(finder).fetchCommits("branchA");
        doReturn(commitsBranchB).when(finder).fetchCommits("branchB");

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testUnauthorizedToken() {
        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(anyString(), anyString()))
                    .thenThrow(new GithubUnauthorizedToken());

            assertThrows(GithubUnauthorizedToken.class, () -> {
                new GithubLastCommonCommitsFinder(OWNER, REPO, "invalidToken");
            });
        }
    }

    @Test
    public void testUserDoesNotExist() {
        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(anyString(), anyString()))
                    .thenReturn(false);

            assertThrows(GithubUserDoesNotExistException.class, () -> {
                new GithubLastCommonCommitsFinder("nonexistentUser", REPO, TOKEN);
            });
        }
    }

    @Test
    public void testUserDoesNotHaveAccessToRepo() {
        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(anyString(), anyString())).thenReturn(true);
            mockedUtils.when(() -> GithubUtils.checkUserHasRepo(anyString(), anyString(), anyString()))
                    .thenReturn(false);

            assertThrows(GithubUserDoesNotHaveAccessToRepo.class, () -> {
                new GithubLastCommonCommitsFinder(OWNER, "restrictedRepo", TOKEN);
            });
        }
    }

    @Test
    public void testGithubApiError() throws IOException {
        doThrow(new IOException("GitHub API error")).when(finder).fetchCommits("branchA");

        assertThrows(IOException.class, () -> {
            finder.findLastCommonCommits("branchA", "branchB");
        });
    }
}