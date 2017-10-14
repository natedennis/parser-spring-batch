package com.natedennis.configuration;


import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.natedennis.jobs.accesslog.BatchConfiguration;

@Configuration
@EnableBatchProcessing(modular=true)
public class AppConfig {
    
    @Bean
    public ApplicationContextFactory addNewPodcastJobs(){
    	return new GenericApplicationContextFactory(BatchConfiguration.class);
    }
    
    
}
