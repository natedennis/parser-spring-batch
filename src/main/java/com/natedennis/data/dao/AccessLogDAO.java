package com.natedennis.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.natedennis.data.domain.AccessLog;

@Component
@Transactional
public class AccessLogDAO {

    @Autowired
    private EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(AccessLogDAO.class);

    public void bulkPersist(List<AccessLog> accessLogs, int batchSize) {
        // Create an EntityManager
        try {

            int i = 0;
            for (AccessLog a : accessLogs) {
                if (a != null) {

                    entityManager.persist(a);

                    if (i++ % batchSize == 0) {
                        entityManager.clear();
                        entityManager.flush();
                    }
                }
            }
            entityManager.flush();
//release memory
            entityManager.clear();
            // Commit the transaction
        } catch (Exception ex) {

            // Print the Exception
            logger.error("accesslog persist error: {}", ex);
        }
    }

    /**
     * Read all the AccessLog.
     * 
     * @return a List of AccessLog
     */

    public List<String> threadHoldQuery(Date startDate, Date endDate, int threshold) {

        List<String> ips = new ArrayList<>();

        try {

            // select distinct a.ip, count(a.id) from access_log a group by a.ip HAVING COUNT(a.id)>200;
            // select count(id) from access_log where ip='192.168.89.111';

            // Get a List of AccessLog
            StringBuffer q = new StringBuffer("SELECT a.ip FROM AccessLog a ");
            q.append("where a.accessDate >= :startDate and a.accessDate < :endDate ");
            q.append("GROUP BY a.ip HAVING COUNT(DISTINCT a.id) > :threshold ");
            ips = entityManager.createQuery(q.toString(), String.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("threshold", Long.valueOf(threshold))
                    .getResultList();

        } catch (Exception ex) {
            // Print the Exception
            logger.error("accesslog select error: {}", ex);
        }
        return ips;
    }

    /**
     * Read all the AccessLog.
     * 
     * @return a List of AccessLog
     */

    public Long count() {
        try {
            Long count = (long) entityManager
                    .createQuery("select count(id) from AccessLog", Long.class)
                    .getSingleResult();

            return count;
        } catch (Exception ex) {
            logger.error("accesslog select count error: {}", ex);
        }
        return 0L;
    }

    /**
     * Delete the existing Student.
     * 
     * @param id
     */

    public void cleanUp() {

        try {
            // clean up with native queries .. still jpa compliant
            entityManager.createNativeQuery("TRUNCATE access_log").executeUpdate();
            entityManager.createNativeQuery("DROP TABLE IF EXISTS access_log_filtered_copy").executeUpdate();
            entityManager.flush();

        } catch (Exception ex) {
            logger.error("accesslog delete error: {}", ex);
        }
    }

    public void copyFilterResults(Date startDate, Date endDate, int threshold) {

        List<String> ips = new ArrayList<>();

        try {

            // select distinct a.ip, count(a.id) from access_log a group by a.ip HAVING COUNT(a.id)>200;
            // select count(id) from access_log where ip='192.168.89.111';

            // Get a List of AccessLog
            // CREATE TABLE ab SELECT b.* FROM access_log b inner join
            // ( select a.ip from access_log a WHERE a.access_date >= '2017-01-01 13:00:00.0'
            // and a.access_date < '2017-01-01 14:00:00.0' GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> 1) c on b.ip=c.ip;

            StringBuffer q = new StringBuffer("CREATE TABLE access_log_filtered_copy ");
            q.append("SELECT b.* FROM access_log b inner join (select a.ip from access_log a ");
            q.append("WHERE a.access_date >= :startDate and a.access_date < :endDate ");
            q.append("GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> :threshold ) c on b.ip=c.ip "
                    + "where b.access_date >=:startDate and b.access_date < :endDate ");
            entityManager.createNativeQuery(q.toString())
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .setParameter("threshold", Long.valueOf(threshold))
                    .executeUpdate();

            // Commit the transaction
            entityManager.flush();
        } catch (Exception ex) {

            // Print the Exception
            logger.error("accesslog select error: {}", ex);
        }
    }

    /**
     * Update the existing AccessLog.
     * 
     * @param accessLog
     */

    public void merge(AccessLog accessLog) {

        try {

            // Update the AccessLog
            entityManager.merge(accessLog);
        } catch (Exception ex) {
            // Print the Exception
            logger.error("accesslog persist error: {}", ex);
        }
    }
}
