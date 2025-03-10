package dev.cozy.sbasync;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import java.util.Set;

@ShellComponent
public class JobShellCommands {
    private final BackgroundJobManager jobManager;

    public JobShellCommands(BackgroundJobManager jobManager) {
        this.jobManager = jobManager;
    }

    @ShellMethod(key = "cancel-all-jobs", value = "Cancel all background jobs.")
    public String cancelAllJobs() {
        jobManager.cancelAllJobs();
        return "All jobs cancelled!";
    }

    @ShellMethod(key = "cancel-job", value = "Cancel a specific job by ID.")
    public String cancelJob(String jobId) {
        jobManager.cancelJob(jobId);
        return "Job [" + jobId + "] cancelled!";
    }

    @ShellMethod(key = "list-jobs", value = "List all active jobs.")
    public String listJobs() {
        Set<String> jobIds = jobManager.getJobIds();
        return jobIds.isEmpty() ? "No active jobs." : "Active jobs: " + jobIds;
    }
}
