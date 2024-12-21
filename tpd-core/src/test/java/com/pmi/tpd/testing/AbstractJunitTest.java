package com.pmi.tpd.testing;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public abstract class AbstractJunitTest extends MockitoTestCase {

    /** */
    protected static final File PATH_HOME = new File(getBasedir(), "target/app-home");

    /** */
    protected static final File APP_HOME = new File(getBasedir(), "target/app");

    /** */
    protected static final File CONF_HOME = new File(APP_HOME, "conf");

    /** */
    private static String basedir;

    /** */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractJunitTest() {
        super();
    }

    public static String getBasedir() {
        if (basedir != null) {
            return basedir;
        }

        basedir = System.getProperty("basedir");

        if (basedir == null) {
            basedir = new File("").getAbsolutePath();
        }

        return basedir;
    }

    @BeforeAll
    public static void init() throws Exception {
        basedir = getBasedir();
    }

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty(ApplicationConstants.PropertyKeys.HOME_PATH_SYSTEM_PROPERTY, PATH_HOME.getAbsolutePath());
        System.setProperty(ApplicationConstants.PropertyKeys.APPLICATION_PATH_PROPETY, APP_HOME.getAbsolutePath());

        FileSystemUtils.deleteRecursively(PATH_HOME);
        FileSystemUtils.deleteRecursively(APP_HOME);

        PATH_HOME.mkdirs();
        APP_HOME.mkdirs();
        CONF_HOME.mkdirs();
    }
}
