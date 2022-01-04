package com.gitlab.jeeto.oboco.common.image;

import javax.imageio.spi.IIORegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.image.TwelveMonkeysImageReader.TwelveMonkeysJpegImageReader;
import com.gitlab.jeeto.oboco.common.image.TwelveMonkeysImageWriter.TwelveMonkeysJpegImageWriter;
import com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageReaderSpi;
import com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageWriterSpi;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysJpegImageReaderSpi;
import com.gitlab.jeeto.oboco.common.image.twelvemonkeys.TwelveMonkeysJpegImageWriterSpi;

public class TwelveMonkeysImageIOFactory implements Factory {
	private static Logger logger = LoggerFactory.getLogger(TwelveMonkeysImageIOFactory.class.getName());
	private static boolean factoryStarted = false;
	
	public TwelveMonkeysImageIOFactory() {
		super();
	}
	
	public ImageReader getImageReader(ImageType imageType) throws Exception {
		ImageReader imageReader = null;
		
		if(ImageType.JPEG.equals(imageType)) {
			imageReader = new TwelveMonkeysJpegImageReader();
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageReader;
	}
	
	public ImageWriter getImageWriter(ImageType imageType) throws Exception {
		ImageWriter imageWriter = null;
		
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
			if(factoryStarted == false) {
				factoryStarted = true;
				
	        	logger.debug("get registry");
				IIORegistry registry = IIORegistry.getDefaultInstance();
				
				logger.debug("register turboJpegImageReaderSpi");
				TurboJpegImageReaderSpi turboJpegImageReaderSpi = new TurboJpegImageReaderSpi();
				registry.registerServiceProvider(turboJpegImageReaderSpi);
				
				logger.debug("register turboMonkeysJPEGImageReaderSpi");
				TwelveMonkeysJpegImageReaderSpi twelveMonkeysJpegImageReaderSpi = new TwelveMonkeysJpegImageReaderSpi(turboJpegImageReaderSpi);
				registry.registerServiceProvider(twelveMonkeysJpegImageReaderSpi);
				
				logger.debug("register turboJpegImageWriterSpi");
				TurboJpegImageWriterSpi turboJpegImageWriterSpi = new TurboJpegImageWriterSpi();
				registry.registerServiceProvider(turboJpegImageWriterSpi);
				
				logger.debug("register turboMonkeysJPEGImageWriterSpi");
				TwelveMonkeysJpegImageWriterSpi twelveMonkeysJPEGImageWriterSpi = new TwelveMonkeysJpegImageWriterSpi(turboJpegImageWriterSpi);
				registry.registerServiceProvider(twelveMonkeysJPEGImageWriterSpi);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		
	}
}
