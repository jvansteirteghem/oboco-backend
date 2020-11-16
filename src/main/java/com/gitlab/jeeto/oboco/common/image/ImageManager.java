package com.gitlab.jeeto.oboco.common.image;

import java.io.File;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.FileWrapper;
import com.gitlab.jeeto.oboco.common.image.ScaleType;

public interface ImageManager  {
	public FileWrapper<File> createImage(FileWrapper<File> inputFileWrapper, FileType outputFileType) throws Exception;
	public FileWrapper<File> createImage(FileWrapper<File> inputFileWrapper, FileType outputFileType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception;
}
