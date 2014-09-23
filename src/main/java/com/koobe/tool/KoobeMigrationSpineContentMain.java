package com.koobe.tool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.koobe.common.core.KoobeApplication;
import com.koobe.common.data.KoobeDataService;
import com.koobe.common.data.repository.EpubConvertSpineContentRepository;
import com.koobe.tool.worker.ExtractSpineContentWorker;

public class KoobeMigrationSpineContentMain {
	
	protected static Logger log = LoggerFactory.getLogger(KoobeMigrationSpineContentMain.class);
	
	static KoobeApplication koobeApplication;
	static KoobeDataService koobeDataService;
	static JdbcTemplate jdbcTemplate;
	
	static EpubConvertSpineContentRepository logRepository;
	
	static ExecutorService executor;
	
	static String SQL = "SELECT eBookGuid_Lower FROM EBookNotInKGL_NoReplica"
			+ " WHERE eBookGuid_Lower NOT IN (SELECT guid FROM EpubConvertSpineContent)"
			+ " AND eBookGuid_Lower IN (SELECT guid FROM EpubConvertResult WHERE runningStatus = 'SUCCESS')";
	
	static String SQL2 = "SELECT guid, runningStatus FROM EpubConvertSpineContent WHERE runningStatus <> 'SUCCESS'";
	
	static String SQL3 = "SELECT guid FROM EpubConvertResult WHERE runningStatus = 'SUCCESS'";
	
	static {
		koobeApplication = KoobeApplication.getInstance();
		koobeDataService = (KoobeDataService) koobeApplication.getService(KoobeDataService.class);
		jdbcTemplate = koobeDataService.getJdbcTemplate();
		logRepository = (EpubConvertSpineContentRepository) koobeDataService.getRepository(EpubConvertSpineContentRepository.class);
	}
	
	static String epubRootFolder = "Q:\\EpubRaw";

	public static void main(String[] args) {
		
		Integer pool = Integer.parseInt(args[0]);
		String host = args[1];
		epubRootFolder = args[2]; 
		
		executor = Executors.newFixedThreadPool(pool, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				return thread;
			}
		});
		
		SqlRowSet rowSet = koobeDataService.getJdbcTemplate().queryForRowSet(SQL3);
		int count = 0;
		while(rowSet.next()) {
			String guid = rowSet.getString("guid");
			ExtractSpineContentWorker worker = new ExtractSpineContentWorker(epubRootFolder, guid, logRepository);
			executor.submit(worker);
			count++;
		}
		log.info("Total {} tasks sended", count);
		
//		ExtractSpineContentWorker worker = new ExtractSpineContentWorker(epubRootFolder, "fef9e070-caff-4d46-a516-c0f6dd8cb216d", logRepository);
//		executor.submit(worker);
		
		try {
			Thread.sleep(999999999);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
