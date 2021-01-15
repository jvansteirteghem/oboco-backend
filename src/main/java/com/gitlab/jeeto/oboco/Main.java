package com.gitlab.jeeto.oboco;

import java.io.File;
import java.util.Scanner;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.api.v1.bookscanner.BookScannerService;
import com.gitlab.jeeto.oboco.api.v1.bookscanner.BookScannerServiceStatus;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderFactory;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {
	private static Configuration configuration;
	
	public static Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			
			configuration = configurationManager.getConfiguration();
		}
		
		return configuration;
	}
	
    public static void main(String... args) {
    	for(String arg: args) {
    		int index = arg.indexOf('=');
			if (index != -1) {
			    String key = arg.substring(0, index);
			    String value = arg.substring(index + 1);
			    
			    getConfiguration().set(key, value);
			}
    	}
    	
    	System.setProperty("quarkus.shutdown.timeout", "5");
    	System.setProperty("quarkus.http.port", getConfiguration().getAsString("application.server.port", "8080"));
    	if(getConfiguration().getAsString("application.server.ssl.port", "").equals("") == false) {
	    	System.setProperty("quarkus.http.ssl-port", getConfiguration().getAsString("application.server.ssl.port", ""));
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-file", getConfiguration().getAsString("application.server.ssl.keyStore.path", ""));
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-file-type", "JKS");
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-password", getConfiguration().getAsString("application.server.ssl.keyStore.password", ""));
    	}
    	System.setProperty("quarkus.log.min-level", getConfiguration().getAsString("application.logger.rootLevel", "ERROR"));
    	System.setProperty("quarkus.log.level", getConfiguration().getAsString("application.logger.rootLevel", "ERROR"));
    	System.setProperty("quarkus.log.category.\"com.gitlab.jeeto.oboco\".level", getConfiguration().getAsString("application.logger.level", "INFO"));
    	System.setProperty("quarkus.log.category.\"com.gitlab.jeeto.oboco\".level", getConfiguration().getAsString("application.logger.level", "INFO"));
    	System.setProperty("quarkus.log.console.enable", "true");
    	System.setProperty("quarkus.log.file.enable", "true");
    	System.setProperty("quarkus.log.file.path", getConfiguration().getAsString("application.logger.path", "./logs/application.log"));
    	System.setProperty("quarkus.log.file.rotation.max-file-size", "10M");
    	System.setProperty("quarkus.log.file.rotation.max-backup-index", "10");
    	System.setProperty("quarkus.log.file.rotation.file-suffix", ".yyyy-MM-dd");
    	System.setProperty("quarkus.log.file.rotation.rotate-on-boot", "false");
    	System.setProperty("quarkus.datasource.jdbc.url", getConfiguration().getAsString("application.database.url", ""));
    	System.setProperty("quarkus.datasource.username", getConfiguration().getAsString("application.database.user.name", ""));
    	System.setProperty("quarkus.datasource.password", getConfiguration().getAsString("application.database.user.password", ""));
		
        Quarkus.run(Application.class, args);
    }
    
    @ApplicationScoped
    public static class DatabaseEventListener {
    	private static Logger logger = LoggerFactory.getLogger(DatabaseEventListener.class.getName());
    	@Inject
    	EntityManager entityManager;
    	
    	@Transactional
    	void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION) StartupEvent ev) {
    		logger.info("start");
    		
    		File ddlFile = new File("database.ddl");
    		
    		if(ddlFile.isFile()) {
    			try {
    				Scanner scanner = new Scanner(ddlFile);
    				while(scanner.hasNextLine()) {
    					String nativeQuery = scanner.nextLine();
    					if(nativeQuery.equals("") == false) {
    						entityManager.createNativeQuery(nativeQuery).executeUpdate();
    					}
    				}
    				scanner.close();
    			} catch(Exception e) {
    				logger.error("error", e);
    			}
    			
    			ddlFile.delete();
    		}
    		
    		File sqlFile = new File("database.sql");
			
			if(sqlFile.isFile()) {
				try {
    				Scanner scanner = new Scanner(sqlFile);
    				while(scanner.hasNextLine()) {
    					String nativeQuery = scanner.nextLine();
    					if(nativeQuery.equals("") == false) {
    						entityManager.createNativeQuery(nativeQuery).executeUpdate();
    					}
    				}
    				scanner.close();
    			} catch(Exception e) {
    				logger.error("error", e);
    			}
				
				sqlFile.delete();
			}
    	}
    	
    	@Transactional
    	void onStop(@Observes @Priority(Interceptor.Priority.APPLICATION) ShutdownEvent ev) {
    		logger.info("stop");
    	}
    }
    
    @ApplicationScoped
    public static class ArchiveReaderFactoryEventListener {
    	private static Logger logger = LoggerFactory.getLogger(ArchiveReaderFactoryEventListener.class.getName());
    	private ArchiveReaderFactory archiveReaderFactory;
    	
    	void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 10) StartupEvent ev) {
    		logger.info("start");
        	
        	archiveReaderFactory = ArchiveReaderFactory.getInstance();
        	archiveReaderFactory.start();
    	}
    	
    	void onStop(@Observes @Priority(Interceptor.Priority.APPLICATION + 10) ShutdownEvent ev) {
    		logger.info("stop");
    		
    		archiveReaderFactory.stop();
    	}
    }
    
    @ApplicationScoped
    public static class BookScannerServiceEventListener {
    	private static Logger logger = LoggerFactory.getLogger(BookScannerServiceEventListener.class.getName());
    	@Inject
    	Instance<BookScannerService> bookScannerServiceProvider;
    	@Inject
    	ManagedExecutor bookScannerServiceExecuter;
    	
    	void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 20) StartupEvent ev) {
    		logger.info("start");
        	
        	String bookScannerServiceId = getConfiguration().getAsString("start", "");
        	
        	if(bookScannerServiceId.equals("") == false) {
	        	BookScannerService bookScannerService = bookScannerServiceProvider.select(NamedLiteral.of(bookScannerServiceId)).get();
	        	if(bookScannerService.getStatus().equals(BookScannerServiceStatus.STOPPED)) {
	        		bookScannerServiceExecuter.submit(new Runnable() {
						@Override
						public void run() {
							try {
			        			bookScannerService.start();
			        		} catch(Exception e) {
			        			logger.error("error", e);
			        		}
						}
	        		});
	        	}
        	}
    	}
    	
    	void onStop(@Observes @Priority(Interceptor.Priority.APPLICATION + 20) ShutdownEvent ev) {
    		logger.info("stop");
    		
    		for(BookScannerService bookScannerService: bookScannerServiceProvider) {
	    		if(bookScannerService.getStatus().equals(BookScannerServiceStatus.STARTING) || bookScannerService.getStatus().equals(BookScannerServiceStatus.STARTED)) {
	    			try {
	        			bookScannerService.stop();
	        		} catch(Exception e) {
	        			logger.error("error", e);
	        		}
	    		}
    		}
    	}
    }
    
    public static class Application implements QuarkusApplication {
        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            
            return 0;
        }
    }
}
