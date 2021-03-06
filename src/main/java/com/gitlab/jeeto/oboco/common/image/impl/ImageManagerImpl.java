package com.gitlab.jeeto.oboco.common.image.impl;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ScaleType;
import com.twelvemonkeys.image.ResampleOp;
import com.twelvemonkeys.imageio.plugins.jpeg.TwelveMonkeysJpegImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.jpeg.TwelveMonkeysJpegImageWriterSpi;

import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageReaderSpi;
import it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageWriterSpi;

public class ImageManagerImpl implements ImageManager {
	private static Logger logger = LoggerFactory.getLogger(ImageManagerImpl.class.getName());
	
	static {
		try {
        	logger.debug("get registry");
			IIORegistry registry = IIORegistry.getDefaultInstance();
			
			logger.debug("register turboJpegImageReaderSpi");
			TurboJpegImageReaderSpi turboJpegImageReaderSpi = new TurboJpegImageReaderSpi();
			registry.registerServiceProvider(turboJpegImageReaderSpi);
			
			logger.debug("register turboJpegImageWriterSpi");
			TurboJpegImageWriterSpi turboJpegImageWriterSpi = new TurboJpegImageWriterSpi();
			registry.registerServiceProvider(turboJpegImageWriterSpi);
			
			logger.debug("register turboMonkeysJPEGImageReaderSpi");
			TwelveMonkeysJpegImageReaderSpi twelveMonkeysJpegImageReaderSpi = new TwelveMonkeysJpegImageReaderSpi(turboJpegImageReaderSpi);
			registry.registerServiceProvider(twelveMonkeysJpegImageReaderSpi);
			
			logger.debug("register turboMonkeysJPEGImageWriterSpi");
			TwelveMonkeysJpegImageWriterSpi twelveMonkeysJPEGImageWriterSpi = new TwelveMonkeysJpegImageWriterSpi(turboJpegImageWriterSpi);
			registry.registerServiceProvider(twelveMonkeysJPEGImageWriterSpi);
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
	
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType) throws Exception {
		return createImage(inputFile, outputFileType, null, null, null);
	}
	
	private BufferedImage read(TypeableFile inputFile) throws Exception {
		FileType inputFileType = inputFile.getFileType();
		
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
	
	private void write(TypeableFile outputFile, BufferedImage outputImage) throws Exception {
		FileType outputFileType = outputFile.getFileType();
		
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
		    
		    BufferedImageOp resampler = new ResampleOp(regionWidth, regionHeight, ResampleOp.FILTER_LANCZOS);
			outputImage = resampler.filter(inputImage, null);
	    } else {
		    double scaleFactor = Math.min(inputImage.getWidth() / outputScaleWidth, inputImage.getHeight() / outputScaleHeight);
		    
			int regionWidth = (int) (outputScaleWidth * scaleFactor);
			int regionHeight = (int) (outputScaleHeight * scaleFactor);
			int regionX = (inputImage.getWidth() - regionWidth) / 2;
			int regionY = (inputImage.getHeight() - regionHeight) / 2;
		    
		    inputImage = inputImage.getSubimage(regionX, regionY, regionWidth, regionHeight);
			
		    BufferedImageOp resampler = new ResampleOp(outputScaleWidth, outputScaleHeight, ResampleOp.FILTER_LANCZOS);
			outputImage = resampler.filter(inputImage, null);
	    }
		
		return outputImage;
	}
	
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception {
		BufferedImage inputBufferedImage = read(inputFile);
		
		BufferedImage outputBufferedImage = scale(inputBufferedImage, outputScaleType, outputScaleWidth, outputScaleHeight);
		
		inputBufferedImage.flush();
		
		TypeableFile outputFile = new TypeableFile(File.createTempFile("oboco-plugin-image-jdk-", ".tmp"), outputFileType);
		
		write(outputFile, outputBufferedImage);
		
		outputBufferedImage.flush();
		
		return outputFile;
	}
}
