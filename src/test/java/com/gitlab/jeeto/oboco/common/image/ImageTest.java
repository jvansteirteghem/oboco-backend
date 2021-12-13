package com.gitlab.jeeto.oboco.common.image;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.TypeableFile;
import com.gitlab.jeeto.oboco.common.image.ImageManager;
import com.gitlab.jeeto.oboco.common.image.ScaleType;

import junit.framework.TestCase;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ImageTest extends TestCase {
	@Test
	public void testFileType() throws Exception {
		//TypeableFile inputFile = new TypeableFile("src/test/resources/java-duke.png");
		TypeableFile inputFile = new TypeableFile("src/test/resources/java-duke-large.jpg");
		FileType outputFileType = FileType.JPG;
		
		ImageManager imageManager = Mockito.spy(ImageManager.class);
		
		TypeableFile outputFile = imageManager.createImage(inputFile, outputFileType, ScaleType.FILL, 250, 450);
		
		System.out.println(outputFile.getPath());
	}
}
