package com.gitlab.jeeto.oboco.common;

//https://www.garykessler.net/library/file_sigs.html
//https://www.loc.gov/preservation/digital/formats/fdd/browse_list.shtml
public interface FileType {
	public int[] getSignature();
}
