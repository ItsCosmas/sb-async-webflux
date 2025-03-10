package dev.cozy.sbasync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BackgroundJobManager {
    private final Map<String, Disposable> jobs = new ConcurrentHashMap<>();

    /**
     * Adds a job to the manager.
     * @param jobId A unique identifier for the job.
     * @param disposable The Disposable representing the job.
     */
    public void addJob(String jobId, Disposable disposable) {
        jobs.put(jobId, disposable);
        log.info("Job [{}] started and tracked.", jobId);
    }


    /**
     * Removes a job from tracking without logging cancellation.
     * Used when a job completes successfully.
     */
    public void removeJob(String jobId) {
        jobs.remove(jobId);
        log.info("Job [{}] removed from tracking after successful completion.", jobId);
    }


    /**
     * Cancels a job if it exists.
     * @param jobId The unique job ID.
     */
    public void cancelJob(String jobId) {
        Disposable disposable = jobs.remove(jobId);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            log.info("Job [{}] cancelled.", jobId);
        } else {
            log.warn("Job [{}] not found or already completed.", jobId);
        }
    }

    /**
     * Cancels all running jobs (useful during shutdown).
     */
    public void cancelAllJobs() {
        jobs.forEach((jobId, disposable) -> {
            if (!disposable.isDisposed()) {
                disposable.dispose();
                log.info("Job [{}] cancelled during shutdown.", jobId);
            }
        });
        jobs.clear();
    }

    /**
     * Checks if a job is still running.
     * @param jobId The unique job ID.
     * @return True if the job is running, false otherwise.
     */
    public boolean isJobRunning(String jobId) {
        return jobs.containsKey(jobId) && !jobs.get(jobId).isDisposed();
    }

    /**
     * Generates a random job ID (can be replaced with a request ID in the future).
     * @return A randomly generated unique job ID.
     */
    public String generateJobId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves the set of active job IDs.
     *
     * @return A set containing the unique identifiers of all currently running jobs.
     */
    public Set<String> getJobIds() {
        return jobs.keySet();
    }
}