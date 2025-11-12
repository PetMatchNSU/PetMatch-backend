package org.nsu.config.logback;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.IThrowableProxy;

import static org.apache.commons.text.StringEscapeUtils.escapeJson;

public class CompressedStackTraceConverter extends ThrowableProxyConverter {

    @Override
    protected String throwableProxyToString(IThrowableProxy tp) {
        String original = super.throwableProxyToString(tp);
        return escapeJson(original);
    }
}
