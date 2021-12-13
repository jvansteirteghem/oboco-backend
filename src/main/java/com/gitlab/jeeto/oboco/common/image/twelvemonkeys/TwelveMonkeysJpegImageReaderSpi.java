package com.gitlab.jeeto.oboco.common.image.twelvemonkeys;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;

public class TwelveMonkeysJpegImageReaderSpi extends ImageReaderSpi {
	private JPEGImageReaderSpi provider;
	private ImageReaderSpi delegateProvider;
    
    public TwelveMonkeysJpegImageReaderSpi(final ImageReaderSpi delegateProvider) {
        this.delegateProvider = delegateProvider;
    }
    
    @Override
    public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
    	try {
    		ImageReaderSpi defaultProvider = ImageReaderSpi.class.cast(registry.getServiceProviderByClass(Class.forName("com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi")));
    		
    		registry.deregisterServiceProvider(defaultProvider);
        } catch (Exception e) {
            // do nothing
        }
    	
    	provider = new JPEGImageReaderSpi(delegateProvider);
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
    public ImageReader createReaderInstance(final Object extension) throws IOException {
        return new JPEGImageReader(provider, delegateProvider.createReaderInstance(extension));
    }

    @Override
    public boolean canDecodeInput(final Object source) throws IOException {
        return provider.canDecodeInput(source);
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
    public String getDescription(final Locale locale) {
        return provider.getDescription(locale);
    }

    @Override
    public Class[] getInputTypes() {
        return provider.getInputTypes();
    }
}
