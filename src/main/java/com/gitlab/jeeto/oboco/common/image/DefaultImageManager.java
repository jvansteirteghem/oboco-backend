package com.gitlab.jeeto.oboco.common.image;

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
import com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageReaderSpi;
import com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageWriterSpi;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysJpegImageReaderSpi;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysJpegImageWriterSpi;
import com.twelvemonkeys.image.ResampleOp;

public class DefaultImageManager implements ImageManager {
	private static Logger logger = LoggerFactory.getLogger(DefaultImageManager.class.getName());
	
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
		BufferedImage bufferedImage = null;
		
		ImageReader imageReader = null;
		ImageReadParam imageReadParam = null;
		try {
			FileType inputFileType = inputFile.getFileType();
			
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
			
			FileImageInputStream fileImageInputStream = null;
			try {
				fileImageInputStream = new FileImageInputStream(inputFile);
				
				imageReader.setInput(fileImageInputStream);
				
				bufferedImage = imageReader.read(0, imageReadParam);
			} finally {
				try {
					if(fileImageInputStream != null) {
						fileImageInputStream.close();
					}
				} catch(Exception e) {
					// pass
				}
			}
		} finally {
			try {
				if(imageReader != null) {
					imageReader.dispose();
				}
			} catch(Exception e) {
				// pass
			}
		}
		
		return bufferedImage;
	}
	
	private void write(TypeableFile outputFile, BufferedImage outputImage) throws Exception {
		ImageWriter imageWriter = null;
		ImageWriteParam imageWriteParam = null;
		try {
			FileType outputFileType = outputFile.getFileType();
			
			if(FileType.JPG.equals(outputFileType)) {
				imageWriter = getImageWriter("jpg");
				
				JPEGImageWriteParam jpegImageWriteParam = new JPEGImageWriteParam(null);
				jpegImageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegImageWriteParam.setCompressionQuality(0.9f);
				
				imageWriteParam = jpegImageWriteParam;
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
			} finally {
				try {
					if(fileImageOutputStream != null) {
						fileImageOutputStream.close();
					}
				} catch(Exception e) {
					// pass
				}
			}
		} finally {
			try {
				if(imageWriter != null) {
					imageWriter.dispose();
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
