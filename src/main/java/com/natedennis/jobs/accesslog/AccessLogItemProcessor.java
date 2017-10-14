package com.natedennis.jobs.accesslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.natedennis.data.domain.AccessLog;

public class AccessLogItemProcessor implements ItemProcessor<AccessLog, AccessLog> {

    private static final Logger log = LoggerFactory.getLogger(AccessLogItemProcessor.class);

    @Override
    public AccessLog process(AccessLog a) throws Exception {
        log.trace("Converting (" + a + ") into (" + a + ")");
        return a;
    }

}
