package com.gitlab.jeeto.oboco.data.bookpage;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.common.image.ImageIOFactory;
import com.gitlab.jeeto.oboco.common.image.ImageReader;
import com.gitlab.jeeto.oboco.common.image.ImageType;
import com.gitlab.jeeto.oboco.common.image.ImageWriter;
import com.twelvemonkeys.image.ResampleOp;

public class BookPageHelper {
	private static double calculateFactor(int inputDimension, int outputDimension) {
		double factor;
		
		if(inputDimension > outputDimension) {
			factor = (double) outputDimension / (double) inputDimension;
		} else {
			factor = 1d;
		}
		
		return factor;
	}
	
	private static BufferedImage scale(BufferedImage inputImage, ScaleType outputType, Integer outputWidth, Integer outputHeight) throws Exception {
		BufferedImage outputImage = inputImage;
		
		if(ScaleType.DEFAULT.equals(outputType)) {
			double factor;
			
			if(outputWidth != null && outputHeight == null) {
				factor = calculateFactor(inputImage.getWidth(), outputWidth);
			} else if(outputWidth == null && outputHeight != null) {
				factor = calculateFactor(inputImage.getHeight(), outputHeight);
			} else if(outputWidth != null && outputHeight != null) {
				factor = Math.max(calculateFactor(inputImage.getWidth(), outputWidth), calculateFactor(inputImage.getHeight(), outputHeight));
			} else {
				factor = 1d;
			}
			
			if(factor < 1d) {
				int width = (int) Math.round(inputImage.getWidth() * factor);
				int height = (int) Math.round(inputImage.getHeight() * factor);
				
				BufferedImageOp op = new ResampleOp(width, height, ResampleOp.FILTER_LANCZOS);
				
				outputImage = op.filter(inputImage, null);
			}
		} else {
			throw new Exception("scaleType not supported.");
		}
		
		return outputImage;
	}
	
	public static File getBookPage(File inputFile, BookPageType outputBookPageType) throws Exception {
		return getBookPage(inputFile, outputBookPageType, null, null, null);
	}
	
	public static File getBookPage(File inputFile, BookPageType outputBookPageType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception {
		ImageIOFactory imageIOFactory = ImageIOFactory.getInstance();
		
		ImageType inputImageType = ImageType.getImageType(inputFile);
		ImageReader imageReader = imageIOFactory.getImageReader(inputImageType);
		
		BufferedImage inputImage = imageReader.read(inputFile);
		
		BufferedImage outputImage = inputImage;
		
		if(outputScaleType != null) {
			outputImage = scale(inputImage, outputScaleType, outputScaleWidth, outputScaleHeight);
			
			inputImage.flush();
		}
		
		File outputFile = null;
		try {
			outputFile = File.createTempFile("oboco-", outputBookPageType.getFileExtension());
			
			ImageType outputImageType = null;
			
			if(BookPageType.JPEG.equals(outputBookPageType)) {
				outputImageType = ImageType.JPEG;
			} else if(BookPageType.PNG.equals(outputBookPageType)) {
				outputImageType = ImageType.PNG;
			}
			
			ImageWriter imageWriter = imageIOFactory.getImageWriter(outputImageType);
			
			imageWriter.write(outputFile, outputImage);
			
			outputImage.flush();
		} catch(Exception e) {
			if(outputFile != null) {
				outputFile.delete();
			}
			
			throw e;
		}
		
		return outputFile;
	}
	
	public static BookPageConfiguration getBookPageConfiguration(List<BookPageConfiguration> bookPageConfigurationList, Integer page) throws Exception {
		for(BookPageConfiguration bookPageConfiguration: bookPageConfigurationList) {
        	if(bookPageConfiguration.getPage() == page) {
        		return bookPageConfiguration;
        	}
        }
		return null;
	}
	
	public static List<BookPageConfiguration> getBookPageConfigurations() throws Exception {
		List<BookPageConfiguration> bookPageConfigurationList = new ArrayList<BookPageConfiguration>();
		
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
			        
			        BookPageConfiguration bookPageConfiguration = getBookPageConfiguration(bookPageConfigurationList, page);
			        if(bookPageConfiguration == null) {
			        	bookPageConfiguration = new BookPageConfiguration();
			        	bookPageConfiguration.setPage(page);
			    		
			    		bookPageConfigurationList.add(bookPageConfiguration);
			        }
			        ScaleConfiguration scaleConfiguration = new ScaleConfiguration();
			        scaleConfiguration.setScaleType(scaleType);
			        scaleConfiguration.setScaleWidth(scaleWidth);
			        scaleConfiguration.setScaleHeight(scaleHeight);
					
					bookPageConfiguration.getScaleConfigurations().add(scaleConfiguration);
			    }
		    }
		} finally {
			if(bufferedReader != null) {
				bufferedReader.close();
			}
		}
		
		return bookPageConfigurationList;
	}
}
