package com.pmi.tpd.web.core.support;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public final class WebHelper {

    /**
     * @return
     */
    public static Optional<HttpServletRequest> currentHttpRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            final HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return Optional.of(request);
        }
        return Optional.empty();
    }

}
