package com.gitlab.jeeto.oboco.common.image.twelvemonkeys;

import com.gitlab.jeeto.oboco.common.image.jdk.JdkImageReader;

public abstract class TwelveMonkeysImageReader extends JdkImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader.JpegImageReader {
	public static class TwelveMonkeysJpegImageReader extends TwelveMonkeysImageReader implements com.gitlab.jeeto.oboco.common.image.ImageReader.JpegImageReader {
		@Override
		public String getFormatName() {
			return "jpg";
		}
	}
}
