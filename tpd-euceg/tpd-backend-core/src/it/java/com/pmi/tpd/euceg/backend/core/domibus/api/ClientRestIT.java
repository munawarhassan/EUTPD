package com.pmi.tpd.euceg.backend.core.domibus.api;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.backend.core.BackendProperties;
import com.pmi.tpd.euceg.backend.core.domibus.api.ClientRest;
import com.pmi.tpd.euceg.backend.core.domibus.api.model.MessageLogResponse;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ClientRestIT extends MockitoTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRestIT.class);

    @Mock
    private IApplicationProperties applicationProperties;

    private final I18nService i18nService = new SimpleI18nService();

    private ClientRest clientRest;

    @BeforeEach
    public void beforeEach() {
        assumeTrue(!Strings.isNullOrEmpty(System.getenv("MODE_ID")));
        when(applicationProperties.getConfiguration(BackendProperties.class)).thenReturn(BackendProperties.builder()
                .url("http://domibus-blue-192-168-1-175.traefik.me")
                .username("admin")
                .password("123456")
                .build());
        clientRest = new ClientRest(applicationProperties, i18nService);
        clientRest.initialize();
    }

    @Test
    public void test() {

        final MessageLogResponse messageLogs = clientRest.getMessageLogs("53a5d522-eb62-4941-94e7-dfe3f1da884a",
            PageUtils.newRequest(0, 100));

        LOGGER.info("entity: {}", messageLogs);
    }

}
