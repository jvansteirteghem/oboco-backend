package com.gitlab.jeeto.oboco.data.bookreader;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookReaderPool {
	private static Logger logger = LoggerFactory.getLogger(BookReaderPool.class.getName());
	private int size;
	private long interval;
	private long age;
	private LinkedList<BookReaderPoolEntry> pool;
	private Timer timer;
	
	public BookReaderPool(int size, long interval, long age) {
		super();
		this.size = size;
		this.interval = interval;
		this.age = age;
	}
	
	private void closeBook(BookReaderPoolEntry poolEntry) {
		logger.debug("poolEntry.bookPath=" + poolEntry.getBookPath());
		
		BookReader bookReader = poolEntry.getBookReader();
		try {
			bookReader.closeBook();
		} catch (Exception e) {
			// pass
		}
	}
	
	public synchronized void start() {
		pool = new LinkedList<BookReaderPoolEntry>();
		
		TimerTask timerTask = new TimerTask() {
			@Override
			public synchronized void run() {
				Date date = new Date(new Date().getTime() - age);
				
				logger.debug("date=" + date);
				
				Iterator<BookReaderPoolEntry> iterator = pool.iterator();
				while(iterator.hasNext()) {
					BookReaderPoolEntry nextPoolEntry = iterator.next();
					
					if(nextPoolEntry.getDate().compareTo(date) < 0) {
						logger.debug("poolEntry.date=" + nextPoolEntry.getDate());
						
						closeBook(nextPoolEntry);
						
						iterator.remove();
						
						logger.debug("removed poolEntry: pool.size=" + pool.size() + "/" + size);
					}
				}
			}
		};
		
		timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0L, interval);
	}
	
	public synchronized void stop() {
		timer.cancel();
		
		Iterator<BookReaderPoolEntry> iterator = pool.iterator();
		while(iterator.hasNext()) {
			BookReaderPoolEntry nextPoolEntry = iterator.next();
			
			closeBook(nextPoolEntry);
			
			iterator.remove();
			
			logger.debug("removed poolEntry: pool.size=" + pool.size() + "/" + size);
		}
	}
	
	public synchronized void addBookReader(String bookPath, BookReader bookReader) {
		if(pool.size() == size) {
			Iterator<BookReaderPoolEntry> iterator = pool.iterator();
			while(iterator.hasNext()) {
				BookReaderPoolEntry nextPoolEntry = iterator.next();
				
				closeBook(nextPoolEntry);
				
				iterator.remove();
				
				logger.debug("removed poolEntry: pool.size=" + pool.size() + "/" + size);
				
				break;
			}
		}
		
		BookReaderPoolEntry poolEntry = new BookReaderPoolEntry(bookPath, bookReader);
		
		pool.add(poolEntry);
		
		logger.debug("added poolEntry: pool.size=" + pool.size() + "/" + size);
	}
	
	public synchronized BookReader removeBookReader(String bookPath) {
		BookReader bookReader = null;
		
		Iterator<BookReaderPoolEntry> iterator = pool.iterator();
		while(iterator.hasNext()) {
			BookReaderPoolEntry nextPoolEntry = iterator.next();
			
			if(nextPoolEntry.getBookPath().equals(bookPath)) {
				bookReader = nextPoolEntry.getBookReader();
				
				iterator.remove();
				
				logger.debug("removed poolEntry: pool.size=" + pool.size() + "/" + size);
				
				break;
			}
		}
		
		return bookReader;
	}
}
