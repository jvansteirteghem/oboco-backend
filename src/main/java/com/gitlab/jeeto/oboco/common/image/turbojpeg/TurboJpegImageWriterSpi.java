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
package com.gitlab.jeeto.oboco.common.image.turbojpeg;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class TurboJpegImageWriterSpi extends ImageWriterSpi {
    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageWriterSpi");

    static final String[] suffixes = { "JPEG", "JPG", "jpeg", "jpg" };

    static final String[] formatNames = { "jpeg", "jpg" };

    static final String[] MIMETypes = { "image/jpeg" };

    static final String version = "1.0";

    static final String writerCN = "com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageWriter";

    static final String vendorName = "GeoSolutions";

    // ReaderSpiNames
    static final String[] readerSpiName = { "com.gitlab.jeeto.oboco.common.image.turbojpeg.TurboJpegImageReaderSpi" };

    // StreamMetadataFormatNames and StreamMetadataFormatClassNames
    static final boolean supportsStandardStreamMetadataFormat = false;

    static final String nativeStreamMetadataFormatName = null;

    static final String nativeStreamMetadataFormatClassName = null;

    static final String[] extraStreamMetadataFormatNames = null;

    static final String[] extraStreamMetadataFormatClassNames = null;

    // ImageMetadataFormatNames and ImageMetadataFormatClassNames
    static final boolean supportsStandardImageMetadataFormat = false;

    static final String nativeImageMetadataFormatName = null;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };
    
    private boolean registered = false;
    
    /**
     * Default {@link ImageWriterSpi} constructor for JP2K writers.
     */
    public TurboJpegImageWriterSpi() {
        super(vendorName, version, formatNames, suffixes, MIMETypes, writerCN,
                new Class[]{ImageOutputStream.class}, readerSpiName,
                supportsStandardStreamMetadataFormat,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                supportsStandardImageMetadataFormat,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames);
    }

    /**
     * @see javax.imageio.spi.ImageWriterSpi#createWriterInstance(java.lang.Object)
     */
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new TurboJpegImageWriter(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return "SPI for JPEG ImageWriter based on TurboJPEG";
    }

    /**
     * TODO: Refine the check before releasing.
     */
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return true;
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        super.onRegistration(registry, category);
        if (registered) {
            return;
        }
        registered = true;
        
    	try {
    		ImageWriterSpi defaultProvider = ImageWriterSpi.class.cast(registry.getServiceProviderByClass(Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi")));
    		
    		registry.deregisterServiceProvider(defaultProvider);
        } catch (Exception e) {
            // do nothing
        }
    }
    
    @Override
    public void onDeregistration(ServiceRegistry registry, Class<?> category) {
        super.onDeregistration(registry, category);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(getClass().getSimpleName() + " being deregistered");
        }
    }
}
