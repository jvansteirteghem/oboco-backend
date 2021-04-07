package com.twelvemonkeys.imageio.plugins.jpeg;

import static com.twelvemonkeys.imageio.util.IIOUtil.lookupProviderByName;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;

import com.twelvemonkeys.imageio.spi.ImageReaderSpiBase;
import com.twelvemonkeys.imageio.spi.ReaderWriterProviderInfo;
import com.twelvemonkeys.imageio.util.IIOUtil;
import com.twelvemonkeys.lang.Validate;

public class TurboMonkeysJPEGImageReaderSpi extends ImageReaderSpiBase {
    protected ImageReaderSpi delegateProvider;

    /**
     * Constructor for use by {@link javax.imageio.spi.IIORegistry} only.
     * The instance created will not work without being properly registered.
     */
    public TurboMonkeysJPEGImageReaderSpi() {
        this(new JPEGProviderInfo());
    }

    /**
     * Creates a {@code JPEGImageReaderSpi} with the given delegate.
     *
     * @param delegateProvider a {@code ImageReaderSpi} that can read JPEG.
     */
    public TurboMonkeysJPEGImageReaderSpi(final ImageReaderSpi delegateProvider) {
        this();

        this.delegateProvider = Validate.notNull(delegateProvider);
    }

    private TurboMonkeysJPEGImageReaderSpi(final ReaderWriterProviderInfo info) {
        super(info);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public void onRegistration(final ServiceRegistry registry, final Class<?> category) {
        if (delegateProvider == null) {
            // Install delegate now
            delegateProvider = lookupProviderByName(registry, "com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi", ImageReaderSpi.class);
        }

        if (delegateProvider != null) {
            // Order before com.sun provider, to aid ImageIO in selecting our reader
            registry.setOrdering((Class<ImageReaderSpi>) category, this, delegateProvider);
        }
        else {
            // Or, if no delegate is found, silently deregister from the registry
            IIOUtil.deregisterProvider(registry, this, category);
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
    public ImageReader createReaderInstance(final Object extension) throws IOException {
        return new JPEGImageReader(this, delegateProvider.createReaderInstance(extension));
    }

    @Override
    public boolean canDecodeInput(final Object source) throws IOException {
        return delegateProvider.canDecodeInput(source);
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
    public String getDescription(final Locale locale) {
        return delegateProvider.getDescription(locale);
    }

    @Override
    public Class[] getInputTypes() {
        return delegateProvider.getInputTypes();
    }
}
