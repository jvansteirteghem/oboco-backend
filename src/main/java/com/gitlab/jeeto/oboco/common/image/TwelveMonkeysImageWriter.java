package com.gitlab.jeeto.oboco.common.image;

import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public abstract class TwelveMonkeysImageWriter extends JdkImageWriter implements com.gitlab.jeeto.oboco.common.image.ImageWriter.JpegImageWriter {
	public static class TwelveMonkeysJpegImageWriter extends TwelveMonkeysImageWriter implements com.gitlab.jeeto.oboco.common.image.ImageWriter.JpegImageWriter {
		@Override
		public String getFormatName() {
			return "jpg";
		}
		
		@Override
		public ImageWriteParam getImageWriteParameter(ImageWriter imageWriter) {
			JPEGImageWriteParam imageWriteParameter = new JPEGImageWriteParam(null);
			imageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParameter.setCompressionQuality(0.9f);
			
			return imageWriteParameter;
		}
	}
}
