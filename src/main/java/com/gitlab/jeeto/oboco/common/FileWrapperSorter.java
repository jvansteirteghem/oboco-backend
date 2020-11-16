package com.gitlab.jeeto.oboco.common;

import java.util.List;

public abstract class FileWrapperSorter<T> {
	public void sort(List<FileWrapper<T>> listFileWrapper) throws Exception {
		for(int i = 0; i < listFileWrapper.size(); i = i + 1) {
			for(int j = 0; j < listFileWrapper.size() - i - 1; j = j + 1) {
				if(compare(listFileWrapper.get(j), listFileWrapper.get(j + 1)) > 0) {
					FileWrapper<T> fileWrapper = listFileWrapper.get(j);
					listFileWrapper.set(j, listFileWrapper.get(j + 1));
					listFileWrapper.set(j + 1, fileWrapper);
				}
			}
		}
	}
	
	public abstract int compare(FileWrapper<T> fileWrapper1, FileWrapper<T> fileWrapper2) throws Exception;
}
