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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.libjpegturbo.turbojpeg.TJLoader;

/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 * 
 *         Class containing some methods ported from the TurboJPEG C code as well as lib availability check.
 * 
 */
public class TurboJpegUtilities {

    private static final Logger LOGGER = Logger.getLogger(TurboJpegUtilities.class.getName());

    private static boolean isAvailable;

    private static boolean isInitialized = false;

    public static boolean isTurboJpegAvailable() {

        loadTurboJpeg();
        return isAvailable;
    }

    public static void loadTurboJpeg() {
        if (isInitialized) {
            return;
        }
        synchronized (LOGGER) {
            if (isInitialized) {
                return;
            }
            try {
                load();
                isAvailable = true;

            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("Failed to load the TurboJpeg native libs."
                            + " This is not a problem, but the TurboJpeg encoder won't be available: " + t.toString());
                }
            } finally {
                isInitialized = true;
            }
        }

    }
    
    static void load() {
    	TJLoader.load();
    }
}
