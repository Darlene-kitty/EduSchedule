package cm.iusjc.course.scheduling.config;

import cm.iusjc.course.scheduling.algorithm.FordFulkersonScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public FordFulkersonScheduler fordFulkersonScheduler() {
        return new FordFulkersonScheduler();
    }

    /** Pool dédié aux générations async (évite de bloquer le pool HTTP) */
    @Bean(name = "schedulingExecutor")
    public Executor schedulingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("timetable-gen-");
        executor.initialize();
        return executor;
    }
}
