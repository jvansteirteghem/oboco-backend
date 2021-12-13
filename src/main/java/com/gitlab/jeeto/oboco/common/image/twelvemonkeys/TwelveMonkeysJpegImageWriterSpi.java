package com.gitlab.jeeto.oboco.common.image.twelvemonkeys;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

public class TwelveMonkeysJpegImageWriterSpi extends ImageWriterSpi {
	private JPEGImageWriterSpi provider;
    private ImageWriterSpi delegateProvider;
    
    public TwelveMonkeysJpegImageWriterSpi(final ImageWriterSpi delegateProvider) {
        this.delegateProvider = delegateProvider;
    }
    
    @Override
    public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
    	try {
    		ImageWriterSpi defaultProvider = ImageWriterSpi.class.cast(registry.getServiceProviderByClass(Class.forName("com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi")));
    		
    		registry.deregisterServiceProvider(defaultProvider);
        } catch (Exception e) {
            // do nothing
        }
    	
    	provider = new JPEGImageWriterSpi(delegateProvider);
    	//System.out.println("register provider: " + provider);
    	registry.registerServiceProvider(provider);
    }

    @Override
    public String getVendorName() {
        return provider.getVendorName();
    }

    @Override
    public String getVersion() {
        return provider.getVersion();
    }

    @Override
    public ImageWriter createWriterInstance(final Object extension) throws IOException {
        return new JPEGImageWriter(provider, delegateProvider.createWriterInstance(extension));
    }

    @Override
    public String[] getFormatNames() {
        return provider.getFormatNames();
    }

    @Override
    public boolean isStandardStreamMetadataFormatSupported() {
        return provider.isStandardStreamMetadataFormatSupported();
    }

    @Override
    public String getNativeStreamMetadataFormatName() {
        return provider.getNativeStreamMetadataFormatName();
    }

    @Override
    public String[] getExtraStreamMetadataFormatNames() {
        return provider.getExtraStreamMetadataFormatNames();
    }

    @Override
    public boolean isStandardImageMetadataFormatSupported() {
        return provider.isStandardImageMetadataFormatSupported();
    }

    @Override
    public String getNativeImageMetadataFormatName() {
        return provider.getNativeImageMetadataFormatName();
    }

    @Override
    public String[] getExtraImageMetadataFormatNames() {
        return provider.getExtraImageMetadataFormatNames();
    }

    @Override
    public IIOMetadataFormat getStreamMetadataFormat(final String formatName) {
        return provider.getStreamMetadataFormat(formatName);
    }

    @Override
    public IIOMetadataFormat getImageMetadataFormat(final String formatName) {
        return provider.getImageMetadataFormat(formatName);
    }

    @Override
    public boolean canEncodeImage(final ImageTypeSpecifier type) {
        return provider.canEncodeImage(type);
    }

    @Override
    public boolean canEncodeImage(final RenderedImage im) {
        return provider.canEncodeImage(im);
    }

    @Override
    public String getDescription(final Locale locale) {
        return provider.getDescription(locale);
    }

    @Override
    public boolean isFormatLossless() {
        return provider.isFormatLossless();
    }

    @Override
    public Class[] getOutputTypes() {
        return provider.getOutputTypes();
    }
}
