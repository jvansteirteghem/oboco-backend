package com.gitlab.jeeto.oboco.api.v1.bookscanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import com.gitlab.jeeto.oboco.api.v1.book.Book;
import com.gitlab.jeeto.oboco.api.v1.book.BookService;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollection;
import com.gitlab.jeeto.oboco.api.v1.bookcollection.BookCollectionService;
import com.gitlab.jeeto.oboco.api.v1.bookmark.BookMarkService;
import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileType.Type;
import com.gitlab.jeeto.oboco.common.NameHelper;
import com.gitlab.jeeto.oboco.common.NaturalOrderComparator;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReader;
import com.gitlab.jeeto.oboco.common.archive.ArchiveReaderFactory;
import com.gitlab.jeeto.oboco.common.configuration.Configuration;
import com.gitlab.jeeto.oboco.common.configuration.ConfigurationManager;
import com.gitlab.jeeto.oboco.common.exception.Problem;
import com.gitlab.jeeto.oboco.common.exception.ProblemException;
import com.gitlab.jeeto.oboco.common.hash.HashManager;
import com.gitlab.jeeto.oboco.common.hash.HashManagerFactory;
import com.gitlab.jeeto.oboco.common.hash.HashType;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ImageManagerFactory;
import com.gitlab.jeeto.oboco.common.image.ScaleType;

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
	private List<BookPage> defaultBookPageList;
	
	public DefaultBookScanner() {
		super();
		this.id = "DEFAULT";
		this.mode = null;
		this.status = BookScannerStatus.STOPPED;
		this.updateDate = null;
		this.defaultBookPageList = new ArrayList<BookPage>();
	}
	
	private List<BookPage> createBookPageList() throws Exception {
		List<BookPage> bookPageList = new ArrayList<BookPage>();
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader("./data.csv"));
		    String line = null;
		    line = bufferedReader.readLine();
		    if(line != null && line.equals("page,scaleType,scaleWidth,scaleHeight")) {
			    while((line = bufferedReader.readLine()) != null) {
			        String[] values = line.split(",");
			        
			        Integer page = null;
			        try {
			        	page = Integer.valueOf(values[0]);
			        } catch(Exception e) {
			        	// pass
			        }
			        ScaleType scaleType = null;
			        try {
			        	scaleType = ScaleType.valueOf(values[1]);
			        } catch(Exception e) {
			        	// pass
			        }
			        Integer scaleWidth = null;
			        try {
			        	scaleWidth = Integer.valueOf(values[2]);
			        } catch(Exception e) {
			        	// pass
			        }
			        Integer scaleHeight = null;
			        try {
			        	scaleHeight = Integer.valueOf(values[3]);
			        } catch(Exception e) {
			        	// pass
			        }
			        
			        BookPage bookPage = getBookPage(bookPageList, page);
			        if(bookPage == null) {
			        	bookPage = new BookPage();
			        	bookPage.setPage(page);
			    		
			    		bookPageList.add(bookPage);
			        }
			        BookPageConfiguration bookPageConfiguration = new BookPageConfiguration();
			        bookPageConfiguration.setScaleType(scaleType);
			        bookPageConfiguration.setScaleWidth(scaleWidth);
			        bookPageConfiguration.setScaleHeight(scaleHeight);
					
					bookPage.getBookPageConfigurationList().add(bookPageConfiguration);
			    }
		    }
		} finally {
			if(bufferedReader != null) {
				bufferedReader.close();
			}
		}
		
		return bookPageList;
	}
	
	private BookPage getBookPage(List<BookPage> bookPageList, Integer page) throws Exception {
		for(BookPage bookPage: bookPageList) {
        	if(bookPage.getPage() == page) {
        		return bookPage;
        	}
        }
		return null;
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
	
	private TypeableFile getDirectory() throws ProblemException {
		String directoryPath = getConfiguration().getAsString("data.path", "./data");
    	
		TypeableFile directory = new TypeableFile(directoryPath);
    	
    	if(directory.isDirectory() == false) {
    		throw new ProblemException(new Problem(500, "PROBLEM", "The directory is invalid: " + directory.getAbsolutePath()));
    	}
    	
    	return directory;
	}
	
	private Map<String, List<TypeableFile>> getDirectoryMap() throws ProblemException {
		try {
			Map<String, List<TypeableFile>> directoryMap = new LinkedHashMap<String, List<TypeableFile>>();
			
			Properties dataProperties = new Properties();
	    	dataProperties.load(new FileInputStream("./data.properties"));
			
			for(Entry<Object, Object> entry: dataProperties.entrySet()) {
				String name = entry.getKey().toString();
				String directoryPathsString = entry.getValue().toString();
				
				if(directoryPathsString == null) {
					directoryPathsString = "";
				}
				
				String[] directoryPaths = directoryPathsString.split(",");
				
				List<TypeableFile> directoryList = new ArrayList<TypeableFile>();
				
				for(String directoryPath: directoryPaths) {
					directoryPath = directoryPath.trim();
					
					TypeableFile directory = new TypeableFile(directoryPath);
					
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
		// no milliseconds
		this.updateDate = new Date((new Date().getTime() / 1000L) * 1000L);
		this.status = BookScannerStatus.STARTED;
		try {
			this.defaultBookPageList = createBookPageList();
	    	
			// validate directory
	    	getDirectory();
	    	
	    	Map<String, List<TypeableFile>> directoryMap = getDirectoryMap();
			
	    	Integer number = 1;
			for(Entry<String, List<TypeableFile>> entry: directoryMap.entrySet()) {
				String name = entry.getKey();
				List<TypeableFile> directoryList = entry.getValue();
				
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
					bookCollection.setChildBookCollections(new ArrayList<BookCollection>());
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
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
					bookCollection.setChildBookCollections(new ArrayList<BookCollection>());
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					bookCollection.setNumber(number);
					
					bookCollection = bookCollectionService.updateBookCollection(bookCollection);
				}
				
				for(TypeableFile directory: directoryList) {
				    number = add(number, bookCollection, bookCollection, directory);
				}
				
				if(BookScannerStatus.STOPPING.equals(this.status)) {
		    		logger.info("stopping!");
		    		return;
				}
	    	}
			
			logger.info("delete bookMarkReferences");
	        
	        bookMarkService.deleteBookMarkReferences(this.updateDate);
	        
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
    
	private Integer add(Integer number, BookCollection rootBookCollection, BookCollection parentBookCollection, TypeableFile parentFile) throws Exception {
		List<FileType> fileTypeList = FileType.getFileTypeList(Type.ARCHIVE);
		
		Integer numberOfBookCollections = parentBookCollection.getNumberOfBookCollections();
		Integer numberOfBooks = parentBookCollection.getNumberOfBooks();
		List<BookCollection> childBookCollections = parentBookCollection.getChildBookCollections();
		
		TypeableFile[] files = parentFile.listTypeableFiles();
    	
    	List<TypeableFile> fileList = Arrays.asList(files);
    	fileList.sort(new NaturalOrderComparator<TypeableFile>() {
    		@Override
    		public String toString(TypeableFile o) {
				return o.getName();
		   }
    	});
    	
		for(TypeableFile file: fileList) {
			if(BookScannerStatus.STOPPING.equals(this.status)) {
	    		logger.info("stopping!");
	    		break;
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
					bookCollection.setChildBookCollections(new ArrayList<BookCollection>());
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					
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
					bookCollection.setChildBookCollections(new ArrayList<BookCollection>());
					bookCollection.setNumberOfBookCollections(0);
					bookCollection.setNumberOfBooks(0);
					
					numberOfBookCollections = numberOfBookCollections + 1;
					number = number + 1;
					
					bookCollection.setNumber(number);
					
					bookCollection = bookCollectionService.updateBookCollection(bookCollection);
				}
				
				childBookCollections.add(bookCollection);
				
				number = add(number, rootBookCollection, bookCollection, file);
				
				childBookCollections.addAll(bookCollection.getChildBookCollections());
			} else {
				FileType fileType = FileType.getFileType(file.getName());
				
				if(fileTypeList.contains(fileType)) {
					Book book = bookService.getBookByRootBookCollectionAndFile(rootBookCollection.getId(), path);
					Book bookUpdate = bookService.getBookByFile(path, this.updateDate);
					
					if(book == null) {
						logger.info("create book " + path);
						
						book = new Book();
						
				    	try {
							processBook(file, book, bookUpdate, BookScannerMode.CREATE);
							
							processBookPageList(file, book, bookUpdate, BookScannerMode.CREATE);
						} catch(Exception e) {
							logger.error("error create book " + path, e);
							
							continue;
						}
				    	
				    	book.setFilePath(file.getPath());
				    	book.setRootBookCollection(rootBookCollection);
				    	book.setBookCollection(parentBookCollection);
						book.setCreateDate(this.updateDate);
				    	book.setUpdateDate(this.updateDate);
						
						numberOfBooks = numberOfBooks + 1;
						number = number + 1;
						
						book.setNumber(number);
						
						book = bookService.createBook(book);
						
						bookMarkService.createBookMarkReferencesByBook(book);
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
							processBook(file, book, bookUpdate, mode);
							
							processBookPageList(file, book, bookUpdate, mode);
						} catch(Exception e) {
							logger.error("error update book " + path, e);
							
							continue;
						}
						
						book.setUpdateDate(this.updateDate);
						
						numberOfBooks = numberOfBooks + 1;
						number = number + 1;
						
						book.setNumber(number);
						
						book = bookService.updateBook(book);
						
						bookMarkService.updateBookMarkReferencesByBook(book);
					}
				}
			}
		}
		
		logger.info("update parentBookCollection");
		
		parentBookCollection.setUpdateDate(this.updateDate);
		parentBookCollection.setChildBookCollections(childBookCollections);
		parentBookCollection.setNumberOfBookCollections(numberOfBookCollections);
		parentBookCollection.setNumberOfBooks(numberOfBooks);
		
		parentBookCollection = bookCollectionService.updateBookCollection(parentBookCollection);
		
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
	
	protected void processBookCollection(TypeableFile bookCollectionInputFile, BookCollection bookCollection, BookCollection bookCollectionUpdate, BookScannerMode mode) throws Exception {
		if(bookCollectionUpdate != null) {
			if(BookScannerMode.CREATE.equals(mode)) {
		    	bookCollection.setName(bookCollectionUpdate.getName());
		    	bookCollection.setNormalizedName(bookCollectionUpdate.getNormalizedName());
			}
		} else {
			if(BookScannerMode.CREATE.equals(mode)) {
				String name = NameHelper.getName(bookCollectionInputFile);
				
				bookCollection.setName(name);
				
				String normalizedName = NameHelper.getNormalizedName(name);
				
				bookCollection.setNormalizedName(normalizedName);
			}
		}
	}
	
	protected void processBook(TypeableFile bookInputFile, Book book, Book bookUpdate, BookScannerMode mode) throws Exception {
		if(bookUpdate != null) {
			if(BookScannerMode.CREATE.equals(mode)) {
				book.setFileId(bookUpdate.getFileId());
		    	book.setName(bookUpdate.getName());
		    	book.setNormalizedName(bookUpdate.getNormalizedName());
			}
		} else {
			if(BookScannerMode.CREATE.equals(mode)) {
				String fileId = createFileId(bookInputFile);
		    	
		    	book.setFileId(fileId);
		    	
				String name = NameHelper.getName(bookInputFile);
				
				book.setName(name);
				
				String normalizedName = NameHelper.getNormalizedName(name);
				
				book.setNormalizedName(normalizedName);
			}
		}
	}
	
	private List<BookPage> getBookPageList(Book book) throws Exception {
		List<BookPage> bookPageList = new ArrayList<BookPage>();
    	
    	for(BookPage defaultBookPage: this.defaultBookPageList) {
    		Integer defaultPage;
    		Integer defaultLastPage;
    		
    		if(defaultBookPage.getPage() != null) {
    			defaultPage = defaultBookPage.getPage();
        		defaultLastPage = defaultPage;
    		} else {
    			defaultPage = 1;
        		defaultLastPage = book.getNumberOfPages();
    		}
    		
    		while(defaultPage <= defaultLastPage) {
	    		for(BookPageConfiguration defaultBookPageConfiguration: defaultBookPage.getBookPageConfigurationList()) {
		    		BookPage bookPage = getBookPage(bookPageList, defaultPage);
		    		if(bookPage == null) {
			    		bookPage = new BookPage();
						bookPage.setPage(defaultPage);
						
						bookPageList.add(bookPage);
		    		}
		    		
		    		bookPage.getBookPageConfigurationList().add(defaultBookPageConfiguration);
	    		}
	    		
	    		defaultPage = defaultPage + 1;
    		}
    	}
    	
    	return bookPageList;
	}
    
    protected void processBookPageList(TypeableFile bookInputFile, Book book, Book bookUpdate, BookScannerMode mode) throws Exception {
    	if(bookUpdate != null) {
    		if(BookScannerMode.CREATE.equals(mode)) {
    			book.setNumberOfPages(bookUpdate.getNumberOfPages());
    		}
		} else {
			ArchiveReaderFactory archiveReaderFactory = ArchiveReaderFactory.getInstance();
	    	ArchiveReader archiveReader = null;
			try {
				Integer numberOfPages = book.getNumberOfPages();
				
				if(BookScannerMode.CREATE.equals(mode)) {
					archiveReader = archiveReaderFactory.getArchiveReader(bookInputFile.getFileType());
					archiveReader.openArchive(bookInputFile);
		
					numberOfPages = archiveReader.readSize();
					
					book.setNumberOfPages(numberOfPages);
				}
				
				List<BookPage> bookPageList = getBookPageList(book);
				
				for(BookPage bookPage: bookPageList) {
					TypeableFile bookPageInputFile = null;
					try {
						for(BookPageConfiguration bookPageConfiguration: bookPage.getBookPageConfigurationList()) {
							TypeableFile bookPageOutputFile = getBookPage(
				    				book, 
				    				bookPage.getPage(), 
				    				bookPageConfiguration.getScaleType(), 
				    				bookPageConfiguration.getScaleWidth(), 
				    				bookPageConfiguration.getScaleHeight()
				    		);
							
							if(BookScannerMode.UPDATE.equals(mode)) {
								if(bookPageOutputFile.isFile()) {
						    		updateBookPage(bookPageOutputFile);
						    		
						    		continue;
						    	}
							}
							
							if(bookPageInputFile == null) {
								if(archiveReader == null) {
									archiveReader = archiveReaderFactory.getArchiveReader(bookInputFile.getFileType());
									archiveReader.openArchive(bookInputFile);
								}
								
								bookPageInputFile = archiveReader.readFile(bookPage.getPage() - 1);
							}
							
							if(FileType.JPG.equals(bookPageInputFile.getFileType()) 
									&& bookPageConfiguration.getScaleType() == null 
									&& bookPageConfiguration.getScaleWidth() == null 
									&& bookPageConfiguration.getScaleHeight() == null) {
								createBookPage(bookPageInputFile, bookPageOutputFile);
							} else {
								TypeableFile bookPageInputFile2 = null;
								try {
									bookPageInputFile2 = createBookPage(
											bookPageInputFile, 
											bookPage.getPage(), 
											bookPageConfiguration.getScaleType(), 
											bookPageConfiguration.getScaleWidth(), 
											bookPageConfiguration.getScaleHeight()
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
    }
    
    private String createFileId(TypeableFile bookInputFile) throws Exception {
    	HashManagerFactory hashManagerFactory = HashManagerFactory.getInstance();
    	HashManager hashManager = hashManagerFactory.getHashManager(HashType.SHA256);
    	
    	String fileId = hashManager.createHash(bookInputFile, HashType.SHA256);
    	
    	return fileId;
    }
    
    private TypeableFile getBookPage(Book book, Integer page, ScaleType scaleType, Integer scaleWidth, Integer scaleHeight) throws Exception {
    	TypeableFile directory = getDirectory();
    	
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
        
        TypeableFile bookPageFile = new TypeableFile(directory, bookPageFilePath);
		
		return bookPageFile;
    }
    
    private TypeableFile createBookPage(TypeableFile bookPageInputFile, Integer page, ScaleType scaleType, Integer scaleWidth, Integer scaleHeight) throws Exception {
		ImageManagerFactory imageManagerFactory = ImageManagerFactory.getInstance();
    	ImageManager imageManager = imageManagerFactory.getImageManager(bookPageInputFile.getFileType(), FileType.JPG);
		
    	TypeableFile bookPageOutputFile = imageManager.createImage(bookPageInputFile, FileType.JPG, scaleType, scaleWidth, scaleHeight);
		
		return bookPageOutputFile;
	}
    
    private void createBookPage(TypeableFile bookPageInputFile, TypeableFile bookPageOutputFile) throws Exception {
    	TypeableFile bookPageOutputDirectory = bookPageOutputFile.getParentTypeableFile();
    	TypeableFile bookPageOutputDirectory2 = bookPageOutputDirectory.getParentTypeableFile();
		
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
    
    private void updateBookPage(TypeableFile bookPageOutputFile) throws Exception {
    	TypeableFile bookPageOutputDirectory = bookPageOutputFile.getParentTypeableFile();
    	TypeableFile bookPageOutputDirectory2 = bookPageOutputDirectory.getParentTypeableFile();
		
		bookPageOutputFile.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory.setLastModified(this.updateDate.getTime());
		bookPageOutputDirectory2.setLastModified(this.updateDate.getTime());
    }
    
    private void deleteBookPageByUpdateDate() throws Exception {
    	TypeableFile directory = getDirectory();
    	
    	if(directory.isDirectory()) {
    		TypeableFile[] bookPageDirectoryList = directory.listTypeableFiles();
    		
			for(TypeableFile bookPageDirectory: bookPageDirectoryList) {
				if(BookScannerStatus.STOPPING.equals(this.status)) {
		    		logger.info("stopping!");
		    		break;
				}
				
				if(bookPageDirectory.isDirectory()) {
					Date bookPageDirectoryUpdateDate = new Date(bookPageDirectory.lastModified());
					
					TypeableFile[] bookPageDirectoryList2 = bookPageDirectory.listTypeableFiles();
					
					for(TypeableFile bookPageDirectory2: bookPageDirectoryList2) {
						if(BookScannerStatus.STOPPING.equals(this.status)) {
				    		logger.info("stopping!");
				    		break;
						}
						
						if(bookPageDirectory2.isDirectory()) {
							Date bookPageDirectoryUpdateDate2 = new Date(bookPageDirectory2.lastModified());
							
							TypeableFile[] bookPageFileList = bookPageDirectory2.listTypeableFiles();
							
							for(TypeableFile bookPageFile: bookPageFileList) {
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
