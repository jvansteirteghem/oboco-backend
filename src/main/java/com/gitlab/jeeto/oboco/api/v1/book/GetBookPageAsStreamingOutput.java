package com.gitlab.jeeto.oboco.api.v1.book;

import java.io.IOException;
import java.io.OutputStream;

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
import com.gitlab.jeeto.oboco.common.image.ScaleType;

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
	
	@Override
	public void write(OutputStream outputStream) throws IOException, WebApplicationException {
		try {
			TypeableFile bookPageInputFile = createBookPageFile();
			
			if(bookPageInputFile.isFile()) {
				write(outputStream, bookPageInputFile);
			} else {
				TypeableFile bookInputFile = new TypeableFile(book.getFilePath());
				
				ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
		    	ArchiveReader archiveReader = null;
				try {
					archiveReader = archiveReaderFactory.getArchiveReader(bookInputFile.getFileType());
					archiveReader.openArchive(bookInputFile);
					
					TypeableFile bookPageInputFile2 = null;
					try {
						bookPageInputFile2 = archiveReader.readFile(page - 1);
						
						if(FileType.JPG.equals(bookPageInputFile2.getFileType()) 
								&& scaleType == null 
								&& scaleWidth == null 
								&& scaleHeight == null) {
							write(outputStream, bookPageInputFile2);
						} else {
							TypeableFile bookPageInputFile3 = null;
							try {
								bookPageInputFile3 = createBookPage(bookPageInputFile2);
								
								write(outputStream, bookPageInputFile3);
							} finally {
								try {
									if(bookPageInputFile3 != null) {
										if(bookPageInputFile3.isFile()) {
											bookPageInputFile3.delete();
										}
									}
								} catch(Exception e) {
									// pass
								}
							}
						}
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
				} finally {
					try {
						if(archiveReader != null) {
							archiveReader.closeArchive();
						}
					} catch(Exception e) {
						// pass
					}
				}
			}
			
			outputStream.flush();
		} catch (Exception e) {
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
	
	private TypeableFile createBookPageFile() throws Exception {
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
