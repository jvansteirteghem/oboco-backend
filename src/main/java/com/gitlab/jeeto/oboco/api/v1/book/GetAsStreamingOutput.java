package com.gitlab.jeeto.oboco.api.v1.book;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.StreamingOutput;

import com.gitlab.jeeto.oboco.common.FileWrapper;

public abstract class GetAsStreamingOutput implements StreamingOutput {
	public void write(OutputStream outputStream, FileWrapper<File> inputFileWrapper) throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(inputFileWrapper.getFile());
			
			byte[] buffer = new byte[8 * 1024];
		    int bufferSize;
		    while ((bufferSize = inputStream.read(buffer)) != -1) {
		    	outputStream.write(buffer, 0, bufferSize);
		    }
		} finally {
			if(inputStream != null) {
				try {
					inputStream.close();
				} catch(Exception e) {
					// pass
				}
			}
		}
	}
}
