package com.gitlab.jeeto.oboco.common.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdkImageWriter implements com.gitlab.jeeto.oboco.common.image.ImageWriter {
	private static Logger logger = LoggerFactory.getLogger(JdkImageWriter.class.getName());
	
	private static synchronized ImageWriter getImageWriter(String formatName) {
		ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(formatName).next();
		
		logger.debug("imageWriter: " + imageWriter);
		
		return imageWriter;
	}
	
	public abstract String getFormatName();
	
	public ImageWriteParam getImageWriteParameter(ImageWriter imageWriter) {
		return imageWriter.getDefaultWriteParam();
	}
	
	public void write(File outputFile, BufferedImage outputImage) throws Exception {
		ImageWriter imageWriter = null;
		try {
			imageWriter = getImageWriter(getFormatName());
			
			ImageWriteParam imageWriteParameter = getImageWriteParameter(imageWriter);
			
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
	
	public static class JdkPngImageWriter extends JdkImageWriter implements com.gitlab.jeeto.oboco.common.image.ImageWriter.PngImageWriter {
		@Override
		public String getFormatName() {
			return "png";
		}
	}
}
