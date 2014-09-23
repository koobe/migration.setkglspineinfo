package com.koobe.common.data.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;


/**
 * The persistent class for the EpubConvertSpineContent database table.
 * 
 */
@Entity
@NamedQuery(name="EpubConvertSpineContent.findAll", query="SELECT e FROM EpubConvertSpineContent e")
public class EpubConvertSpineContent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String guid;

	private String spine;

	private String tree;
	
	private String runningStatus;
	
	private Date runningTime;
	
	private String runningHost;
	
	private Long elapsedCopyFile;
	
	private Long elapsedExtract;
	
	private String exceptionExtract;
	
	private Boolean extractSuccess;
	
	private String workerExMsg;

	public EpubConvertSpineContent() {
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getSpine() {
		return this.spine;
	}

	public void setSpine(String spine) {
		this.spine = spine;
	}

	public String getTree() {
		return this.tree;
	}

	public void setTree(String tree) {
		this.tree = tree;
	}

	public String getRunningStatus() {
		return runningStatus;
	}

	public void setRunningStatus(String runningStatus) {
		this.runningStatus = runningStatus;
	}

	public Date getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(Date runningTime) {
		this.runningTime = runningTime;
	}

	public String getRunningHost() {
		return runningHost;
	}

	public void setRunningHost(String runningHost) {
		this.runningHost = runningHost;
	}

	public Long getElapsedCopyFile() {
		return elapsedCopyFile;
	}

	public void setElapsedCopyFile(Long elapsedCopyFile) {
		this.elapsedCopyFile = elapsedCopyFile;
	}

	public Long getElapsedExtract() {
		return elapsedExtract;
	}

	public void setElapsedExtract(Long elapsedExtract) {
		this.elapsedExtract = elapsedExtract;
	}

	public String getExceptionExtract() {
		return exceptionExtract;
	}

	public void setExceptionExtract(String exceptionExtract) {
		this.exceptionExtract = exceptionExtract;
	}

	public Boolean getExtractSuccess() {
		return extractSuccess;
	}

	public void setExtractSuccess(Boolean extractSuccess) {
		this.extractSuccess = extractSuccess;
	}

	public String getWorkerExMsg() {
		return workerExMsg;
	}

	public void setWorkerExMsg(String workerExMsg) {
		this.workerExMsg = workerExMsg;
	}

	

}