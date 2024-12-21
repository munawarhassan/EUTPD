module tpd.euceg.backend.ws.stubs {

    exports eu.domibus.backend.ws;

    opens eu.domibus.backend.ws;

    exports eu.domibus.backend.ws.message;

    opens eu.domibus.backend.ws.message;

    exports eu.domibus.backend.ws.schema;

    opens eu.domibus.backend.ws.schema;

    requires java.xml.bind;

    requires java.xml.ws;

    requires jakarta.activation;

    requires java.jws;
}
