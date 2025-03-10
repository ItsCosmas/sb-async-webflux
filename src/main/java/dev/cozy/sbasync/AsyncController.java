package dev.cozy.sbasync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api")
public class AsyncController {
    private final AsyncService asyncService;
    private final BackgroundJobManager jobManager;

    public AsyncController(AsyncService asyncService, BackgroundJobManager jobManager) {
        this.asyncService = asyncService;
        this.jobManager = jobManager;
    }

    @GetMapping("/start-task")
    public Mono<String> startAsyncTask() {
        log.info("Received request to start async task.");
        String jobId = jobManager.generateJobId(); // Generate a unique job ID

        Disposable disposable = asyncService.executeAsyncTask()
                .subscribeOn(Schedulers.boundedElastic()) // Run async task in the background
                .doOnSubscribe(subscription -> log.info("Kicking off async task [{}] within response stream.", jobId))
                .doOnSuccess(response -> {
                    log.info("Job [{}] completed successfully.", jobId);
                    jobManager.removeJob(jobId); // Remove only on successful completion
                })
                .doOnError(error -> {
                    log.error("Error occurred in job [{}]: {}", jobId, error.getMessage(), error);
                    jobManager.cancelJob(jobId); // Ensure job is cleaned up if it failed
                }).subscribe();

        jobManager.addJob(jobId, disposable); // Track the job in BackgroundJobManager

        return Mono.just("Async task started with ID: " + jobId + ". You can continue doing other work.")
                .doOnSuccess(response -> log.info("HTTP response sent: {}", response))
                .doOnError(error -> log.error("Error in startAsyncTask endpoint: {}", error.getMessage(), error));
    }
}
