package com.gitlab.jeeto.oboco.common.image;

import com.gitlab.jeeto.oboco.common.image.impl.ImageManagerImpl;
import com.gitlab.jeeto.oboco.common.FileType;

public class ImageManagerFactory {
	private static ImageManagerFactory instance;
	
	public static ImageManagerFactory getInstance() {
		if(instance == null) {
			synchronized(ImageManagerFactory.class) {
				if(instance == null) {
					instance = new ImageManagerFactory();
				}
			}
		}
		return instance;
	}
	
	private ImageManagerFactory() {
		super();
	}
	
	public ImageManager getImageManager(FileType inputFileType, FileType outputFileType) throws Exception {
		ImageManager imageManager = null;
		
		if(FileType.JPG.equals(inputFileType) && FileType.JPG.equals(outputFileType)) {
			imageManager = new ImageManagerImpl();
		} else if(FileType.PNG.equals(inputFileType) && FileType.JPG.equals(outputFileType)) {
			imageManager = new ImageManagerImpl();
		}
		
		return imageManager;
	}
}
