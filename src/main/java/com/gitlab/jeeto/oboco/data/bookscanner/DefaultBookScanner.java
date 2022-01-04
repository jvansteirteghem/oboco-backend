package com.gitlab.jeeto.oboco.data.bookscanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FactoryManager;
import com.gitlab.jeeto.oboco.common.FileHelper;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;
import com.gitlab.jeeto.oboco.common.hash.Hash;
import com.gitlab.jeeto.oboco.common.hash.HashFactory;
import com.gitlab.jeeto.oboco.common.hash.HashType;
import com.gitlab.jeeto.oboco.data.DateHelper;
import com.gitlab.jeeto.oboco.data.NameHelper;
import com.gitlab.jeeto.oboco.data.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.data.book.BookReader;
import com.gitlab.jeeto.oboco.data.book.BookReaderFactory;
import com.gitlab.jeeto.oboco.data.book.BookType;
import com.gitlab.jeeto.oboco.data.bookpage.BookPageConfiguration;
import com.gitlab.jeeto.oboco.data.bookpage.BookPageHelper;
import com.gitlab.jeeto.oboco.data.bookpage.BookPageType;
import com.gitlab.jeeto.oboco.data.bookpage.ScaleConfiguration;
import com.gitlab.jeeto.oboco.data.bookpage.ScaleType;
import com.gitlab.jeeto.oboco.database.book.Book;
import com.gitlab.jeeto.oboco.database.book.BookService;
import com.gitlab.jeeto.oboco.database.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.database.bookcollection.BookCollectionService;
import com.gitlab.jeeto.oboco.database.bookmark.BookMarkService;
import com.gitlab.jeeto.oboco.problem.Problem;
import com.gitlab.jeeto.oboco.problem.ProblemException;

@ApplicationScoped
@Named("DEFAULT")
public class DefaultBookScanner implements BookScanner {
	private static Logger logger = LoggerFactory.getLogger(DefaultBookScanner.class.getName());
	@Inject
	BookMarkService bookMarkService;
	@Inject
	BookService bookService;
	@Inject
	BookCollectionService bookCollectionService;
	private Configuration configuration;
	
	private Configuration getConfiguration() {
		if(configuration == null) {
			ConfigurationManager configurationManager = ConfigurationManager.getInstance();
			configuration = configurationManager.getConfiguration();
		}
		return configuration;
	}
	
	private String id;
	private BookScannerMode mode;
	private BookScannerStatus status;
	private Date updateDate;
	private List<BookPageConfiguration> defaultBookPageConfigurationList;
	
