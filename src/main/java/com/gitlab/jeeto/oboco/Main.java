package com.gitlab.jeeto.oboco;

import java.io.File;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static void main(String... args) {
    	ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    	Configuration configuration = configurationManager.getConfiguration();
    	
    	System.setProperty("quarkus.shutdown.timeout", "5");
    	System.setProperty("quarkus.http.port", configuration.getAsString("application.server.port", "8080"));
    	if(configuration.getAsString("application.server.ssl.port", "").equals("") == false) {
	    	System.setProperty("quarkus.http.ssl-port", configuration.getAsString("application.server.ssl.port", ""));
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-file", configuration.getAsString("application.server.ssl.keyStore.path", ""));
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-file-type", "JKS");
	    	System.setProperty("quarkus.http.ssl.certificate.key-store-password", configuration.getAsString("application.server.ssl.keyStore.password", ""));
    	}
    	System.setProperty("quarkus.log.min-level", configuration.getAsString("application.logger.rootLevel", "ERROR"));
    	System.setProperty("quarkus.log.level", configuration.getAsString("application.logger.rootLevel", "ERROR"));
    	System.setProperty("quarkus.log.category.\"com.gitlab.jeeto.oboco\".level", configuration.getAsString("application.logger.level", "INFO"));
    	System.setProperty("quarkus.log.category.\"com.gitlab.jeeto.oboco\".level", configuration.getAsString("application.logger.level", "INFO"));
    	System.setProperty("quarkus.log.console.enable", "true");
    	System.setProperty("quarkus.log.file.enable", "true");
    	System.setProperty("quarkus.log.file.path", configuration.getAsString("application.logger.path", "./logs/application.log"));
    	System.setProperty("quarkus.log.file.rotation.max-file-size", "10M");
    	System.setProperty("quarkus.log.file.rotation.max-backup-index", "10");
    	System.setProperty("quarkus.log.file.rotation.file-suffix", ".yyyy-MM-dd");
    	System.setProperty("quarkus.log.file.rotation.rotate-on-boot", "false");
		
        Quarkus.run(Application.class, args);
    }
    
    @ApplicationScoped
    public static class ApplicationEventListener {
    	private static Logger logger = LoggerFactory.getLogger(ApplicationEventListener.class.getName());
    	private ArchiveReaderFactory archiveReaderFactory;
    	@Inject
    	EntityManager entityManager;
    	
    	@Transactional
    	void onStart(@Observes StartupEvent ev) {
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
        	
        	archiveReaderFactory = ArchiveReaderFactory.getInstance();
        	archiveReaderFactory.start();
    	}
    	
    	void onStop(@Observes ShutdownEvent ev) {
    		logger.info("stop");
    		
    		archiveReaderFactory.stop();
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
