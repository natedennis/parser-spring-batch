package com.natedennis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.natedennis.data.dao.AccessLogDAO;
import com.natedennis.data.enumeration.Duration;


@Configuration 
@EnableAutoConfiguration 
@ComponentScan( lazyInit = true)
public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private static final String ACCESS_LOG_JOB = "accessLogJob";

    public static void main(String[] args) throws ParseException, IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, InterruptedException {
        String file = "access.log";
        Date startDate;
        Date endDate;
        Duration duration;
        int threshold = 100;

        Log log = LogFactory.getLog(Parser.class);

        SpringApplication app = new SpringApplication(Parser.class);
        app.setWebEnvironment(false);
        ConfigurableApplicationContext ctx = app.run(args);   
        
        JobLauncher jobLauncher = ctx.getBean("asyncJobLauncher",JobLauncher.class);
        AccessLogDAO accessLogDAO = ctx.getBean(AccessLogDAO.class);
        Options options = setupCLI();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);

        if (cmdLine.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sb server", options);
        } else {

            logger.debug("startDate : {}", cmdLine.hasOption("startDate") ? ((String) cmdLine.getParsedOptionValue("startDate")) : "startDate not found");
            logger.debug("duration : {}", cmdLine.hasOption("duration") ? ((String) cmdLine.getParsedOptionValue("duration")) : "duration not found");
            logger.debug("threshold : {}", cmdLine.hasOption("threshold") ? ((Number) cmdLine.getParsedOptionValue("threshold")) : "threshold not found");

            if (cmdLine.hasOption("accesslog")) {
                file = ((String) cmdLine.getParsedOptionValue("accesslog"));
                if (!Files.exists(Paths.get(file))) {
                    throw new IOException("File does not exist");
                }
            }

            // required 2017-01-02.13:00:00
            // format determined by the requirements document
            String pattern = "yyyy-MM-dd.HH:mm:ss";
            DateTime dateTime = DateTime.parse((String) cmdLine.getParsedOptionValue("startDate"),
                    DateTimeFormat.forPattern(pattern));
            ;
            startDate = dateTime.toDate();

            // required
            duration = Duration.valueOf(((String) cmdLine.getParsedOptionValue("duration")).toUpperCase());

            DateTime endCal = new DateTime(startDate);

            if (duration.equals(Duration.HOURLY)) {
                endDate = endCal.plusHours(1).toDate();
            } else {
                endDate = endCal.plusDays(1).toDate();
            }

            // optional
            if (cmdLine.hasOption("threshold")) {
                threshold = ((Number) cmdLine.getParsedOptionValue("threshold")).intValue();
            }

            accessLogDAO.cleanUp();
            
            Job accessLogJob = ctx.getBean(ACCESS_LOG_JOB, Job.class);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("runDate", new Date())
                    .addDate("startDate", startDate)
                    .addDate("endDate", endDate)
                    .addString("file", file)
                    .addLong("threshold", Long.valueOf(threshold))
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(accessLogJob, jobParameters);

            BatchStatus batchStatus = jobExecution.getStatus();
            while (batchStatus.isRunning()) {
                Long c = accessLogDAO.count();
                log.info("*********** Still running.... "+  c.toString() + " **************");
                Thread.sleep(1000);
                batchStatus = jobExecution.getStatus();
            }
            
            ExitStatus exitStatus = jobExecution.getExitStatus();
            String exitCode = exitStatus.getExitCode();
            
            log.debug(String.format("*********** Exit status: %s", exitCode));

            if(exitStatus.equals(ExitStatus.COMPLETED)){
            
                logger.info("finding ips occuring more than {}, between the dates {} and {}",
                        threshold, 
                        startDate,
                        endDate);

                List<String> ips = accessLogDAO.threadHoldQuery(startDate, endDate, threshold);
                
                logger.info("******");
                logger.info("*");
                logger.info("*");
                ips.forEach(ip -> logger.info("* " + ip ));
                logger.info("*");
                logger.info("*");
                logger.info("******");

                logger.info("copy records matching this criteria to access_log_filtered_copy");

                // TODO this could be moved into an async job as well with status... but
                accessLogDAO.copyFilterResults(startDate, endDate, threshold);
                logger.info(" ");
                logger.info(" ");
                logger.info(" ");
                logger.info("process complete");
                
            }

            JobInstance jobInstance = jobExecution.getJobInstance();
            log.debug(String.format("********* Name of the job %s", jobInstance.getJobName()));            
            log.debug(String.format("*********** job instance Id: %d", jobInstance.getId()));
            
        }
        System.exit(0);

    }

    /**
     * set up cli options
     * 
     * @return
     */
    private static Options setupCLI() {
        Option helpOption = Option.builder("h").longOpt("help").required(false).desc("shows this message").build();

        // default target/classes/access.log
        Option fileOption = Option.builder("f").longOpt("accesslog").numberOfArgs(1).required(true).type(String.class)
                .desc("path to log file. default: target/classes/access.log").build();

        Option startDateOption = Option.builder("sD").longOpt("startDate").numberOfArgs(1).required(true)
                .type(String.class).desc("--startDate=2017-01-01.13:00:00  (start date of search) ").build();

        Option durationOption = Option.builder("dur").longOpt("duration").numberOfArgs(1).required(true)
                .type(String.class).desc("ex/ --duration=hourly (duration from startDate to search: hourly or daily)")
                .build();

        Option threshholdOption = Option.builder("th").longOpt("threshold").numberOfArgs(1).required(false)
                .type(Number.class)
                .desc("default 100 (a given IP makes more than this threshold number of requests for the given duration)")
                .build();

        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(fileOption);
        options.addOption(startDateOption);
        options.addOption(durationOption);
        options.addOption(threshholdOption);

        return options;
    }

}