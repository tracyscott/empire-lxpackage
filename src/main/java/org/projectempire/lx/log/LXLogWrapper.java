package org.projectempire.lx.log;

import heronarts.lx.LX;

public class LXLogWrapper {
    private final String LOG_PREFIX;

    public LXLogWrapper() {
        this.LOG_PREFIX = "[Empire] ";
    }

    public LXLogWrapper(String LOG_PREFIX) {
        this.LOG_PREFIX = LOG_PREFIX;
    }

    public boolean isDebugEnabled() {
        return LX.LOG_DEBUG;
    }

    public boolean isWarningEnabled() {
        return LX.LOG_WARNINGS;
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            LX.debug(LOG_PREFIX + message);
        }
    }

    public void error(String message) {
        LX.error(LOG_PREFIX + message);
    }

    public void error(Throwable t, String message) {
        LX.error(t, LOG_PREFIX + message);
    }

    public void log(String message) {
        LX.log(LOG_PREFIX + message);
    }

    public void warning(String message) {
        if (isWarningEnabled()) {
            LX.warning(LOG_PREFIX + message);
        }
    }
}
