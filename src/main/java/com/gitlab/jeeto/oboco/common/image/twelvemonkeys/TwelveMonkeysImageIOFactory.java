package com.gitlab.jeeto.oboco.common.image.twelvemonkeys;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.image.ImageType;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysImageReader.TwelveMonkeysJpegImageReader;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysImageWriter.TwelveMonkeysJpegImageWriter;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi;

public class TwelveMonkeysImageIOFactory extends Factory {
	private static Logger logger = LoggerFactory.getLogger(TwelveMonkeysImageIOFactory.class.getName());
	
	public TwelveMonkeysImageIOFactory() {
		super();
	}
	
	public com.gitlab.jeeto.oboco.common.image.ImageReader getImageReader(ImageType imageType) throws Exception {
		com.gitlab.jeeto.oboco.common.image.ImageReader imageReader = null;
		
		if(ImageType.JPEG.equals(imageType)) {
			imageReader = new TwelveMonkeysJpegImageReader();
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageReader;
	}
	
	public com.gitlab.jeeto.oboco.common.image.ImageWriter getImageWriter(ImageType imageType) throws Exception {
		com.gitlab.jeeto.oboco.common.image.ImageWriter imageWriter = null;
		
		if(ImageType.JPEG.equals(imageType)) {
			imageWriter = new TwelveMonkeysJpegImageWriter();
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageWriter;
	}
	
	@Override
	public void start() {
		try {
			IIORegistry registry = IIORegistry.getDefaultInstance();
			
			ImageReaderSpi imageReaderSpi = registry.getServiceProviderByClass(JPEGImageReaderSpi.class);
			
			if(imageReaderSpi == null) {
				imageReaderSpi = new JPEGImageReaderSpi();
				
				registry.registerServiceProvider(imageReaderSpi);
			}
			
			Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersByFormatName("jpg");
			while(imageReaderIterator.hasNext()) {
				ImageReader imageReader = imageReaderIterator.next();
				
				logger.debug("imageReader: " + imageReader);
			}
			
			ImageWriterSpi imageWriterSpi = registry.getServiceProviderByClass(JPEGImageWriterSpi.class);
			
			if(imageWriterSpi == null) {
				imageWriterSpi = new JPEGImageWriterSpi();
				
				registry.registerServiceProvider(imageWriterSpi);
			}
			
			Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByFormatName("jpg");
			while(imageWriterIterator.hasNext()) {
				ImageWriter imageWriter = imageWriterIterator.next();
				
				logger.debug("imageWriter: " + imageWriter);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void stop() {
		try {
			IIORegistry registry = IIORegistry.getDefaultInstance();
			
			ImageReaderSpi imageReaderSpi = registry.getServiceProviderByClass(JPEGImageReaderSpi.class);
			
			if(imageReaderSpi != null) {
				registry.deregisterServiceProvider(imageReaderSpi);
			}
			
			Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReadersByFormatName("jpg");
			while(imageReaderIterator.hasNext()) {
				ImageReader imageReader = imageReaderIterator.next();
				
				logger.debug("imageReader: " + imageReader);
			}
			
			ImageWriterSpi imageWriterSpi = registry.getServiceProviderByClass(JPEGImageWriterSpi.class);
			
			if(imageWriterSpi != null) {
				registry.deregisterServiceProvider(imageWriterSpi);
			}
			
			Iterator<ImageWriter> imageWriterIterator = ImageIO.getImageWritersByFormatName("jpg");
			while(imageWriterIterator.hasNext()) {
				ImageWriter imageWriter = imageWriterIterator.next();
				
				logger.debug("imageWriter: " + imageWriter);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
