import org.example.GithubFinder.GithubLastCommonCommitsFinder;
import org.example.exceptions.*;
import org.example.utils.GithubUtils;
import org.example.utils.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GithubLastCommonCommitsFinderTest {

    private static final String OWNER = "exampleOwner";
    private static final String REPO = "exampleRepo";
    private static final String TOKEN = "exampleToken";

    private GithubLastCommonCommitsFinder finder;

    @Mock
    private CacheUtil cacheUtil;

    @BeforeEach
    public void setup() throws GithubUserDoesNotExistException, GithubUserDoesNotHaveAccessToRepo, GithubUnauthorizedToken, GithubRequestTimeoutException, GithubConnectionException {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(OWNER, TOKEN)).thenReturn(true);
            mockedUtils.when(() -> GithubUtils.checkUserHasRepo(OWNER, REPO, TOKEN)).thenReturn(true);

            finder = spy(new GithubLastCommonCommitsFinder(OWNER, REPO, TOKEN));
            finder.setCacheUtil(cacheUtil);
        }

        reset(cacheUtil);
    }

    @Test
    public void testFindLastCommonCommits_CommonExists() throws IOException, GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commonCommit");
        List<String> commitsBranchB = Arrays.asList("commitB1", "commonCommit", "commitB2");

        when(cacheUtil.get(OWNER, REPO, "branchA")).thenReturn(new ArrayList<>());
        when(cacheUtil.get(OWNER, REPO, "branchB")).thenReturn(new ArrayList<>());

        doReturn(commitsBranchA).when(finder).fetchCommits("branchA", 1);
        doReturn(commitsBranchB).when(finder).fetchCommits("branchB", 1);

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertEquals(1, result.size());
        assertEquals("commonCommit", result.iterator().next());

        verify(cacheUtil).put(OWNER, REPO, "branchA", commitsBranchA);
        verify(cacheUtil).put(OWNER, REPO, "branchB", commitsBranchB);
    }

    @Test
    public void testFindLastCommonCommits_NoCommonCommits() throws IOException, GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commitA3");
        List<String> commitsBranchB = Arrays.asList("commitB1", "commitB2", "commitB3");

        when(cacheUtil.get(OWNER, REPO, "branch1")).thenReturn(new ArrayList<>());
        when(cacheUtil.get(OWNER, REPO, "branch2")).thenReturn(new ArrayList<>());

        doReturn(commitsBranchA).when(finder).fetchCommits("branch1", 1);
        doReturn(commitsBranchB).when(finder).fetchCommits("branch2", 1);

        doReturn(new ArrayList<>()).when(finder).fetchCommits("branch1", 2);
        doReturn(new ArrayList<>()).when(finder).fetchCommits("branch2", 2);

        Collection<String> result = finder.findLastCommonCommits("branch1", "branch2");

        assertTrue(result.isEmpty(), "Expected no common commits but found some.");
    }

    @Test
    public void testFindLastCommonCommits_OneBranchEmpty() throws IOException, GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        List<String> commitsBranchA = Arrays.asList("commitA1", "commitA2", "commitA3");
        List<String> commitsBranchB = Collections.emptyList();

        when(cacheUtil.get(OWNER, REPO, "branchA")).thenReturn(new ArrayList<>());
        when(cacheUtil.get(OWNER, REPO, "branchB")).thenReturn(new ArrayList<>());

        doReturn(commitsBranchA).when(finder).fetchCommits("branchA", 1);
        doReturn(commitsBranchB).when(finder).fetchCommits("branchB", 1);

        doReturn(new ArrayList<>()).when(finder).fetchCommits("branchA", 2);
        doReturn(new ArrayList<>()).when(finder).fetchCommits("branchB", 2);

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertTrue(result.isEmpty(), "Expected no common commits with one branch empty.");
    }

    @Test
    public void testFetchCommitsFromCache() throws IOException, GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        List<String> cachedCommitsBranchA = Arrays.asList("commitA1", "commitA2", "commonCommit");
        List<String> cachedCommitsBranchB = Arrays.asList("commitB1", "commonCommit", "commitB2");

        when(cacheUtil.get(OWNER, REPO, "branchA")).thenReturn(cachedCommitsBranchA);
        when(cacheUtil.get(OWNER, REPO, "branchB")).thenReturn(cachedCommitsBranchB);

        Collection<String> result = finder.findLastCommonCommits("branchA", "branchB");

        assertEquals(1, result.size());
        assertTrue(result.contains("commonCommit"), "Expected 'commonCommit' to be found as the last common commit.");
        verify(finder, never()).fetchCommits("branchA", 1);
        verify(finder, never()).fetchCommits("branchB", 1);
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
    public void testGithubApiError() throws GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        doThrow(new GithubConnectionException("Error", new Throwable())).when(finder).fetchCommits("branchA", 1);

        assertThrows(GithubConnectionException.class, () -> {
            finder.findLastCommonCommits("branchA", "branchB");
        });
    }

    @Test
    public void testGithubApiErrorTimeout() throws GithubUserDoesNotHaveAccessToRepo, GithubRequestTimeoutException, GithubConnectionException {
        doThrow(new GithubRequestTimeoutException("Error", new Throwable())).when(finder).fetchCommits("branchA", 1);

        assertThrows(GithubRequestTimeoutException.class, () -> {
            finder.findLastCommonCommits("branchA", "branchB");
        });
    }


    @Test
    public void testFetchCommits_404NotFound() throws IOException {
        try (MockedStatic<GithubUtils> mockedUtils = Mockito.mockStatic(GithubUtils.class)) {
            mockedUtils.when(() -> GithubUtils.checkUserExistsByUsername(anyString(), anyString())).thenReturn(true);
            mockedUtils.when(() -> GithubUtils.checkUserHasRepo(anyString(), anyString(), anyString())).thenReturn(true);

            HttpURLConnection mockConnection = mock(HttpURLConnection.class);
            when(mockConnection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);

            mockedUtils.when(() -> GithubUtils.createConnection(anyString(), anyString())).thenReturn(mockConnection);

            assertThrows(GithubUserDoesNotHaveAccessToRepo.class, () -> {
                finder.fetchCommits("nonexistentBranch", 1);
            });
        }
    }
}
