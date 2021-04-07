package com.twelvemonkeys.imageio.plugins.jpeg;

import static com.twelvemonkeys.imageio.util.IIOUtil.deregisterProvider;
import static com.twelvemonkeys.imageio.util.IIOUtil.lookupProviderByName;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;

import com.twelvemonkeys.lang.Validate;

public class TurboMonkeysJPEGImageWriterSpi extends JPEGImageWriterSpi {
    private ImageWriterSpi delegateProvider;

    /**
     * Constructor for use by {@link javax.imageio.spi.IIORegistry} only.
     * The instance created will not work without being properly registered.
     */
    public TurboMonkeysJPEGImageWriterSpi() {
        super();
    }
    
    /**
     * Creates a {@code JPEGImageWriterSpi} with the given delegate.
     *
     * @param delegateProvider a {@code ImageWriterSpi} that can read JPEG.
     */
    public TurboMonkeysJPEGImageWriterSpi(final ImageWriterSpi delegateProvider) {
        this();

        this.delegateProvider = Validate.notNull(delegateProvider);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
        if (delegateProvider == null) {
            // Install delegate now
            delegateProvider = lookupProviderByName(registry, "com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi", ImageWriterSpi.class);
        }

        if (delegateProvider != null) {
            // Order before com.sun provider, to aid ImageIO in selecting our writer
            registry.setOrdering((Class<ImageWriterSpi>) category, this, delegateProvider);
        }
        else {
            // Or, if no delegate is found, silently deregister from the registry
            deregisterProvider(registry, this, category);
        }
    }

    @Override
    public String getVendorName() {
        return String.format("%s/%s", super.getVendorName(), delegateProvider.getVendorName());
    }

    @Override
    public String getVersion() {
        return String.format("%s/%s", super.getVersion(), delegateProvider.getVersion());
    }

    @Override
    public ImageWriter createWriterInstance(final Object extension) throws IOException {
        return new JPEGImageWriter(this, delegateProvider.createWriterInstance(extension));
    }

    @Override
    public String[] getFormatNames() {
        // NOTE: Can't use super.getFormatNames() which includes JPEG-Lossless
        return delegateProvider.getFormatNames();
    }

    @Override
    public boolean isStandardStreamMetadataFormatSupported() {
        return delegateProvider.isStandardStreamMetadataFormatSupported();
    }

    @Override
    public String getNativeStreamMetadataFormatName() {
        return delegateProvider.getNativeStreamMetadataFormatName();
    }

    @Override
    public String[] getExtraStreamMetadataFormatNames() {
        return delegateProvider.getExtraStreamMetadataFormatNames();
    }

    @Override
    public boolean isStandardImageMetadataFormatSupported() {
        return delegateProvider.isStandardImageMetadataFormatSupported();
    }

    @Override
    public String getNativeImageMetadataFormatName() {
        return delegateProvider.getNativeImageMetadataFormatName();
    }

    @Override
    public String[] getExtraImageMetadataFormatNames() {
        return delegateProvider.getExtraImageMetadataFormatNames();
    }

    @Override
    public IIOMetadataFormat getStreamMetadataFormat(final String formatName) {
        return delegateProvider.getStreamMetadataFormat(formatName);
    }

    @Override
    public IIOMetadataFormat getImageMetadataFormat(final String formatName) {
        return delegateProvider.getImageMetadataFormat(formatName);
    }

    @Override
    public boolean canEncodeImage(final ImageTypeSpecifier type) {
        return delegateProvider.canEncodeImage(type);
    }

    @Override
    public boolean canEncodeImage(final RenderedImage im) {
        return delegateProvider.canEncodeImage(im);
    }

    @Override
    public String getDescription(final Locale locale) {
        return delegateProvider.getDescription(locale);
    }

    @Override
    public boolean isFormatLossless() {
        return delegateProvider.isFormatLossless();
    }

    @Override
    public Class[] getOutputTypes() {
        return delegateProvider.getOutputTypes();
    }
}
