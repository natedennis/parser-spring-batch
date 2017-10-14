package com.natedennis.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;

public class LogProcessListener implements ItemProcessListener<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(LogProcessListener.class);

	@Override
    public void afterProcess(Object item, Object result) {
		if(item!=null) log.trace("Input to Processor: " + item.toString());
		if(result!=null) log.trace("Output of Processor: " + result.toString());
	}

	@Override
    public void beforeProcess(Object item) {
	}

	@Override
    public void onProcessError(Object item, Exception e) {
	}

}
