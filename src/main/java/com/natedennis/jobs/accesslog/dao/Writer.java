package com.natedennis.jobs.accesslog.dao;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.natedennis.data.domain.AccessLog;

public class Writer implements ItemWriter<AccessLog> {

    @Autowired
    private EntityManager entityManager;

    int batchSize = 500;

    @Override
    public void write(List<? extends AccessLog> items) throws Exception {
        int i = 0;
        for (AccessLog a : items) {
            if (a != null) {

                entityManager.persist(a);
                if (i++ % batchSize == 0) {
                    entityManager.flush();
                }
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}
