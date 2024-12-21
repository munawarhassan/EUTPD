package com.pmi.tpd.keystore;

import org.springframework.core.io.Resource;

import com.pmi.tpd.api.config.annotation.ConfigurationProperties;
import com.pmi.tpd.api.config.annotation.NoPersistent;

import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 * {@code
 *   app.security:
 *     keystore:
 *       location: keystore file path
 *       password: "sklédf23094324m330"
 *       notification:
 *         email:
 *           contact: john.cook@company.com
 *         expiration:
 *           enable: false|true
 *           # Controls how frequently the job to check expiration is run.
 *           # Value is in DAYS.
 *           interval:
 *           # period before send notification. Value is in DAYS
 *           threshold:
 *           # period to remind expiration. value is in DAYS
 *           reminder:
 * }
 * </pre>
 *
 * @author Christophe Friederich
 * @since 2.2
 */
@Getter
@Setter
@ConfigurationProperties("app.security.keystore")
public class KeyStoreProperties {

    @NoPersistent
    private Resource defaultLocation;

    /** */
    @NoPersistent
    private Resource location;

    /** */
    @NoPersistent
    private String password;

    /** */
    private final Notification notification = new Notification();

    /**
     * @author Christophe Friederich
     * @since 2.2
     */
    @Getter
    @Setter
    public static class Notification {

        /** */
        private String contact;

        /** */
        private final NotificationExpiration expiration = new NotificationExpiration();

    }

    /**
     * @author Christophe Friederich
     * @since 2.2
     */
    @Getter
    @Setter
    public static class NotificationExpiration {

        /** */
        private boolean enable;

        /** **/
        @NoPersistent
        private int interval;

        /** */
        @NoPersistent
        private int threshold;

        /** */
        @NoPersistent
        private int reminder;

    }
}
