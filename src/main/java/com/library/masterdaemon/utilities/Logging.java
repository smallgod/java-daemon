/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.library.masterdaemon.utilities;

import org.slf4j.Logger;

/**
 *
 * @author smallgod
 */
public final class Logging {

    private final Logger LOGGER;

    public Logging(final Logger LOGGER) {
        this.LOGGER = LOGGER;
    }

    public void debug(String debugLogText) {

        if (this.LOGGER != null) {
            this.LOGGER.debug(debugLogText);
        } else {
            System.out.println("DEBUG: " + debugLogText);
        }
    }

    public void info(String infoLogText) {

        if (LOGGER != null) {
            LOGGER.info(infoLogText);
        } else {
            System.out.println("INFO: " + infoLogText);
        }
    }

    public void error(String errorLogText) {

        if (LOGGER != null) {
            LOGGER.error(errorLogText);
        } else {
            System.err.println("ERROR: " + errorLogText);
        }
    }

    public void warn(String warnLogText) {

        if (LOGGER != null) {
            LOGGER.warn(warnLogText);
        } else {
            System.out.println("WARNING: " + warnLogText);
        }
    }

    public void fatal(String fatalLogText) {

        if (LOGGER != null) {
            LOGGER.error(fatalLogText);
            System.exit(-1);
        } else {
            System.err.println("FATAL: " + fatalLogText);
            System.exit(-1);
        }
    }

}
