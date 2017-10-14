package com.natedennis.jobs.accesslog;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.natedennis.configuration.InfrastructureConfiguration;
import com.natedennis.data.domain.AccessLog;
import com.natedennis.jobs.LogProcessListener;
import com.natedennis.jobs.ProtocolListener;

@Configuration
@EnableBatchProcessing
@Import({InfrastructureConfiguration.class})
public class BatchConfiguration {
    public static final String OVERRIDEN_BY_EXPRESSION_VALUE = "overriden by expression value";
    
    @Autowired
    private JobBuilderFactory jobs;
   
    @Autowired
    private JobRepository jobRepository;
 
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Bean(name="accessLogJob")
    public Job addAccessLogJob(){
        return jobs.get("accessLogJob")
                .listener(protocolListener())
                .start(step())
                .build();
    }   
    
    @Bean
    public JobLauncher asyncJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }
    
    @Bean
    public Step step(){
        return stepBuilderFactory.get("step")
                .<AccessLog,AccessLog>chunk(500) //important to be one in this case to commit after every line read
                .reader(reader(OVERRIDEN_BY_EXPRESSION_VALUE))
                .processor(processor())
                .writer(writer())
                .listener(logProcessListener())
                .faultTolerant()
                .skipLimit(10) //default is set to 0
                .skip(Exception.class)
                .build();
    }   
    

    @Bean
    @StepScope
    public FlatFileItemReader<AccessLog> reader(@Value("#{jobParameters[file]}") String file) {
        FlatFileItemReader<AccessLog> reader = new FlatFileItemReader<AccessLog>();
        if(file == null) { 
            file = "target/classes/access.log";
        }
        reader.setResource(getFileFromDirectory(file));
        reader.setLineMapper(lineMapper());

        return reader; 
    }


    @Bean
    public AccessLogItemProcessor processor() {
        return new AccessLogItemProcessor();
    }
    @Bean
    public ItemWriter<AccessLog> writer() {
        return new  com.natedennis.jobs.accesslog.dao.Writer();
    }
    

    
    @Bean
    public LineMapper<AccessLog> lineMapper() {
        DefaultLineMapper<AccessLog> lineMapper = new DefaultLineMapper<AccessLog>();
        
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter("|");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(new String[]{"ACCESS_DATE", "IP", "REQUEST", "STATUS", "USER_AGENT"});
        
        BeanWrapperFieldSetMapper<AccessLog> fieldSetMapper = new BeanWrapperFieldSetMapper<AccessLog>();
        fieldSetMapper.setTargetType(AccessLog.class);
        
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(accessLogFieldSetMapper());
        
        return lineMapper;
    }
    
    @Bean
    public AccessLogFieldSetMapper accessLogFieldSetMapper() {
        return new AccessLogFieldSetMapper();
    }
    

    private Resource getFileFromDirectory(String path) {
        File f =  Paths.get(path).toFile();
        return new FileSystemResource(f);
    }
    
    @Bean
    public ProtocolListener protocolListener(){
        return new ProtocolListener();
    }
 
    @Bean
    public LogProcessListener logProcessListener(){
        return new LogProcessListener();
    }    
    // end::jobstep[]
}
