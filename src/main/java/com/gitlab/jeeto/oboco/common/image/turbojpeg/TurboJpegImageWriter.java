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
package com.gitlab.jeeto.oboco.common.image.turbojpeg;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;


/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Simone Giannecchini, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 */
public class TurboJpegImageWriter extends ImageWriter
{

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger.getLogger("com.gitlab.jeeto.oboco.common.image.turbojpeg");

    private ImageOutputStream outputStream = null;

    public TurboJpegImageWriter(ImageWriterSpi originatingProvider)
    {
        super(originatingProvider);
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType,
        ImageWriteParam param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param)
    {
    	throw new UnsupportedOperationException();
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param)
    {
        return null;
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param)
    {
        return null;
    }

    /**
     * Sets the destination to the given <code>Object</code>.
     * For this TurboJPEG specific implementation, it needs to be
     * an instance of  {@link ImageOutputStreamAdapter2}.
     *
     * @param output
     *            the <code>Object</code> to use for future writing.
     */
    public void setOutput(Object output) 
    {
    	if (output != null) {
	      if (!(output instanceof ImageOutputStream)) {
	        throw new IllegalArgumentException("Output not an ImageOutputStream");
	      }
	      outputStream = (ImageOutputStream) output;
	    } else {
	      outputStream = null;
	    }
        super.setOutput(output);
    }
    
    private BufferedImage convertRenderedImage(RenderedImage img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage)img;  
        }   
        ColorModel cm = img.getColorModel();
        int width = img.getWidth();
        int height = img.getHeight();
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        Hashtable properties = new Hashtable();
        String[] keys = img.getPropertyNames();
        if (keys!=null) {
            for (int i = 0; i < keys.length; i++) {
                properties.put(keys[i], img.getProperty(keys[i]));
            }
        }
        BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
        img.copyData(raster);
        return result;
    }

    @Override
    public void write(IIOMetadata metadata, IIOImage image, ImageWriteParam writeParam) throws IOException
    {
        RenderedImage srcImage = image.getRenderedImage();
        
        if (writeParam == null) {
        	writeParam = getDefaultWriteParam();
        }
        
        int quality = 85;
        if (writeParam.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
          quality = (int) (writeParam.getCompressionQuality() * 100);
        }
        
        int componentSampling = TJ.SAMP_420;
        if (srcImage.getSampleModel().getNumBands() == 1) {
        	componentSampling = TJ.SAMP_GRAY;
	    }
        
        TJCompressor compressor = null;
        try
        {   
            BufferedImage inputImage = convertRenderedImage(srcImage);
            
            byte[] outputImageData;
            try {
                compressor = new TJCompressor();
                compressor.setSourceImage(inputImage, 0, 0, inputImage.getWidth(), inputImage.getHeight());
                compressor.setJPEGQuality(quality);
                compressor.setSubsamp(componentSampling);
                
                outputImageData = compressor.compress(TJ.FLAG_FASTDCT);
            } catch (Exception ex) {
                throw new IOException("Error in turbojpeg compressor: " + ex.getMessage(), ex);
            }            
            
            final int imageDataSize = compressor.getCompressedSize();
            
            outputStream.write(outputImageData, 0, imageDataSize);
        }
        finally
        {
            if(compressor != null) {
                try
                {
                    compressor.close();
                }
                catch (Exception t)
                {
                    LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
                }
            }
        }
    }
}
