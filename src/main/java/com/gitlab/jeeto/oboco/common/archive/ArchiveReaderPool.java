package com.gitlab.jeeto.oboco.common.archive;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveReaderPool {
	private static Logger logger = LoggerFactory.getLogger(ArchiveReaderPool.class.getName());
	private int size;
	private long interval;
	private long age;
	private LinkedList<ArchiveReaderPoolEntry> pool;
	private Timer timer;
	
	public ArchiveReaderPool(int size, long interval, long age) {
		super();
		this.size = size;
		this.interval = interval;
		this.age = age;
	}
	
	private void closeArchive(ArchiveReaderPoolEntry poolEntry) {
		logger.debug("closeArchive: " + poolEntry.getArchivePath());
		
		try {
			ArchiveReader archiveReader = poolEntry.getArchiveReader();
			archiveReader.closeArchive();
		} catch (Exception e) {
			logger.error("Error.", e);
		}
	}
	
	public synchronized void start() {
		pool = new LinkedList<ArchiveReaderPoolEntry>();
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public synchronized void run() {
				Date date = new Date(new Date().getTime() - age);
				
				logger.debug("timerTask: date=" + date);
				
				Iterator<ArchiveReaderPoolEntry> iterator = pool.iterator();
				while(iterator.hasNext()) {
					ArchiveReaderPoolEntry nextPoolEntry = iterator.next();
					
					if(nextPoolEntry.getDate().compareTo(date) < 0) {
						closeArchive(nextPoolEntry);
						
						iterator.remove();
						
						logger.debug("timerTask: poolEntry.date=" + nextPoolEntry.getDate());
						logger.debug("timerTask: removed poolEntry, pool.size=" + pool.size() + "/" + size);
					}
				}
			}
		};
		
		timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0L, interval);
	}
	
	public synchronized void stop() {
		timer.cancel();
		
		Iterator<ArchiveReaderPoolEntry> iterator = pool.iterator();
		while(iterator.hasNext()) {
			ArchiveReaderPoolEntry nextPoolEntry = iterator.next();
			
			closeArchive(nextPoolEntry);
			
			iterator.remove();
			
			logger.debug("stop: removed poolEntry, pool.size=" + pool.size() + "/" + size);
		}
	}
	
	public synchronized void addArchiveReader(String archivePath, ArchiveReader archiveReader) {
		if(pool.size() == size) {
			Iterator<ArchiveReaderPoolEntry> iterator = pool.iterator();
			while(iterator.hasNext()) {
				ArchiveReaderPoolEntry nextPoolEntry = iterator.next();
				
				closeArchive(nextPoolEntry);
				
				iterator.remove();
				
				logger.debug("addArchiveReader: removed poolEntry, pool.size=" + pool.size() + "/" + size);
				
				break;
			}
		}
		
		ArchiveReaderPoolEntry poolEntry = new ArchiveReaderPoolEntry(archivePath, archiveReader);
		
		pool.add(poolEntry);
		
		logger.debug("addArchiveReader: added poolEntry, pool.size=" + pool.size() + "/" + size);
	}
	
	public synchronized ArchiveReader removeArchiveReader(String archivePath) {
		ArchiveReader archiveReader = null;
		
		Iterator<ArchiveReaderPoolEntry> iterator = pool.iterator();
		while(iterator.hasNext()) {
			ArchiveReaderPoolEntry nextPoolEntry = iterator.next();
			
			if(nextPoolEntry.getArchivePath().equals(archivePath)) {
				archiveReader = nextPoolEntry.getArchiveReader();
				
				iterator.remove();
				
				logger.debug("removeArchiveReader: removed poolEntry, pool.size=" + pool.size() + "/" + size);
				
				break;
			}
		}
		
		return archiveReader;
	}
}
