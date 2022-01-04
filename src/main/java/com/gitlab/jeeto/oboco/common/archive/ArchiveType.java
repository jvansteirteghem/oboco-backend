package com.gitlab.jeeto.oboco.common.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.common.FileHelper;
import com.gitlab.jeeto.oboco.common.FileType;

public enum ArchiveType implements FileType {
	ZIP(new int[] {0x50, 0x4B, 0x03, 0x04}),
	RAR(new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00}),
	RAR5(new int[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00}),
	SEVENZIP(new int[] {0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C});
	
	public static ArchiveType getArchiveType(File file) throws IOException {
		List<FileType> fileTypeList = new ArrayList<FileType>();
		fileTypeList.add(ArchiveType.ZIP);
		fileTypeList.add(ArchiveType.RAR);
		fileTypeList.add(ArchiveType.RAR5);
		fileTypeList.add(ArchiveType.SEVENZIP);
		
		return (ArchiveType) FileHelper.getFileType(file, fileTypeList);
	}
	
	private int[] signature;
	
	private ArchiveType(int[] signature) {
		this.signature = signature;
	}
	
	@Override
	public int[] getSignature() {
		return signature;
	}
}
