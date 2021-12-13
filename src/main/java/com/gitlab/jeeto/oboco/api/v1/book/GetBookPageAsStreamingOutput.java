package com.gitlab.jeeto.oboco.api.v1.book;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ImageManagerFactory;
import com.gitlab.jeeto.oboco.common.image.ScaleType;
import com.gitlab.jeeto.oboco.data.bookreader.BookReader;
import com.gitlab.jeeto.oboco.data.bookreader.BookReaderManager;
import com.gitlab.jeeto.oboco.database.book.Book;

public class GetBookPageAsStreamingOutput extends GetAsStreamingOutput {
	private static Logger logger = LoggerFactory.getLogger(GetBookPageAsStreamingOutput.class.getName());
	private Book book;
	private Integer page;
	private ScaleType scaleType;
	private Integer scaleWidth;
	private Integer scaleHeight;
	private Configuration configuration;
	
	private Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			configuration = configurationManager.getConfiguration();
		}
		return configuration;
	}
	
	public GetBookPageAsStreamingOutput(Book book, Integer page, ScaleType scaleType, Integer scaleWidth, Integer scaleHeight) {
		this.book = book;
		this.page = page;
		this.scaleType = scaleType;
		this.scaleWidth = scaleWidth;
		this.scaleHeight = scaleHeight;
	}
	
	private boolean writeBookPage(OutputStream outputStream) throws Exception {
		boolean isWritten = false;
		
		TypeableFile bookPageInputFile = getBookPage(scaleType, scaleWidth, scaleHeight);
		
		if(bookPageInputFile.isFile()) {
			write(outputStream, bookPageInputFile);
			
			isWritten = true;
		}
		
		return isWritten;
	}
	
	private boolean writeBookPage2(OutputStream outputStream) throws Exception {
		boolean isWritten = false;
		
		TypeableFile bookPageInputFile = getBookPage(null, null, null);
		
		if(bookPageInputFile.isFile()) {
			if(FileType.JPG.equals(bookPageInputFile.getFileType()) 
					&& scaleType == null 
					&& scaleWidth == null 
					&& scaleHeight == null) {
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
		}
		
		return isWritten;
	}
	
	private boolean writeBookPage3(OutputStream outputStream, BookReader bookReader) throws Exception {
		boolean isWritten = false;
		
		TypeableFile bookPageInputFile = null;
		try {
			bookPageInputFile = bookReader.getBookPage(page - 1);
			
			if(FileType.JPG.equals(bookPageInputFile.getFileType()) 
					&& scaleType == null 
					&& scaleWidth == null 
					&& scaleHeight == null) {
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
		try {
			BookReader bookReader = null;
			try {
				boolean isWritten = writeBookPage(outputStream);
				
				if(isWritten == false) {
					isWritten = writeBookPage2(outputStream);
					
					if(isWritten == false) {
						if(bookReader == null) {
							TypeableFile bookInputFile = new TypeableFile(book.getFilePath());
							
							BookReaderManager bookReaderManager = BookReaderManager.getInstance();
							
							bookReader = bookReaderManager.getBookReader();
							bookReader.openBook(bookInputFile);
						}
						
						writeBookPage3(outputStream, bookReader);
					}
				}
			} finally {
				try {
					if(bookReader != null) {
						bookReader.closeBook();
					}
				} catch(Exception e) {
					// pass
				}
			}
			
			outputStream.flush();
		} catch(Exception e) {
			logger.error("Error.", e);
			
    		throw new WebApplicationException(e, 500);
		} finally {
    		if(outputStream != null) {
    			try {
	    			outputStream.close();
    			} catch(Exception e) {
					// pass
				}
    		}
		}
	}
	
	private TypeableFile getBookPage(ScaleType scaleType, Integer scaleWidth, Integer scaleHeight) throws Exception {
    	String directoryPath = getConfiguration().getAsString("data.path", "./data");
    	
    	String bookPageFilePath = book.getFileId().substring(0, 2) + "/" + book.getFileId().substring(2) + "/" + page;
        if(scaleType != null) {
        	bookPageFilePath = bookPageFilePath + "-scaleType" + scaleType;
        }
        if(scaleWidth != null) {
        	bookPageFilePath = bookPageFilePath + "-scaleWidth" + scaleWidth;
        }
        if(scaleHeight != null) {
        	bookPageFilePath = bookPageFilePath + "-scaleHeight" + scaleHeight;
        }
        bookPageFilePath = bookPageFilePath + ".jpg";
        
        TypeableFile bookPageFile = new TypeableFile(directoryPath, bookPageFilePath);
		
		return bookPageFile;
    }
	
	private TypeableFile createBookPage(TypeableFile bookPageInputFile) throws Exception {
		ImageManagerFactory imageManagerFactory = ImageManagerFactory.getInstance();
    	ImageManager imageManager = imageManagerFactory.getImageManager(bookPageInputFile.getFileType(), FileType.JPG);
		
    	TypeableFile bookPageOutputFile = imageManager.createImage(bookPageInputFile, FileType.JPG, scaleType, scaleWidth, scaleHeight);
		
		return bookPageOutputFile;
	}
}
