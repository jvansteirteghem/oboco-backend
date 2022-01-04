package com.gitlab.jeeto.oboco.common.image;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ImageWriter  {
	public void write(File outputFile, BufferedImage outputImage) throws Exception;
	
	public static interface JpegImageWriter extends ImageWriter {
		
	}
	
	public static interface PngImageWriter extends ImageWriter {
		
	}
}
