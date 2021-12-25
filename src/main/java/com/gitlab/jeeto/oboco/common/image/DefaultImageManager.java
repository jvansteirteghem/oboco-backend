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
			logger.error("error", e);
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
	
	private double calculateFactor(int inputDimension, int outputDimension) {
		double factor;
		
		if(inputDimension > outputDimension) {
			factor = (double) outputDimension / (double) inputDimension;
		} else {
			factor = 1d;
		}
		
		return factor;
	}
	
	private BufferedImage scale(BufferedImage inputImage, ScaleType outputType, Integer outputWidth, Integer outputHeight) throws Exception {
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
	
	private BufferedImage read(TypeableFile inputFile) throws Exception {
		ImageReader imageReader = null;
		try {
			ImageReadParam imageReadParameter = null;
			
			FileType inputFileType = inputFile.getType();
			
			if(FileType.JPG.equals(inputFileType)) {
				imageReader = getImageReader("jpg");
				
				imageReadParameter = imageReader.getDefaultReadParam();
			} else if(FileType.PNG.equals(inputFileType)) {
				imageReader = getImageReader("png");
				
				imageReadParameter = imageReader.getDefaultReadParam();
			} else {
				throw new Exception("fileType not supported.");
			}
			
			FileImageInputStream fileImageInputStream = null;
			try {
				fileImageInputStream = new FileImageInputStream(inputFile);
				
				imageReader.setInput(fileImageInputStream);
				
				BufferedImage outputImage = imageReader.read(0, imageReadParameter);
				
				return outputImage;
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
	}
	
	private void write(TypeableFile outputFile, BufferedImage outputImage) throws Exception {
		ImageWriter imageWriter = null;
		try {
			ImageWriteParam imageWriteParameter = null;
			
			FileType outputFileType = outputFile.getType();
			
			if(FileType.JPG.equals(outputFileType)) {
				imageWriter = getImageWriter("jpg");
				
				JPEGImageWriteParam jpegImageWriteParameter = new JPEGImageWriteParam(null);
				jpegImageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpegImageWriteParameter.setCompressionQuality(0.9f);
				
				imageWriteParameter = jpegImageWriteParameter;
			} else {
				throw new Exception("fileType not supported.");
			}
			
			FileImageOutputStream fileImageOutputStream = null;
			try {
				fileImageOutputStream = new FileImageOutputStream(outputFile);
				
				imageWriter.setOutput(fileImageOutputStream);
				 
				imageWriter.write(null, new IIOImage(outputImage, null, null), imageWriteParameter);
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
	
	@Override
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType) throws Exception {
		return createImage(inputFile, outputFileType, null, null, null);
	}
	
	@Override
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception {
		BufferedImage inputImage = read(inputFile);
		
		BufferedImage outputImage = inputImage;
		
		if(outputScaleType != null) {
			outputImage = scale(inputImage, outputScaleType, outputScaleWidth, outputScaleHeight);
			
			inputImage.flush();
		}
		
		TypeableFile outputFile = new TypeableFile(File.createTempFile("oboco-common-image-", ".tmp"), outputFileType);
		
		write(outputFile, outputImage);
		
		outputImage.flush();
		
		return outputFile;
	}
}
