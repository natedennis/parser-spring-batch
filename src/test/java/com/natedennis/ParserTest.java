package com.natedennis;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.natedennis.data.dao.AccessLogDAO;

/**
 * Unit test for simple Parser.
 */
@RunWith(SpringRunner.class)
//@DataJpaTest
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})

@SpringBootTest(classes=Parser.class, webEnvironment = WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ParserTest {
    private final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    private static final String pattern = "yyyy-MM-dd.HH:mm:ss";// 2017-01-02.13:00:00

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccessLogDAO accessLogDAO;

    @Autowired
    private Job accessLogJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Before
    public void runBeforeEachMethod() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException,
            JobParametersInvalidException, InterruptedException {
        accessLogDAO.cleanUp();
        String file = "src/test/resources/access.log";
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("runDate", new Date())
                .addString("file", file)
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(accessLogJob, jobParameters);
        BatchStatus batchStatus = jobExecution.getStatus();
        while (batchStatus.isRunning()) {
            logger.info("*********** Still running.... **************");
            Thread.sleep(1000);
            batchStatus=jobExecution.getStatus();
        }
    }

    /**
     * Rigourous Test :-)
     * 
     * @throws ParseException
     * @throws IOException
     */
    @Test
    public void testParserAccessLog()
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException,
            JobParametersInvalidException, InterruptedException {

        Long count = 0L;
        count = (long) entityManager.createQuery("select count(a.id) from AccessLog a", Long.class).getSingleResult();
        logger.info("count " + count.toString());
        
        assertTrue(count.equals(30L));
    }

    @Test
    public void testAggregationQueryHourTest1() {
        DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusHours(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 2);
        assertTrue(t.size() == 1);
        assertTrue(t.get(0).equals("0.0.0.3"));
    }

    @Test
    public void testAggregationQueryHourTest2() {
        DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusHours(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 0);
        assertTrue(t.size() == 3);
        assertTrue(t.contains("0.0.0.1") && t.contains("0.0.0.2") && t.contains("0.0.0.3"));
    }

    @Test
    public void testAggregationQueryHourTest3() {
        DateTime dateTime = DateTime.parse("2017-01-01.02:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusHours(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 0);
        assertTrue(t.size() == 0);
    }

    @Test
    public void testAggregationQueryDayTest1() {
        DateTime dateTime = DateTime.parse("2017-01-01.02:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusDays(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 2);
        assertTrue(t.size() == 1 && t.contains("0.0.0.3"));
    }

    @Test
    public void testAggregationQueryDayTest2() {
        DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusDays(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 2);
        assertTrue(t.size() == 1 && t.contains("0.0.0.3"));
    }

    @Test
    public void testAggregationQueryDayTest3() {
        DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusDays(1).toDate();
        List<String> t = accessLogDAO.threadHoldQuery(startDate, endDate, 1);
        assertTrue(t.size() == 2 && t.contains("0.0.0.2") && t.contains("0.0.0.3"));
    }

    @Test
//    @Ignore
    @SuppressWarnings("unchecked")
    public void testCopyAccessLog() {
        DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
        Date startDate = dateTime.toDate();
        DateTime endCal = new DateTime(startDate);
        Date endDate = endCal.plusDays(1).toDate();
        accessLogDAO.copyFilterResults(startDate, endDate, 1);

        List<String> ips = entityManager.createNativeQuery("select distinct a.ip from access_log_filtered_copy a").getResultList();
        assertTrue(ips.size() == 2 && ips.contains("0.0.0.2") && ips.contains("0.0.0.3"));

        Long countAccessLog = (long) entityManager.createQuery("select count(a.id) from AccessLog a where a.ip in :ips "
                + "and a.accessDate >= :startDate and a.accessDate < :endDate ")
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("ips", ips)
                .getSingleResult();

        BigInteger countCopy = (BigInteger) entityManager.createNativeQuery("select count(a.id) from access_log_filtered_copy a"
                + " where a.ip in (:ips)")
                .setParameter("ips", ips)
                .getSingleResult();

        assertTrue(countAccessLog.equals(countCopy.longValue()));
    }

}
