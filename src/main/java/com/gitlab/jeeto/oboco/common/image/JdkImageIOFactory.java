package com.gitlab.jeeto.oboco.common.image;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.image.JdkImageReader.JdkPngImageReader;
import com.gitlab.jeeto.oboco.common.image.JdkImageWriter.JdkPngImageWriter;

public class JdkImageIOFactory implements Factory {
	public JdkImageIOFactory() {
		super();
	}
	
	public ImageReader getImageReader(ImageType imageType) throws Exception {
		ImageReader imageReader = null;
		
		if(ImageType.PNG.equals(imageType)) {
			imageReader = new JdkPngImageReader();
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageReader;
	}
	
	public ImageWriter getImageWriter(ImageType imageType) throws Exception {
		ImageWriter imageWriter = null;
		
		if(ImageType.PNG.equals(imageType)) {
			imageWriter = new JdkPngImageWriter();
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageWriter;
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}
}
