package com.gitlab.jeeto.oboco.common.image;

import com.gitlab.jeeto.oboco.common.FileType;
import com.gitlab.jeeto.oboco.common.TypeableFile;

public interface ImageManager {
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType) throws Exception;
	public TypeableFile createImage(TypeableFile inputFile, FileType outputFileType, ScaleType outputScaleType, Integer outputScaleWidth, Integer outputScaleHeight) throws Exception;
}
