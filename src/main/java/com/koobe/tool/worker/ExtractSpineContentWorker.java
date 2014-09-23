package com.koobe.tool.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactXmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.gson.Gson;
import com.koobe.common.crypto.UniqueID;
import com.koobe.common.data.domain.EpubConvertSpineContent;
import com.koobe.common.data.repository.EpubConvertSpineContentRepository;
import com.koobe.kgl.client.db.PageNavigator;
import com.koobe.kgl.client.db.SpineInfo;
import com.koobe.tool.worker.enums.Status;

public class ExtractSpineContentWorker implements Runnable {
	
	protected Logger log = LoggerFactory.getLogger(getClass());

	private String epubFileRoot;
	
	private String guid;
	
	private JdbcTemplate jdbcTemplate;
	
	private EpubConvertSpineContentRepository logRepository;
	
	private Long elapsedCopyFile;
	
	private Long elapsedExtract;
	
	private String workerExMsg;
	
	private Status status = Status.IDLE;

	public ExtractSpineContentWorker(String epubFileRoot, String guid, EpubConvertSpineContentRepository logRepository) {
		super();
		this.epubFileRoot = epubFileRoot;
		this.guid = guid;
		this.logRepository = logRepository;
	}

	public void run() {
		
		log.info("Extract spine content for book: {}", guid);
		
		File epubFile = null;
		File tempEpubFile = null;
		
		SpineContentExtractor extractor = null;
		
		try {
			
			String epubPath = epubFileRoot + File.separator + 
					guid.substring(0, 1) + File.separator + 
					guid.substring(1, 2) + File.separator + 
					guid.substring(2, 3) + File.separator + 
					guid + ".epub";
			
			epubFile = new File(epubPath);
			
			log.info("Epub file path: {}", epubPath);
			
			if (epubFile.exists()) {
				
				
				long start = System.currentTimeMillis();
				String tempEpubFilePath = System.getProperty("java.io.tmpdir") + guid + ".epub";
				tempEpubFile = new File(tempEpubFilePath);
				log.info("[{}] copy epub file to {}", guid, tempEpubFilePath);
				FileUtils.copyFile(epubFile, tempEpubFile);
				long elapsed = System.currentTimeMillis() - start;
				elapsedCopyFile = elapsed;
				
				log.info("[{}] extract spine content", guid);
				start = System.currentTimeMillis();
				extractor = new SpineContentExtractor(tempEpubFile, guid);
				extractor.start();
				elapsed = System.currentTimeMillis() - start;
				elapsedExtract = elapsed;
				
		        status = Status.SUCCESS;
			} else {
				status = Status.EPUBFILENOTFOUND;
			}
			
		} catch (Throwable e) {
			status = Status.FAIL;
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			workerExMsg = sw.toString();
		} finally {
			
			// delete temp file
			log.info("[{}] delete temp file {}", guid, tempEpubFile.getAbsolutePath());
			if (tempEpubFile != null) {
				try {
					FileDeleteStrategy.FORCE.delete(tempEpubFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				
				String hostName = "unknow";
				try {
					hostName = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				EpubConvertSpineContent model = new EpubConvertSpineContent();
				
				model.setGuid(guid);
				
				if (extractor != null) {
					model.setSpine(extractor.getSpineJson());
					model.setTree(extractor.getSpineInfoJson());
					model.setExceptionExtract(extractor.getExMsg());
					model.setExtractSuccess(extractor.getSuccess());
				}
				
				model.setRunningStatus(status.toString());
				model.setRunningTime(new Date());
				model.setRunningHost(hostName);

				if (elapsedCopyFile != null) {
					model.setElapsedCopyFile(elapsedCopyFile);
				}
				if (elapsedExtract != null) {
					model.setElapsedExtract(elapsedExtract);
				}
				
				model.setWorkerExMsg(workerExMsg);
				
				log.info("[{}] save execution result", guid);
				logRepository.save(model);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
