package com.gitlab.jeeto.oboco.api.v1.book;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderFactory;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ImageManagerFactory;

public class GetBookAsStreamingOutput extends GetAsStreamingOutput {
	private static Logger logger = LoggerFactory.getLogger(GetBookAsStreamingOutput.class.getName());
	private Book book;
	private Configuration configuration;
	
	private Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			configuration = configurationManager.getConfiguration();
		}
		return configuration;
	}
	
	public GetBookAsStreamingOutput(Book book) {
		this.book = book;
	}
	
	private boolean writeBookPage(OutputStream outputStream, Integer page) throws Exception {
		boolean isWritten = false;
		
		TypeableFile bookPageInputFile = getBookPage(page);
		
		if(bookPageInputFile.isFile()) {
			write(outputStream, bookPageInputFile);
			
			isWritten = true;
		}
		
		return isWritten;
	}
	
	private boolean writeBookPage2(OutputStream outputStream, Integer page, ArchiveReader archiveReader) throws Exception {
		boolean isWritten = false;
		
		TypeableFile bookPageInputFile = null;
		try {
			bookPageInputFile = archiveReader.readFile(page - 1);
			
			if(FileType.JPG.equals(bookPageInputFile.getFileType())) {
				write(outputStream, bookPageInputFile);
				
				isWritten = true;
			} else {
				TypeableFile bookPageInputFile2 = null;
				try {
					bookPageInputFile2 = createBookPage(bookPageInputFile);
					
					write(outputStream, bookPageInputFile2);
					
					isWritten = true;
				} finally {
					try {
						if(bookPageInputFile2 != null) {
							if(bookPageInputFile2.isFile()) {
								bookPageInputFile2.delete();
							}
						}
					} catch(Exception e) {
						// pass
					}
				}
			}
		} finally {
			try {
				if(bookPageInputFile != null) {
					if(bookPageInputFile.isFile()) {
						bookPageInputFile.delete();
					}
				}
			} catch(Exception e) {
				// pass
			}
		}
		
		return isWritten;
	}
	
	@Override
	public void write(OutputStream outputStream) throws IOException, WebApplicationException {
		ZipOutputStream zipOutputStream = null;
		try {
			zipOutputStream = new ZipOutputStream(outputStream);
			
	    	ArchiveReader archiveReader = null;
			try {
				for(Integer page = 1; page <= book.getNumberOfPages(); page = page + 1) {
					ZipEntry zipEntry = new ZipEntry(page + ".jpg");
					
					zipOutputStream.putNextEntry(zipEntry);
					
					boolean isWritten = writeBookPage(zipOutputStream, page);
					
					if(isWritten == false) {
						if(archiveReader == null) {
							TypeableFile bookInputFile = new TypeableFile(book.getFilePath());
							
							ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
							
							archiveReader = archiveReaderFactory.getArchiveReader(bookInputFile.getFileType());
							archiveReader.openArchive(bookInputFile);
						}
						
						writeBookPage2(zipOutputStream, page, archiveReader);
					}
					
					zipOutputStream.closeEntry();
				}
			} finally {
				try {
					if(archiveReader != null) {
						archiveReader.closeArchive();
					}
				} catch(Exception e) {
					// pass
				}
			}
			
			zipOutputStream.flush();
		} catch(Exception e) {
			logger.error("Error.", e);
			
    		throw new WebApplicationException(e, 500);
		} finally {
    		if(zipOutputStream != null) {
    			try {
    				zipOutputStream.close();
    			} catch(Exception e) {
					// pass
				}
    		}
		}
	}
	
	private TypeableFile getBookPage(Integer page) throws Exception {
    	String directoryPath = getConfiguration().getAsString("data.path", "./data");
    	
    	String bookPageFilePath = book.getFileId().substring(0, 2) + "/" + book.getFileId().substring(2) + "/" + page + ".jpg";
        
    	TypeableFile bookPageFile = new TypeableFile(directoryPath, bookPageFilePath);
		
		return bookPageFile;
    }
	
	private TypeableFile createBookPage(TypeableFile bookPageInputFile) throws Exception {
		ImageManagerFactory imageManagerFactory = ImageManagerFactory.getInstance();
    	ImageManager imageManager = imageManagerFactory.getImageManager(bookPageInputFile.getFileType(), FileType.JPG);
		
    	TypeableFile bookPageOutputFile = imageManager.createImage(bookPageInputFile, FileType.JPG);
		
		return bookPageOutputFile;
	}
}
