/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

/**
 * 
 * @author Emanuele Tajariol, GeoSolutions SaS
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class TurboJpegImageReaderSpi extends ImageReaderSpi {
    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageReaderSpi");

    // Adding a byte[] supported class. This allows the reader to receive bytes from a tiff reader
    // which may have internally JPEG compressed tiles
    public static final Class[] SUPPORTED_CLASSES = { ImageInputStream.class };
    static final String version = "1.0";
    static final String vendorName = "GeoSolutions";

    public static final String[] names = { "jpeg", "JPEG", "jpg", "JPG", "jfif", "JFIF",
            "jpeg-lossless", "JPEG-LOSSLESS", "jpeg-ls", "JPEG-LS" };

    private static final String[] suffixes = {"jpeg", "jpg", "jfif", "jls"};

    private static final String[] MIMETypes = {"image/jpeg"};

    private static final String readerClassName =
        "it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageReader";

    private static final String[] writerSpiNames = {
        "it.geosolutions.imageio.plugins.turbojpeg.TurboJpegImageWriterSpi"
    };

    static final String nativeImageMetadataFormatName = null;

    static final String nativeImageMetadataFormatClassName = null;

    static final String[] extraImageMetadataFormatNames = { null };

    static final String[] extraImageMetadataFormatClassNames = { null };

    private boolean registered = false;

    public TurboJpegImageReaderSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              MIMETypes,
              readerClassName,
              SUPPORTED_CLASSES,
              writerSpiNames,
              false, // supportsStandardStreamMetadataFormat
              null,  // nativeStreamMetadataFormatName
              null,  // nativeStreamMetadataFormatClassName
              null,  // extraStreamMetadataFormatNames
              null,  // extraStreamMetadataFormatClassNames
              true,  // supportsStandardImageMetadataFormat
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames);
    }
    
    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
    	super.onRegistration(registry, category);
        if (registered) {
            return;
        }
        registered = true;
        
    	try {
    		ImageReaderSpi defaultProvider = ImageReaderSpi.class.cast(registry.getServiceProviderByClass(Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi")));
    		
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

    public String getDescription(Locale locale) {
        return "SPI for JPEG ImageReader based on TurboJPEG";
    }

    public boolean canDecodeInput(Object source) throws IOException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("canDecodeInput");
        }

        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream) source;
        iis.mark();
        // If the first two bytes are a JPEG SOI marker, it's probably
        // a JPEG file. If they aren't, it definitely isn't a JPEG file.
        int byte1 = iis.read();
        int byte2 = iis.read();
        if ((byte1 != 0xFF) || (byte2 != 0xD8)) {
            iis.reset();
            return false;
        }
        do {
            byte1 = iis.read();
            byte2 = iis.read();
            if (byte1 != 0xFF)
                break; // something wrong, but probably readable
            if (byte2 == 0xDA)
                break; // Start of scan
            if (byte2 == 0xC2) { // progressive mode, can't decode
                iis.reset();
                return false;
            }
            if ((byte2 >= 0xC0) && (byte2 <= 0xC3)) // not progressive, can decode
                break;
            int length = iis.read() << 8;
            length += iis.read();
            length -= 2;
            while (length > 0)
                length -= iis.skipBytes(length);
        } while (true);
        iis.reset();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("canDecodeInput -> True");
        }

        return true;
    }

    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new TurboJpegImageReader(this);
    }
}