	public DefaultBookScanner() {
		super();
		this.id = "DEFAULT";
		this.mode = null;
		this.status = BookScannerStatus.STOPPED;
		this.updateDate = null;
		this.defaultBookPageConfigurationList = new ArrayList<BookPageConfiguration>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public BookScannerMode getMode() {
		return mode;
	}
	
	public BookScannerStatus getStatus() {
		return this.status;
	}
	
	private File getDirectory() throws ProblemException {
		String directoryPath = getConfiguration().getAsString("data.path", "./data");
    	
		File directory = new File(directoryPath);
    	
    	if(directory.isDirectory() == false) {
    		throw new ProblemException(new Problem(500, "PROBLEM", "The directory is invalid: " + directory.getAbsolutePath()));
    	}
    	
    	return directory;
	}
	
	private Map<String, List<File>> getDirectoryMap() throws ProblemException {
		try {
			Map<String, List<File>> directoryMap = new LinkedHashMap<String, List<File>>();
			
			Properties dataProperties = new Properties();
	    	dataProperties.load(new FileInputStream("./data.properties"));
			
			for(Entry<Object, Object> entry: dataProperties.entrySet()) {
				String name = entry.getKey().toString();
				String directoryPathsString = entry.getValue().toString();
				
				if(directoryPathsString == null) {
					directoryPathsString = "";
				}
				
				String[] directoryPaths = directoryPathsString.split(",");
				
				List<File> directoryList = new ArrayList<File>();
				
				for(String directoryPath: directoryPaths) {
					directoryPath = directoryPath.trim();
					
					File directory = new File(directoryPath);
					
					if(directory.isDirectory() == false) {
			    		throw new ProblemException(new Problem(500, "PROBLEM", "The directory is invalid: " + directory.getAbsolutePath()));
			    	}
			        
					directoryList.add(directory);
				}
				
				directoryMap.put(name, directoryList);
			}
			
			return directoryMap;
		} catch(ProblemException e) {
			logger.error("Error.", e);
			
			throw e;
		} catch(Exception e) {
			logger.error("Error.", e);
			
			throw new ProblemException(new Problem(500, "PROBLEM", "Problem."), e);
		}
	}
	
	public void start(BookScannerMode mode) throws ProblemException {
		this.mode = mode;
		this.status = BookScannerStatus.STARTING;
		this.updateDate = DateHelper.getDate();
		this.status = BookScannerStatus.STARTED;
		try {
			this.defaultBookPageConfigurationList = BookPageHelper.getBookPageConfigurations();
	    	
			// validate directory
	    	getDirectory();
	    	
	    	Map<String, List<File>> directoryMap = getDirectoryMap();
			
	    	Integer number = 1;
			for(Entry<String, List<File>> entry: directoryMap.entrySet()) {
				String name = entry.getKey();
				List<File> directoryList = entry.getValue();
				
				BookCollection bookCollection = bookCollectionService.getRootBookCollection(name);
				
				if(bookCollection == null) {
					logger.info("create rootBookCollection " + name);
					
					bookCollection = new BookCollection();
					
					try {
						processRootBookCollection(name, bookCollection, BookScannerMode.CREATE);
					} catch(Exception e) {
						logger.error("error create rootBookCollection " + name, e);
						
						continue;
					}
					
					bookCollection.setDirectoryPath("");
					bookCollection.setRootBookCollection(null);
					bookCollection.setParentBookCollection(null);
					bookCollection.setCreateDate(this.updateDate);
					bookCollection.setUpdateDate(this.updateDate);
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					bookCollection.setNumberOfBookPages(0);
					bookCollection.setNumber(number);
			        
			        bookCollection = bookCollectionService.createBookCollection(bookCollection);
				} else {
					logger.info("update rootBookCollection " + name);
					
					try {
						processRootBookCollection(name, bookCollection, this.mode);
					} catch(Exception e) {
						logger.error("error update rootBookCollection " + name, e);
						
						continue;
					}
					
					bookCollection.setUpdateDate(this.updateDate);
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					bookCollection.setNumberOfBookPages(0);
					bookCollection.setNumber(number);
					
					bookCollection = bookCollectionService.updateBookCollection(bookCollection);
				}
				
				for(File directory: directoryList) {
				    number = add(number, bookCollection, bookCollection, directory);
				}
				
				if(BookScannerStatus.STOPPING.equals(this.status)) {
		    		logger.info("stopping!");
		    		
		    		return;
				}
	    	}
	        
	        logger.info("delete books");
	        
	        bookService.deleteBooks(this.updateDate);
	        
	        logger.info("delete bookCollections");
	        
	        bookCollectionService.deleteBookCollections(this.updateDate);
	        
	        deleteBookPageByUpdateDate();
		} catch(ProblemException e) {
			logger.error("Error.", e);
			
			throw e;
		} catch(Exception e) {
			logger.error("Error.", e);
			
			throw new ProblemException(new Problem(500, "PROBLEM", "Problem."), e);
		} finally {
			this.status = BookScannerStatus.STOPPED;
			this.updateDate = null;
		}
    }
	
	public void stop() throws ProblemException {
		logger.info("stopping..");
		this.status = BookScannerStatus.STOPPING;
	}
    
	private Integer add(Integer number, BookCollection rootBookCollection, BookCollection parentBookCollection, File parentFile) throws Exception {
		Integer numberOfBookCollections = parentBookCollection.getNumberOfBookCollections();
		Integer numberOfBooks = parentBookCollection.getNumberOfBooks();
		Integer numberOfBookPages = parentBookCollection.getNumberOfBookPages();
		
		File[] files = parentFile.listFiles();
    	
    	List<File> fileList = Arrays.asList(files);
    	fileList.sort(new NaturalOrderComparator<File>() {
    		@Override
    		public String toString(File o) {
				return o.getName();
		   }
    	});
    	
		for(File file: fileList) {
			if(BookScannerStatus.STOPPING.equals(this.status)) {
	    		logger.info("stopping!");
	    		
	    		return number;
			}
			
			String path = file.getPath();
			
			if(file.isDirectory()) {
				BookCollection bookCollection = bookCollectionService.getBookCollectionByRootBookCollectionAndDirectory(rootBookCollection.getId(), path);
				BookCollection bookCollectionUpdate = bookCollectionService.getBookCollectionByDirectory(path, this.updateDate);
				
				if(bookCollection == null) {
					logger.info("create bookCollection " + path);
					
					bookCollection = new BookCollection();
					
					try {
						processBookCollection(file, bookCollection, bookCollectionUpdate, BookScannerMode.CREATE);
					} catch(Exception e) {
						logger.error("error create bookCollection " + path, e);
						
						continue;
					}
					
					bookCollection.setDirectoryPath(file.getPath());
					bookCollection.setRootBookCollection(rootBookCollection);
					bookCollection.setParentBookCollection(parentBookCollection);
					bookCollection.setCreateDate(this.updateDate);
					bookCollection.setUpdateDate(this.updateDate);
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					bookCollection.setNumberOfBookPages(0);
					
					numberOfBookCollections = numberOfBookCollections + 1;
					number = number + 1;
					
					bookCollection.setNumber(number);
			        
			        bookCollection = bookCollectionService.createBookCollection(bookCollection);
				} else {
					logger.info("update bookCollection " + path);
					
					BookScannerMode mode = this.mode;
					
					if(BookScannerMode.UPDATE.equals(mode)) {
						Date fileUpdateDate = new Date(file.lastModified());
						
						if(bookCollection.getUpdateDate().compareTo(fileUpdateDate) < 0) {
							mode = BookScannerMode.CREATE;
						}
					}
					
					try {
						processBookCollection(file, bookCollection, bookCollectionUpdate, mode);
					} catch(Exception e) {
						logger.error("error update bookCollection " + path, e);
						
						continue;
					}
					
					bookCollection.setUpdateDate(this.updateDate);
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					bookCollection.setNumberOfBookPages(0);
					
					numberOfBookCollections = numberOfBookCollections + 1;
					number = number + 1;
					
					bookCollection.setNumber(number);
					
					bookCollection = bookCollectionService.updateBookCollection(bookCollection);
				}
				
				number = add(number, rootBookCollection, bookCollection, file);
			} else {
				BookType bookType = BookType.getBookType(file);
				
				if(bookType != null) {
					Book book = bookService.getBookByRootBookCollectionAndFile(rootBookCollection.getId(), path);
					Book bookUpdate = bookService.getBookByFile(path, this.updateDate);
					
					if(book == null) {
						logger.info("create book " + path);
						
						book = new Book();
						
				    	try {
							processBook(file, bookType, book, bookUpdate, BookScannerMode.CREATE);
							
							processBookPages(file, bookType, book, bookUpdate, BookScannerMode.CREATE);
						} catch(Exception e) {
							logger.error("error create book " + path, e);
							
							continue;
						}
				    	
				    	if(book.getNumberOfPages() == null || book.getNumberOfPages() == 0) {
				    		continue;
				    	}
				    	
				    	book.setFilePath(file.getPath());
				    	book.setRootBookCollection(rootBookCollection);
				    	book.setBookCollection(parentBookCollection);
						book.setCreateDate(this.updateDate);
				    	book.setUpdateDate(this.updateDate);
						
						numberOfBooks = numberOfBooks + 1;
						numberOfBookPages = numberOfBookPages + book.getNumberOfPages();
						number = number + 1;
						
						book.setNumber(number);
						
						book = bookService.createBook(book);
						
						bookMarkService.createBookMarkReferencesByBook(book, this.updateDate);
					} else {
						logger.info("update book " + path);
						
						BookScannerMode mode = this.mode;
						
						if(BookScannerMode.UPDATE.equals(mode)) {
							Date fileUpdateDate = new Date(file.lastModified());
							
							if(book.getUpdateDate().compareTo(fileUpdateDate) < 0) {
								mode = BookScannerMode.CREATE;
							}
						}
						
						try {
							processBook(file, bookType, book, bookUpdate, mode);
							
							processBookPages(file, bookType, book, bookUpdate, mode);
						} catch(Exception e) {
							logger.error("error update book " + path, e);
							
							continue;
						}
						
						if(book.getNumberOfPages() == null || book.getNumberOfPages() == 0) {
				    		continue;
				    	}
						
						book.setUpdateDate(this.updateDate);
						
						numberOfBooks = numberOfBooks + 1;
						numberOfBookPages = numberOfBookPages + book.getNumberOfPages();
						number = number + 1;
						
						book.setNumber(number);
						
						book = bookService.updateBook(book);
						
						bookMarkService.updateBookMarkReferencesByBook(book, this.updateDate);
					}
				}
			}
		}
		
		logger.info("update parentBookCollection");
		
		parentBookCollection.setUpdateDate(this.updateDate);
		parentBookCollection.setNumberOfBookCollections(numberOfBookCollections);
		parentBookCollection.setNumberOfBooks(numberOfBooks);
		parentBookCollection.setNumberOfBookPages(numberOfBookPages);
		
		parentBookCollection = bookCollectionService.updateBookCollection(parentBookCollection);
		
		bookMarkService.createOrUpdateOrDeleteBookCollectionMarkByBookCollection(parentBookCollection, this.updateDate);
		
		return number;
    }
	
	protected void processRootBookCollection(String bookCollectionName, BookCollection bookCollection, BookScannerMode mode) throws Exception {
		if(BookScannerMode.CREATE.equals(mode)) {
			String name = bookCollectionName;
			
			bookCollection.setName(name);
			
			String normalizedName = NameHelper.getNormalizedName(name);
			
			bookCollection.setNormalizedName(normalizedName);
		}
	}
	
	protected void processBookCollection(File bookCollectionInputFile, BookCollection bookCollection, BookCollection bookCollectionUpdate, BookScannerMode mode) throws Exception {
		if(bookCollectionUpdate != null) {
			if(BookScannerMode.CREATE.equals(mode)) {
		    	bookCollection.setName(bookCollectionUpdate.getName());
		    	bookCollection.setNormalizedName(bookCollectionUpdate.getNormalizedName());
			}
		} else {
			if(BookScannerMode.CREATE.equals(mode)) {
				String name = FileHelper.getName(bookCollectionInputFile);
				
				bookCollection.setName(name);
				
				String normalizedName = NameHelper.getNormalizedName(name);
				
				bookCollection.setNormalizedName(normalizedName);
			}
		}
	}
	
	private String getFileId(File bookInputFile) throws Exception {
		FactoryManager factoryManager = FactoryManager.getInstance();
		
		HashFactory hashFactory = factoryManager.getFactory(HashFactory.class);
		Hash hash = hashFactory.getHash(HashType.SHA256);

		return hash.calculate(bookInputFile);
	}
	
	protected void processBook(File bookInputFile, BookType bookType, Book book, Book bookUpdate, BookScannerMode mode) throws Exception {
		if(bookUpdate != null) {
			if(BookScannerMode.CREATE.equals(mode)) {
				book.setFileId(bookUpdate.getFileId());
		    	book.setName(bookUpdate.getName());
		    	book.setNormalizedName(bookUpdate.getNormalizedName());
			}
		} else {
			if(BookScannerMode.CREATE.equals(mode)) {
				String fileId = getFileId(bookInputFile);
		    	
		    	book.setFileId(fileId);
		    	
				String name = FileHelper.getName(bookInputFile);
				
				book.setName(name);
				
				String normalizedName = NameHelper.getNormalizedName(name);
				
				book.setNormalizedName(normalizedName);
			}
		}
	}
	
	private List<BookPageConfiguration> getBookPageConfigurations(Book book) throws Exception {
		List<BookPageConfiguration> bookPageConfigurationList = new ArrayList<BookPageConfiguration>();
    	
    	for(BookPageConfiguration defaultBookPageConfiguration: this.defaultBookPageConfigurationList) {
    		Integer page;
    		Integer lastPage;
    		
    		if(defaultBookPageConfiguration.getPage() != null) {
    			page = defaultBookPageConfiguration.getPage();
        		lastPage = page;
    		} else {
    			page = 1;
        		lastPage = book.getNumberOfPages();
    		}
    		
    		while(page <= lastPage) {
	    		for(ScaleConfiguration defaultScaleConfiguration: defaultBookPageConfiguration.getScaleConfigurations()) {
		    		BookPageConfiguration bookPageConfiguration = BookPageHelper.getBookPageConfiguration(bookPageConfigurationList, page);
		    		if(bookPageConfiguration == null) {
			    		bookPageConfiguration = new BookPageConfiguration();
						bookPageConfiguration.setPage(page);
						
						bookPageConfigurationList.add(bookPageConfiguration);
		    		}
		    		
		    		bookPageConfiguration.getScaleConfigurations().add(defaultScaleConfiguration);
	    		}
	    		
	    		page = page + 1;
    		}
    	}
    	
    	return bookPageConfigurationList;
	}
    
    protected void processBookPages(File bookInputFile, BookType bookType, Book book, Book bookUpdate, BookScannerMode mode) throws Exception {
    	if(bookUpdate != null) {
    		if(BookScannerMode.CREATE.equals(mode)) {
    			book.setNumberOfPages(bookUpdate.getNumberOfPages());
    		}
		} else {
	    	BookReader bookReader = null;
			try {
				if(BookScannerMode.CREATE.equals(mode)) {
					BookReaderFactory bookReaderFactory = BookReaderFactory.getInstance();
					
					bookReader = bookReaderFactory.getBookReader(bookType);
					bookReader.openBook(bookInputFile);
		
					Integer numberOfPages = bookReader.getNumberOfBookPages();
					
					book.setNumberOfPages(numberOfPages);
				}
				
				List<BookPageConfiguration> bookPageConfigurationList = getBookPageConfigurations(book);
				
				for(BookPageConfiguration bookPageConfiguration: bookPageConfigurationList) {
					if(bookPageConfiguration.getPage() >= 1 && bookPageConfiguration.getPage() <= book.getNumberOfPages()) {
						File bookPageInputFile = null;
						try {
							for(ScaleConfiguration scaleConfiguration: bookPageConfiguration.getScaleConfigurations()) {
								File bookPageOutputFile = getBookPage(
					    				book, 
					    				bookPageConfiguration.getPage(), 
					    				scaleConfiguration.getScaleType(), 
					    				scaleConfiguration.getScaleWidth(), 
					    				scaleConfiguration.getScaleHeight()
					    		);
								
								if(BookScannerMode.UPDATE.equals(mode)) {
									if(bookPageOutputFile.isFile()) {
							    		updateBookPage(bookPageOutputFile);
							    		
							    		continue;
							    	}
								}
								
								if(bookPageInputFile == null) {
									if(bookReader == null) {
										BookReaderFactory bookReaderFactory = BookReaderFactory.getInstance();
										
										bookReader = bookReaderFactory.getBookReader(bookType);
										bookReader.openBook(bookInputFile);
									}
									
									bookPageInputFile = bookReader.getBookPage(bookPageConfiguration.getPage() - 1);
								}
								
								BookPageType bookPageType = BookPageType.getBookPageType(bookPageInputFile);
								
								if(BookPageType.JPEG.equals(bookPageType) 
										&& scaleConfiguration.getScaleType() == null 
										&& scaleConfiguration.getScaleWidth() == null 
										&& scaleConfiguration.getScaleHeight() == null) {
									createBookPage(bookPageInputFile, bookPageOutputFile);
								} else {
									File bookPageInputFile2 = null;
									try {
										bookPageInputFile2 = BookPageHelper.getBookPage(
												bookPageInputFile, 
												BookPageType.JPEG, 
												scaleConfiguration.getScaleType(), 
												scaleConfiguration.getScaleWidth(), 
												scaleConfiguration.getScaleHeight()
										);
										
										createBookPage(bookPageInputFile2, bookPageOutputFile);
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
		}
    }
    
    private File getBookPage(Book book, Integer page, ScaleType scaleType, Integer scaleWidth, Integer scaleHeight) throws Exception {
    	File directory = getDirectory();
    	
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
        
        File bookPageFile = new File(directory, bookPageFilePath);
		
		return bookPageFile;
    }
    
    private void createBookPage(File bookPageInputFile, File bookPageOutputFile) throws Exception {
    	File bookPageOutputDirectory = bookPageOutputFile.getParentFile();
    	File bookPageOutputDirectory2 = bookPageOutputDirectory.getParentFile();
		
		if(bookPageOutputDirectory2.isDirectory() == false) {
			bookPageOutputDirectory2.mkdir();
		}
		
		if(bookPageOutputDirectory.isDirectory() == false) {
			bookPageOutputDirectory.mkdir();
		}
    	
		InputStream bookPageInputStream = null;
		OutputStream bookPageOutputStream = null;
		try {
			bookPageInputStream = new FileInputStream(bookPageInputFile);
			bookPageOutputStream = new FileOutputStream(bookPageOutputFile);
			
			byte[] buffer = new byte[8 * 1024];
		    int bufferSize;
		    while ((bufferSize = bookPageInputStream.read(buffer)) != -1) {
		    	bookPageOutputStream.write(buffer, 0, bufferSize);
		    }
		} finally {
			try {
				if(bookPageOutputStream != null) {
					bookPageOutputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
			
			try {
				if(bookPageInputStream != null) {
					bookPageInputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
		
		bookPageOutputFile.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory2.setLastModified(this.updateDate.getTime());
	}
    
    private void updateBookPage(File bookPageOutputFile) throws Exception {
    	File bookPageOutputDirectory = bookPageOutputFile.getParentFile();
    	File bookPageOutputDirectory2 = bookPageOutputDirectory.getParentFile();
		
		bookPageOutputFile.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory2.setLastModified(this.updateDate.getTime());
    }
    
    private void deleteBookPageByUpdateDate() throws Exception {
    	File directory = getDirectory();
    	
    	if(directory.isDirectory()) {
    		File[] bookPageDirectoryList = directory.listFiles();
    		
			for(File bookPageDirectory: bookPageDirectoryList) {
				if(BookScannerStatus.STOPPING.equals(this.status)) {
		    		logger.info("stopping!");
		    		
		    		return;
				}
				
				if(bookPageDirectory.isDirectory()) {
					Date bookPageDirectoryUpdateDate = new Date(bookPageDirectory.lastModified());
					
					File[] bookPageDirectoryList2 = bookPageDirectory.listFiles();
					
					for(File bookPageDirectory2: bookPageDirectoryList2) {
						if(BookScannerStatus.STOPPING.equals(this.status)) {
				    		logger.info("stopping!");
				    		
				    		return;
						}
						
						if(bookPageDirectory2.isDirectory()) {
							Date bookPageDirectoryUpdateDate2 = new Date(bookPageDirectory2.lastModified());
							
							File[] bookPageFileList = bookPageDirectory2.listFiles();
							
							for(File bookPageFile: bookPageFileList) {
								if(bookPageFile.isFile()) {
									Date bookPageUpdateDate = new Date(bookPageFile.lastModified());
									
									if(bookPageUpdateDate.compareTo(this.updateDate) < 0) {
										bookPageFile.delete();
									}
								}
							}
							
							if(bookPageDirectoryUpdateDate2.compareTo(this.updateDate) < 0) {
								bookPageDirectory2.delete();
							}
						}
					}
					
					if(bookPageDirectoryUpdateDate.compareTo(this.updateDate) < 0) {
						bookPageDirectory.delete();
					}
				}
			}
    	}
    }
}
