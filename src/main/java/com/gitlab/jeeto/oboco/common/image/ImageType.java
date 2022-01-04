package com.gitlab.jeeto.oboco.common.image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gitlab.jeeto.oboco.common.FileHelper;
import com.gitlab.jeeto.oboco.common.FileType;

public enum ImageType implements FileType {
	JPEG(new int[] {0xFF, 0xD8}),
	PNG(new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
	
	public static ImageType getImageType(File file) throws IOException {
		List<FileType> fileTypeList = new ArrayList<FileType>();
		fileTypeList.add(ImageType.JPEG);
		fileTypeList.add(ImageType.PNG);
		
		return (ImageType) FileHelper.getFileType(file, fileTypeList);
	}
	
	private int[] signature;
	
	private ImageType(int[] signature) {
		this.signature = signature;
	}
	
	@Override
	public int[] getSignature() {
		return signature;
	}
}
