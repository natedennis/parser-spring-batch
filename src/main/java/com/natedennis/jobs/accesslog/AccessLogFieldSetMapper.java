package com.natedennis.jobs.accesslog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.natedennis.data.domain.AccessLog;

public class AccessLogFieldSetMapper implements FieldSetMapper<AccessLog> {
    private final Logger logger = LoggerFactory.getLogger(AccessLogFieldSetMapper.class);

	@Override
	public AccessLog mapFieldSet(FieldSet fieldSet) throws BindException {
		
	    AccessLog a = new AccessLog();
        try {
            a.setAccessDate(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .parse(fieldSet.readString("ACCESS_DATE")));
        } catch (ParseException e) {
            logger.error("error parsing date:{}",e);
        }
        a.setIp(fieldSet.readRawString("IP"));
        a.setRequest(fieldSet.readRawString("REQUEST"));
		a.setStatus(fieldSet.readInt("STATUS"));
		a.setUserAgent(fieldSet.readRawString("USER_AGENT"));
	
		return a;
	}
	

}
