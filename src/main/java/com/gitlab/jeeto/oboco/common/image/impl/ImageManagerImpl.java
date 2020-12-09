package com.gitlab.jeeto.oboco.common.image.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileWrapper;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ScaleType;
import com.mortennobel.imagescaling.experimental.ResampleOpSingleThread;

import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageReaderSpi;
import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageWriterSpi;
import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegUtilities;

public class ImageManagerImpl implements ImageManager {
	private static Logger logger = LoggerFactory.getLogger(ImageManagerImpl.class.getName());
	
	static {
		try {
			logger.info("load library");
        	TurboJpegUtilities.loadTurboJpeg();
    		
        	logger.info("get registry");
			IIORegistry registry = IIORegistry.getDefaultInstance();
			
			logger.info("get jpegImageReaderSpi");
			com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi jpegImageReaderSpi = registry.getServiceProviderByClass(com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi.class);
			logger.info("deregister jpegImageReaderSpi");
			registry.deregisterServiceProvider(jpegImageReaderSpi, ImageReaderSpi.class);
			
			logger.info("get jpegImageWriterSpi");
			com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi jpegImageWriterSpi = registry.getServiceProviderByClass(com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi.class);
			logger.info("deregister jpegImageWriterSpi");
			registry.deregisterServiceProvider(jpegImageWriterSpi, ImageWriterSpi.class);
			
			logger.info("create turboJpegImageReaderSpi");
			TurboJpegImageReaderSpi turboJpegImageReaderSpi = new TurboJpegImageReaderSpi();
			logger.info("register turboJpegImageReaderSpi");
			registry.registerServiceProvider(turboJpegImageReaderSpi);
			
			logger.info("create turboJpegImageWriterSpi");
			TurboJpegImageWriterSpi turboJpegImageWriterSpi = new TurboJpegImageWriterSpi();
			logger.info("register turboJpegImageWriterSpi");
			registry.registerServiceProvider(turboJpegImageWriterSpi);
        	
			Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName("jpg");
		    while(imageReaders.hasNext()) {
		    	logger.info("imageReader: " + imageReaders.next());
		    }
		    
		    Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
		    while(imageWriters.hasNext()) {
		    	logger.info("imageWriter: " + imageWriters.next());
		    }
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}
	
	private static synchronized ImageReader getImageReader(String formatName) {
		ImageReader imageReader = ImageIO.getImageReadersByFormatName(formatName).next();
		
		logger.debug("imageReader: " + imageReader);
		
		return imageReader;
	}
	
	private static synchronized ImageWriter getImageWriter(String formatName) {
		ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(formatName).next();
		
		logger.debug("imageWriter: " + imageWriter);
		
		return imageWriter;
	}
	
	public FileWrapper<File> createImage(FileWrapper<File> inputFileWrapper, FileType outputFileType) throws Exception {
		return createImage(inputFileWrapper, outputFileType, null, null, null);
	}
	
	private BufferedImage read(FileWrapper<File> inputFileWrapper) throws Exception {
		FileType inputFileType = inputFileWrapper.getFileType();
		File inputFile = inputFileWrapper.getFile();
		
		ImageReader imageReader = null;
		ImageReadParam imageReadParam = null;
		
		if(FileType.JPG.equals(inputFileType)) {
			imageReader = getImageReader("jpg");
		} else if(FileType.PNG.equals(inputFileType)) {
			imageReader = getImageReader("png");
		}
		
		if(imageReader == null) {
			throw new Exception("inputFileType not supported.");
		}
		
		if(imageReadParam == null) {
			imageReadParam = imageReader.getDefaultReadParam();
		}
		
		BufferedImage bufferedImage = null;
		
		FileImageInputStream fileImageInputStream = null;
		try {
			fileImageInputStream = new FileImageInputStream(inputFile);
			
			imageReader.setInput(fileImageInputStream);
			
			bufferedImage = imageReader.read(0, imageReadParam);
			
			imageReader.dispose();
		} finally {
			try {
				if(fileImageInputStream != null) {
					fileImageInputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
		
		return bufferedImage;
	}
	
	private void write(FileWrapper<File> outputFileWrapper, BufferedImage outputImage) throws Exception {
		FileType outputFileType = outputFileWrapper.getFileType();
		File outputFile = outputFileWrapper.getFile();
		
		ImageWriter imageWriter = null;
		ImageWriteParam imageWriteParam = null;
		
		if(FileType.JPG.equals(outputFileType)) {
			imageWriter = getImageWriter("jpg");
			
			imageWriteParam = new JPEGImageWriteParam(null);
			imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParam.setCompressionQuality(0.9f);
		}
		
		if(imageWriter == null) {
			throw new Exception("outputFileType not supported.");
		}
		
		if(imageWriteParam == null) {
			imageWriteParam = imageWriter.getDefaultWriteParam();
		}
		
		FileImageOutputStream fileImageOutputStream = null;
		try {
			fileImageOutputStream = new FileImageOutputStream(outputFile);
			
			imageWriter.setOutput(fileImageOutputStream);
			 
			imageWriter.write(null, new IIOImage(outputImage, null, null), imageWriteParam);
			
			imageWriter.dispose();
		} finally {
			try {
				if(fileImageOutputStream != null) {
					fileImageOutputStream.close();
				}
			} catch(Exception e) {
				// pass
			}
		}
	}
	
	private BufferedImage scale(BufferedImage inputImage, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception {
		BufferedImage outputImage;
	    
	    if(inputImage.getWidth() < outputScaleWidth || inputImage.getHeight() < outputScaleHeight) {
	    	int regionWidth;
	    	int regionX;
	    	if(inputImage.getWidth() < outputScaleWidth) {
	    		regionWidth = inputImage.getWidth();
	    		regionX = 0;
	    	} else {
	    		regionWidth = outputScaleWidth;
	    		regionX = (inputImage.getWidth() - regionWidth) / 2;
	    	}
	    	
	    	int regionHeight;
	    	int regionY;
	    	if(inputImage.getHeight() < outputScaleHeight) {
	    		regionHeight = inputImage.getHeight();
	    		regionY = 0;
	    	} else {
	    		regionHeight = outputScaleHeight;
	    		regionY = (inputImage.getHeight() - regionHeight) / 2;
	    	}
		    
		    inputImage = inputImage.getSubimage(regionX, regionY, regionWidth, regionHeight);
		    
		    ResampleOpSingleThread resampleOp = new ResampleOpSingleThread(regionWidth, regionHeight);
			outputImage = resampleOp.filter(inputImage, null);
	    } else {
		    double scaleFactor = Math.min(inputImage.getWidth() / outputScaleWidth, inputImage.getHeight() / outputScaleHeight);
		    
			int regionWidth = (int) (outputScaleWidth * scaleFactor);
			int regionHeight = (int) (outputScaleHeight * scaleFactor);
			int regionX = (inputImage.getWidth() - regionWidth) / 2;
			int regionY = (inputImage.getHeight() - regionHeight) / 2;
		    
		    inputImage = inputImage.getSubimage(regionX, regionY, regionWidth, regionHeight);
			
		    ResampleOpSingleThread resampleOp = new ResampleOpSingleThread(outputScaleWidth, outputScaleHeight);
			outputImage = resampleOp.filter(inputImage, null);
	    }
		
		return outputImage;
	}
	
	public FileWrapper<File> createImage(FileWrapper<File> inputFileWrapper, FileType outputFileType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception {
FileWrapper<File> outputFileWrapper = null;
		
		BufferedImage inputBufferedImage = read(inputFileWrapper);
		
		BufferedImage outputBufferedImage = scale(inputBufferedImage, outputScaleType, outputScaleWidth, outputScaleHeight);
		
		inputBufferedImage.flush();
		
		File outputFile = File.createTempFile("oboco-plugin-image-jdk-", ".tmp");
		
		outputFileWrapper = new FileWrapper<File>(outputFile, outputFileType);
		
		write(outputFileWrapper, outputBufferedImage);
		
		outputBufferedImage.flush();
		
		return outputFileWrapper;
	}
}
