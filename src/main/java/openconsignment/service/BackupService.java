package openconsignment.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import openconsignment.common.AppConfig;
import openconsignment.common.Constants;
import openconsignment.common.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
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
        // Implementation for checking sync
        log.info("executing called");
        try {
            pull();
        } catch (IOException | GitAPIException e) {
            log.error("Error during git pull", e);
            throw e;
        }
        log.info("checkSync called");

        try {
            String localFileHash = Utils.md5sum(Path.of(Constants.DATABASE_FILE_NAME));
            String repoFileHash =
                    Utils.md5sum(Path.of(appConfig.getGithub().getRepoName() + "/" + Constants.DATABASE_FILE_NAME));
            boolean result = 0 == localFileHash.compareTo(repoFileHash);
            log.info(
                    "CheckSync result = {}, localFileHash = {}, repoFileHash = {}",
                    result,
                    localFileHash,
                    repoFileHash);
            return result;
        } catch (Exception ex) {
            log.error(ex.toString());
            throw ex;
        }
    }

    public void pull() throws IOException, GitAPIException {
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
}
