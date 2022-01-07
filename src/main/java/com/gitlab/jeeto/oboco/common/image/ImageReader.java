package com.gitlab.jeeto.oboco.common.image;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageReader {
	public BufferedImage read(File inputFile) throws Exception;
	
	public static interface JpegImageReader extends ImageReader {
		
	}
	
	public static interface PngImageReader extends ImageReader {
		
	}
}
