package openconsignment.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private String getRepoFileHash() throws Exception {
        return Utils.md5sum(Path.of(appConfig.getGithub().getRepository() + "/" + Constants.DATABASE_FILE_NAME));
    }

    public boolean compareDatabaseHashes() throws Exception {
        String localFileHash = Utils.md5sum(Path.of(Constants.DATABASE_FILE_NAME));
        String repoFileHash = getRepoFileHash();

        boolean result = 0 == localFileHash.compareTo(repoFileHash);
        log.info("CheckSync result = {}, localFileHash = {}, repoFileHash = {}", result, localFileHash, repoFileHash);
        return result;
    }

    private void copyRepoToLocal() throws IOException {
        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepository();

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
        String repoPath = github.getRepository();
        String username = github.getUsername();
        String token = github.getToken();
        String branchName = github.getBranch();
        String remoteName = github.getRemote();

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
        String repoPath = github.getRepository();

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
        String repoPath = github.getRepository();

        try (Git git = Git.open(new File(repoPath))) {

            // Stage changes
            git.add().addFilepattern(".").call();

            // Current time in string
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String repoFileHash = "null";
            try {
                repoFileHash = getRepoFileHash();
            } catch (Exception e) {
                log.error(e.toString());
            }

            String message = "Backed up at " + currentTime + "; database hash = " + repoFileHash;

            // Commit changes
            RevCommit commit =
                    git.commit().setMessage(message).setAllowEmpty(false).call();

            log.info("Commit: " + commit.getId().getName());
        }
    }

    private void push() throws IOException, GitAPIException {
        log.info("executing git push");

        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepository();
        String username = github.getUsername();
        String token = github.getToken();
        String branchName = github.getBranch();
        String remoteName = github.getRemote();

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
