package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

public class ArchiveTypeTest extends TestCase {
	@Test
	public void test() throws IOException {
		assertEquals(ArchiveType.ZIP, ArchiveType.getArchiveType(new File("src/test/resources/abcde.zip")));
		
		assertEquals(ArchiveType.RAR, ArchiveType.getArchiveType(new File("src/test/resources/abcde.rar")));
		
		assertEquals(ArchiveType.RAR5, ArchiveType.getArchiveType(new File("src/test/resources/abcde.rar5")));
		
		assertEquals(ArchiveType.SEVENZIP, ArchiveType.getArchiveType(new File("src/test/resources/abcde.7z")));
	}
}
