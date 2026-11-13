package openconsignment.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.AppConfig;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Slf4j
@Singleton
public class BackupService {

    private final AppConfig appConfig;

    @Inject
    public BackupService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public boolean checkSync() throws Exception {
        log.info("executing called");
        try {
            pull();
            return compareDatabaseHashes();
        } catch (Exception e) {
            log.error("Error during git pull", e);
            throw e;
        }
    }

    public boolean compareDatabaseHashes() throws Exception {
        String localFileHash = Utils.md5sum(Path.of(Constants.DATABASE_FILE_NAME));
        String repoFileHash =
                Utils.md5sum(Path.of(appConfig.getGithub().getRepoName() + "/" + Constants.DATABASE_FILE_NAME));
        boolean result = 0 == localFileHash.compareTo(repoFileHash);
        log.info("CheckSync result = {}, localFileHash = {}, repoFileHash = {}", result, localFileHash, repoFileHash);
        return result;
    }

    private void copyRepoToLocal() throws IOException {
        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();

        log.info("Copying {} to {}", repoPath, Constants.DATABASE_FILE_NAME);
        Files.copy(
                Path.of(new File(repoPath + "/" + Constants.DATABASE_FILE_NAME).toURI()),
                Path.of(new File(Constants.DATABASE_FILE_NAME).toURI()),
                StandardCopyOption.REPLACE_EXISTING);
        log.info("Copied {} to {}", repoPath, Constants.DATABASE_FILE_NAME);
    }

    private void pull() throws IOException, GitAPIException {
        log.info("executing git pull");

        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();
        String username = github.getUsername();
        String token = github.getToken();
        String branchName = github.getBranchName();
        String remoteName = github.getRemoteName();

        log.info("token is {}", token);

        try (Git git = Git.open(new File(repoPath))) {

            PullResult result = git.pull()
                    .setRemote(remoteName)
                    .setRemoteBranchName(branchName)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();

            if (result.isSuccessful()) {
                log.info("Successfully pulled: {}", branchName);
            } else {
                throw new IllegalStateException("Pull failed for branch: " + branchName);
            }
        }
    }

    public void download() throws GitAPIException, IOException {
        pull();
        copyRepoToLocal();
    }

    private void copyLocalToRepo() throws IOException {
        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();

        log.info("Copying {} to {}", Constants.DATABASE_FILE_NAME, repoPath);
        Files.copy(
                Path.of(new File(Constants.DATABASE_FILE_NAME).toURI()),
                Path.of(new File(repoPath + "/" + Constants.DATABASE_FILE_NAME).toURI()),
                StandardCopyOption.REPLACE_EXISTING);
        log.info("Copied {} to {}", Constants.DATABASE_FILE_NAME, repoPath);
    }

    private void addAndCommit() throws IOException, GitAPIException {

        log.info("executing add and commit");

        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();

        try (Git git = Git.open(new File(repoPath))) {

            // Stage changes
            git.add().addFilepattern(".").call();

            // Commit changes
            RevCommit commit = git.commit().setMessage("Automated backup").call();

            log.info("Commit: " + commit.getId().getName());
        }
    }

    private void push() throws IOException, GitAPIException {
        log.info("executing git push");

        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();
        String username = github.getUsername();
        String token = github.getToken();
        String branchName = github.getBranchName();
        String remoteName = github.getRemoteName();

        log.info("token is {}", token);

        try (Git git = Git.open(new File(repoPath))) {
            Iterable<PushResult> results = git.push()
                    .setRemote(remoteName)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();

            for (PushResult result : results) {
                log.info(result.getMessages());
            }
        }
    }

    public void upload() throws GitAPIException, IOException {
        copyLocalToRepo();
        addAndCommit();
        push();
    }
}
