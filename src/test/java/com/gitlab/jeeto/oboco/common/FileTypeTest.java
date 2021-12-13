package com.gitlab.jeeto.oboco.common;

import java.io.File;

import org.junit.Test;

import com.gitlab.jeeto.oboco.common.FileType;

import junit.framework.TestCase;

public class FileTypeTest extends TestCase {
	@Test
	public void testFileType() {
		FileType fileType = FileType.getFileType(new File("src/test/resources/abcde.zip"));
		assertEquals(FileType.ZIP, fileType);
		
		fileType = FileType.getFileType(new File("src/test/resources/abcde.rar"));
		assertEquals(FileType.RAR, fileType);
		
		fileType = FileType.getFileType(new File("src/test/resources/abcde.rar5"));
		assertEquals(FileType.RAR5, fileType);
		
		fileType = FileType.getFileType(new File("src/test/resources/abcde.7z"));
		assertEquals(FileType.SEVENZIP, fileType);
	}
}
