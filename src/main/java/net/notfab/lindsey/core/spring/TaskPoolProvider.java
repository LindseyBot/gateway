package net.notfab.lindsey.core.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class TaskPoolProvider {

    @Bean
    public TaskExecutor commands() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(6);
        executor.setCorePoolSize(2);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("Lindsey-CMD-");
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor eventWaiter() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setCorePoolSize(1);
        executor.setThreadNamePrefix("EventWaiter-");
        executor.initialize();
        return executor;
    }

}
