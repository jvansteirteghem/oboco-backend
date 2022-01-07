package com.gitlab.jeeto.oboco.common.image;

public abstract class TwelveMonkeysImageReader extends JdkImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader.JpegImageReader {
	public static class TwelveMonkeysJpegImageReader extends TwelveMonkeysImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader.JpegImageReader {
		@Override
		public String getFormatName() {
			return "jpg";
		}
	}
}
