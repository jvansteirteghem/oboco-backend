package com.gitlab.jeeto.oboco.api.v1.book;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileWrapper;
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
	
	@Override
	public void write(OutputStream outputStream) throws IOException, WebApplicationException {
		ZipOutputStream zipOutputStream = null;
		try {
			zipOutputStream = new ZipOutputStream(outputStream);
			
			File bookInputFile = new File(book.getFilePath());
			FileType bookInputFileType = FileType.getFileType(bookInputFile);
			
			FileWrapper<File> bookInputFileWrapper = new FileWrapper<File>(bookInputFile, bookInputFileType);
			
			ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
	    	ArchiveReader archiveReader = null;
			try {
				for(Integer page = 1; page <= book.getNumberOfPages(); page = page + 1) {
					FileWrapper<File> bookPageInputFileWrapper = createBookPageFileWrapper(page);
					File bookPageInputFile = bookPageInputFileWrapper.getFile();
					
					if(bookPageInputFile.isFile()) {
						ZipEntry zipEntry = new ZipEntry(page + bookPageInputFileWrapper.getFileType().getExtension());
						
						zipOutputStream.putNextEntry(zipEntry);
						
						write(zipOutputStream, bookPageInputFileWrapper);
						
						zipOutputStream.closeEntry();
					} else {
						if(archiveReader == null) {
							archiveReader = archiveReaderFactory.getArchiveReader(bookInputFileWrapper.getFileType());
							archiveReader.openArchive(bookInputFileWrapper);
						}
						
						FileWrapper<File> bookPageInputFileWrapper2 = null;
						try {
							bookPageInputFileWrapper2 = archiveReader.readFile(page - 1);
							
							ZipEntry zipEntry = new ZipEntry(page + bookPageInputFileWrapper2.getFileType().getExtension());
							
							zipOutputStream.putNextEntry(zipEntry);
							
							if(FileType.JPG.equals(bookPageInputFileWrapper2.getFileType())) {
								write(zipOutputStream, bookPageInputFileWrapper2);
							} else {
								FileWrapper<File> bookPageInputFileWrapper3 = null;
								try {
									bookPageInputFileWrapper3 = createBookPage(bookPageInputFileWrapper2);
									
									write(zipOutputStream, bookPageInputFileWrapper3);
								} finally {
									try {
										if(bookPageInputFileWrapper3 != null) {
											File bookPageOutputFile3 = bookPageInputFileWrapper3.getFile();
											if(bookPageOutputFile3.isFile()) {
												bookPageOutputFile3.delete();
											}
										}
									} catch(Exception e) {
										// pass
									}
								}
							}
							
							zipOutputStream.closeEntry();
						} finally {
							try {
								if(bookPageInputFileWrapper2 != null) {
									File bookPageInputFile2 = bookPageInputFileWrapper2.getFile();
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
		} catch (Exception e) {
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
	
	private FileWrapper<File> createBookPageFileWrapper(Integer page) throws Exception {
    	String directoryPath = getConfiguration().getAsString("data.path", "./data");
    	
    	String bookPageFilePath = book.getFileId().substring(0, 2) + "/" + book.getFileId().substring(2) + "/" + page + ".jpg";
        
		File bookPageFile = new File(directoryPath, bookPageFilePath);
		FileType bookPageFileType = FileType.JPG;
		
		FileWrapper<File> bookPageFileWrapper = new FileWrapper<File>(bookPageFile, bookPageFileType);
		
		return bookPageFileWrapper;
    }
	
	private FileWrapper<File> createBookPage(FileWrapper<File> bookPageInputFileWrapper) throws Exception {
		ImageManagerFactory imageManagerFactory = ImageManagerFactory.getInstance();
    	ImageManager imageManager = imageManagerFactory.getImageManager(bookPageInputFileWrapper.getFileType(), FileType.JPG);
		
    	FileWrapper<File> bookPageOutputFileWrapper = imageManager.createImage(bookPageInputFileWrapper, FileType.JPG);
		
		return bookPageOutputFileWrapper;
	}
}
