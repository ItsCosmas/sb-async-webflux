package dev.cozy.sbasync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.time.Duration;

@Slf4j
@Service
public class AsyncService {

    public Mono<Void> executeAsyncTask() {
        log.info("Task scheduled in thread: {}", Thread.currentThread().getName());

        return Mono.delay(Duration.ofSeconds(5)) // Non-blocking delay
                .publishOn(Schedulers.boundedElastic()) // Move execution to a non-blocking thread pool
                .doOnNext(ignore -> log.info("Task completed in thread: {}", Thread.currentThread().getName()))
                .doOnError(error -> log.error("Error occurred in async task: {}", error.getMessage(), error)) // Log errors
                .then();
    }
}

