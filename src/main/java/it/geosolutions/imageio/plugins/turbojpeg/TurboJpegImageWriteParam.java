/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.turbojpeg;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

/**
 * Class holding Write parameters to customize the write operations
 * 
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 */
public class TurboJpegImageWriteParam extends ImageWriteParam {
	
    public TurboJpegImageWriteParam() {
        this(Locale.getDefault());
        
        this.compressionTypes = new String[] { DEFAULT_COMPRESSION_SCHEME };
    }

    public TurboJpegImageWriteParam(Locale locale) {
        super(locale);
        // fix compression type
        this.compressionTypes = new String[] { DEFAULT_COMPRESSION_SCHEME };

    }

    public final static String DEFAULT_COMPRESSION_SCHEME = "JPEG";

    public final static float DEFAULT_COMPRESSION_QUALITY = 0.75f;
    
    private int scaleWidth = 0;
    private int scaleHeight = 0;
	
    @Override
    public boolean canWriteCompressed() {
        return true;
    }

    @Override
    public boolean canWriteTiles() {
        return false;
    }

	public int getScaleWidth() {
		return scaleWidth;
	}

	public void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
	}

	public int getScaleHeight() {
		return scaleHeight;
	}

	public void setScaleHeight(int scaleHeight) {
		this.scaleHeight = scaleHeight;
	}
   
}