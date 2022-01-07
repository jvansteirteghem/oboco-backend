package com.gitlab.jeeto.oboco.common.image;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.gitlab.jeeto.oboco.common.image.JdkImageReader.JdkPngImageReader;
import com.gitlab.jeeto.oboco.common.image.TwelveMonkeysImageWriter.TwelveMonkeysJpegImageWriter;

import junit.framework.TestCase;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ImageTest extends TestCase {
	@Test
	public void testFileType() throws Exception {
		JdkPngImageReader imageReader = Mockito.spy(JdkPngImageReader.class);
		
		File inputFile = new File("src/test/resources/java-duke.png");
		
		BufferedImage outputImage = imageReader.read(inputFile);
		
		TwelveMonkeysJpegImageWriter imageWriter = Mockito.spy(TwelveMonkeysJpegImageWriter.class);
		
		File outputFile = File.createTempFile("oboco-plugin-image-jdk-", ".jpg");
		
		imageWriter.write(outputFile, outputImage);
		
		System.out.println(outputFile.getPath());
	}
}
