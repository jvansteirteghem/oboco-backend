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

import java.awt.Dimension;
import java.awt.Graphics2D;
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
import org.libjpegturbo.turbojpeg.TJDecompressor;
import org.libjpegturbo.turbojpeg.TJScalingFactor;


/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Simone Giannecchini, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 */
public class TurboJpegImageWriter extends ImageWriter
{

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.turbojpeg");

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
    
    // https://stackoverflow.com/questions/11959758/java-maintaining-aspect-ratio-of-jpanel-background-image/11959928#11959928
	
 	private double calculateScaleFactor(int originalSize, int targetSize) {
 		double scaleFactor = 1;
 		
 		if(originalSize > targetSize) {
 			scaleFactor = (double) targetSize / (double) originalSize;
 		}
 		
 		return scaleFactor;

 	}
    
    private TJScalingFactor getScalingFactor(BufferedImage inputImage, Integer outputScaleWidth, Integer outputScaleHeight) {
    	Dimension originalSize = new Dimension(inputImage.getWidth(), inputImage.getHeight());
    	Dimension targetSize = new Dimension(outputScaleWidth, outputScaleHeight);
    	
		double scaleFactor = 1d;
		
		if(originalSize != null && targetSize != null) {
			double scaleWidth = calculateScaleFactor(originalSize.width, targetSize.width);
			double scaleHeight = calculateScaleFactor(originalSize.height, targetSize.height);
			
			scaleFactor = Math.max(scaleWidth, scaleHeight);
		}
		
		scaleFactor = Math.min(1d, scaleFactor);
		
		LOGGER.log(Level.INFO, "scaleFactor=" + scaleFactor);
		
		TJScalingFactor tjScalingFactor = null;
		TJScalingFactor[] tjScalingFactors = TJ.getScalingFactors();
		for(int i = 0; i < tjScalingFactors.length; i = i + 1) {
			double tjScaleFactor = (double) tjScalingFactors[i].getNum() / (double) tjScalingFactors[i].getDenom();
			
			LOGGER.log(Level.INFO, "tjScalingFactor-" + i + ":" + tjScalingFactors[i].getNum() + "/" + tjScalingFactors[i].getDenom() + "=" + tjScaleFactor);
		    
		    if(tjScaleFactor == scaleFactor) {
		    	tjScalingFactor = tjScalingFactors[i];
		    	break;
		    } else if(tjScaleFactor < scaleFactor) {
		    	if(i > 0) {
		    		tjScalingFactor = tjScalingFactors[i - 1];
		    	}
		    	break;
		    }
		}
		
		if(tjScalingFactor == null) {
		    	throw new IllegalArgumentException("ScalingFactor not supported.");
		}
		
		double tjScaleFactor = (double) tjScalingFactor.getNum() / (double) tjScalingFactor.getDenom();
		
		LOGGER.log(Level.INFO, "tjScalingFactor:" + tjScalingFactor.getNum() + "/" + tjScalingFactor.getDenom() + "=" + tjScaleFactor);
		
		return tjScalingFactor;
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
                
                if(writeParam instanceof TurboJpegImageWriteParam) {
                	TurboJpegImageWriteParam tjWriteParam = (TurboJpegImageWriteParam) writeParam;
	    			
                	int outputScaleWidth = tjWriteParam.getScaleWidth();
            		int outputScaleHeight = tjWriteParam.getScaleHeight();
            		
                	if(outputScaleWidth != 0 || outputScaleHeight != 0) {
            			if(outputScaleWidth == 0) {
            				outputScaleWidth = inputImage.getWidth();
            			}
            			
            			if(outputScaleHeight == 0) {
            				outputScaleHeight = inputImage.getHeight();
            			}
            			
            			LOGGER.log(Level.INFO, "outputScaleWidth=" + outputScaleWidth + ",outputScaleHeight=" + outputScaleHeight);
            			
            			TJScalingFactor tjScalingFactor = getScalingFactor(inputImage, outputScaleWidth, outputScaleHeight);
                		
                		if(tjScalingFactor.isOne() == false) {
                			int scaleWidth = tjScalingFactor.getScaled(inputImage.getWidth());
                			int scaleHeight = tjScalingFactor.getScaled(inputImage.getHeight());
                			
                			LOGGER.log(Level.INFO, "scaleWidth=" + scaleWidth + ",scaleHeight=" + scaleHeight);
                			
                			TJDecompressor decompressor = new TJDecompressor();
    		    			decompressor.setSourceImage(outputImageData, outputImageData.length);
    		    			inputImage = decompressor.decompress(scaleWidth, scaleHeight, inputImage.getType(), TJ.FLAG_FASTUPSAMPLE);
    		    			
    		    			int regionX = (scaleWidth - outputScaleWidth) / 2;
                			int regionY = (scaleHeight - outputScaleHeight) / 2;
                			int regionWidth = scaleWidth - (scaleWidth - outputScaleWidth);
                			int regionHeight = scaleHeight - (scaleHeight - outputScaleHeight);
                			
                			LOGGER.log(Level.INFO, "regionX=" + regionX + ",regionY=" + regionY + ",regionWidth=" + regionWidth + ",regionHeight=" + regionHeight);
    		    			
    		    			compressor = new TJCompressor();
    		                compressor.setSourceImage(inputImage, regionX, regionY, regionWidth, regionHeight);
    		                compressor.setJPEGQuality(quality);
    		                compressor.setSubsamp(componentSampling);
    		                
    		                outputImageData = compressor.compress(TJ.FLAG_FASTDCT);
                		}
                	}
                }
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
