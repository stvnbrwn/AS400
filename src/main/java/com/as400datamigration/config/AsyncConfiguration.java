/*
 * package com.as400datamigration.config;
 * 
 * 
 * import java.util.concurrent.Executor;
 * 
 * import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Value; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.scheduling.annotation.EnableAsync; import
 * org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 * 
 * import lombok.extern.slf4j.Slf4j;
 * 
 * @Slf4j
 * 
 * @Configuration
 * 
 * @EnableAsync public class AsyncConfiguration {
 * 
 * @Value("${thread.pool.size}") private int poolSize;
 * 
 * @Value("${thread.queue.capacity}") private int queueCapacity;
 * 
 * 
 * @Bean (name = "ThreadExecutor") public Executor taskExecutor() {
 * log.debug("Creating Async Task Executor"); final ThreadPoolTaskExecutor
 * executor = new ThreadPoolTaskExecutor(); //executor.setCorePoolSize(2);
 * executor.setMaxPoolSize(poolSize); executor.setQueueCapacity(queueCapacity);
 * executor.setThreadNamePrefix("**TableThread**"); executor.initialize();
 * return executor; }
 * 
 * }
 */