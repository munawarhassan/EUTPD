module tpd.euceg.plugin.ws.stubs {

    exports eu.domibus.plugin.ws;

    opens eu.domibus.plugin.ws;

    exports eu.domibus.plugin.ws.message;

    opens eu.domibus.plugin.ws.message;

    exports eu.domibus.plugin.ws.schema;

    opens eu.domibus.plugin.ws.schema;

    requires java.jws;

    requires java.xml.bind;

    requires java.xml.ws;

    requires jakarta.activation;

}
