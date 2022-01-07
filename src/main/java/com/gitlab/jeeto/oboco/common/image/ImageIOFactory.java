package com.gitlab.jeeto.oboco.common.image;

import com.gitlab.jeeto.oboco.common.Factory;
import com.gitlab.jeeto.oboco.common.FactoryManager;

public class ImageIOFactory extends Factory {
	private static ImageIOFactory instance;
	private JdkImageIOFactory jdkImageIOFactory;
	private TwelveMonkeysImageIOFactory twelveMonkeysImageIOFactory;
	
	public static ImageIOFactory getInstance() {
		if(instance == null) {
			synchronized(ImageIOFactory.class) {
				if(instance == null) {
					instance = new ImageIOFactory();
					
					FactoryManager factoryManager = FactoryManager.getInstance();
					factoryManager.addFactory(instance);
				}
			}
		}
		return instance;
	}
	
	private ImageIOFactory() {
		super();
		
		jdkImageIOFactory = new JdkImageIOFactory();
		twelveMonkeysImageIOFactory = new TwelveMonkeysImageIOFactory();
	}
	
	public ImageReader getImageReader(ImageType imageType) throws Exception {
		ImageReader imageReader = null;
		
		if(ImageType.JPEG.equals(imageType)) {
			imageReader = twelveMonkeysImageIOFactory.getImageReader(imageType);
		} else if(ImageType.PNG.equals(imageType)) {
			imageReader = jdkImageIOFactory.getImageReader(imageType);
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageReader;
	}
	
	public ImageWriter getImageWriter(ImageType imageType) throws Exception {
		ImageWriter imageWriter = null;
		
		if(ImageType.JPEG.equals(imageType)) {
			imageWriter = twelveMonkeysImageIOFactory.getImageWriter(imageType);
		} else if(ImageType.PNG.equals(imageType)) {
			imageWriter = jdkImageIOFactory.getImageWriter(imageType);
		} else {
			throw new Exception("imageType not supported.");
		}
		
		return imageWriter;
	}

	@Override
	public void start() {
		jdkImageIOFactory.start();
		twelveMonkeysImageIOFactory.start();
	}

	@Override
	public void stop() {
		jdkImageIOFactory.stop();
		twelveMonkeysImageIOFactory.stop();
	}
}
