package org.jenkinsci.plugins.liquibase.common;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.jenkinsci.plugins.liquibase.builder.EmbeddedDriver;
import org.jenkinsci.plugins.liquibase.builder.LiquibaseBuilder;
import org.jenkinsci.plugins.liquibase.builder.LiquibaseProperty;
import org.jenkinsci.plugins.liquibase.builder.LiquibaseRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class PropertiesParser {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesParser.class);



    /**
     * Creates a properties instance representing liquibase configuration elements.  Will first attempt to load
     * based on {@link org.jenkinsci.plugins.liquibase.builder.LiquibaseBuilder#liquibasePropertiesPath}, then
     * on any elements set directly on the builder.  Therefore, those set "manually" take precedence over those
     * defined in the external file.
     * @param liquibaseBuilder
     * @return
     */
    public static Properties createConfigProperties(LiquibaseBuilder liquibaseBuilder) {
        Properties properties = new Properties();

        String liquibasePropertiesPath = liquibaseBuilder.getLiquibasePropertiesPath();
        if (!Strings.isNullOrEmpty(liquibasePropertiesPath)) {
            try {
                properties.load(new FileReader(liquibasePropertiesPath));
            } catch (IOException e) {
                throw new LiquibaseRuntimeException(
                        "Unable to load properties file at[" + liquibasePropertiesPath + "] ", e);
            }
        }
        setBasedOnConfiguration(liquibaseBuilder, properties);

        return properties;
    }

    private static void setBasedOnConfiguration(LiquibaseBuilder liquibaseBuilder, Properties properties) {
        setDriverFromDBEngine(liquibaseBuilder, properties);
        setIfNotNull(properties, LiquibaseProperty.USERNAME, liquibaseBuilder.getUsername());
        setIfNotNull(properties, LiquibaseProperty.DEFAULT_SCHEMA_NAME, liquibaseBuilder.getDefaultSchemaName());
        setIfNotNull(properties, LiquibaseProperty.PASSWORD, liquibaseBuilder.getPassword());
        setIfNotNull(properties, LiquibaseProperty.URL, liquibaseBuilder.getUrl());
        setIfNotNull(properties, LiquibaseProperty.CHANGELOG_FILE, liquibaseBuilder.getChangeLogFile());
        setIfNotNull(properties, LiquibaseProperty.CONTEXTS, liquibaseBuilder.getContexts());
    }

    private static void setIfNotNull(Properties properties,
                                     LiquibaseProperty liquibaseProperty,
                                     String value) {
        if (!Strings.isNullOrEmpty(value)) {
            properties.setProperty(liquibaseProperty.getOptionName(), value);
        }

    }

    private static void setDriverFromDBEngine(LiquibaseBuilder liquibaseBuilder, Properties properties) {
        if (!Strings.isNullOrEmpty(liquibaseBuilder.getDatabaseEngine())) {
            for (EmbeddedDriver embeddedDriver : liquibaseBuilder.getDrivers()) {
                if (embeddedDriver.getDisplayName().equals(liquibaseBuilder.getDatabaseEngine())) {
                    properties.setProperty("driver", embeddedDriver.getDriverClassName());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("using db driver class[" + embeddedDriver.getDriverClassName() + "] ");
                    }
                    break;
                }
            }
        }
    }

}
