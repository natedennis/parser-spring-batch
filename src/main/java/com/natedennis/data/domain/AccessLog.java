package com.natedennis.data.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "access_log")
public class AccessLog {
	// Date, IP, Request, Status, User Agent (pipe delimited, open the example
	// file in text editor)

	private Integer id;
	private Date accessDate;
	private String ip;
	private String request;
	private Integer status;
	private String userAgent;

	
	public AccessLog() {
		
	}
	
	public AccessLog(Integer id, Date accessDate, String ip, String request, Integer status, String userAgent) {
		super();
		this.id = id;
		this.accessDate = accessDate;
		this.ip = ip;
		this.request = request;
		this.status = status;
		this.userAgent = userAgent;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "access_date", length = 19)
	public Date getAccessDate() {
		return accessDate;
	}

	public void setAccessDate(Date accessDate) {
		this.accessDate = accessDate;
	}
	
	@Column(name = "ip", length = 15)
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column(name = "request", length = 2050)
	public String getRequest() {
		return request;
	}

	

	public void setRequest(String request) {
		this.request = request;
	}

	@Column(name = "status")
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "user_agent", length = 512)
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

}
