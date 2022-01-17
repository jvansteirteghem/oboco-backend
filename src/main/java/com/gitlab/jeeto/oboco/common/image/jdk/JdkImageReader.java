package com.gitlab.jeeto.oboco.common.image.jdk;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdkImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader {
	private static Logger logger = LoggerFactory.getLogger(JdkImageReader.class.getName());
	
	private static synchronized ImageReader getImageReader(String formatName) {
		ImageReader imageReader = ImageIO.getImageReadersByFormatName(formatName).next();
		
		logger.debug("imageReader: " + imageReader);
		
		return imageReader;
	}
	
	public abstract String getFormatName();
	
	public ImageReadParam getImageReadParameter(ImageReader imageReader) {
		return imageReader.getDefaultReadParam();
	}
	
	public BufferedImage read(File inputFile) throws Exception {
		ImageReader imageReader = null;
		try {
			imageReader = getImageReader(getFormatName());
			
			ImageReadParam imageReadParameter = getImageReadParameter(imageReader);
			
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
	
	public static class JdkPngImageReader extends JdkImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader.PngImageReader {
		@Override
		public String getFormatName() {
			return "png";
		}
	}
}
