package openconsignment.service;

import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import openconsignment.common.AppConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BackupService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private AppConfig appConfig;

    @Inject
    public BackupService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public boolean checkSync() {
        // Implementation for checking sync
        logger.info("executing called");
        try {
            pull();
        } catch (IOException | GitAPIException e) {
            logger.error("Error during git pull", e);
            return false;
        }
        logger.info("checkSync called");
        return true;
    }

    public void pull() throws IOException, GitAPIException {
        logger.info("executing git pull");

        AppConfig.Github github = appConfig.getGithub();
        String repoPath = github.getRepoName();
        String username = github.getUsername();
        String token = github.getToken();
        String branchName = github.getBranchName();
        String remoteName = github.getRemoteName();

        logger.info("token is {}", token);

        try (Git git = Git.open(new File(repoPath))) {

            PullResult result = git.pull()
                    .setRemote(remoteName)
                    .setRemoteBranchName(branchName)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();

            if (result.isSuccessful()) {
                logger.info("Successfully pulled: {}", branchName);
            } else {
                throw new IllegalStateException("Pull failed for branch: " + branchName);
            }
        }
    }
}
