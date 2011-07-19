package org.aklein.swap.util.configuration;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.aklein.swap.bus.MessageBus;
import org.aklein.swap.bus.channel.DefaultChannel;
import org.aklein.swap.bus.message.DefaultMessage;
import org.aklein.swap.security.ConfigurationBuilderProvider;
import org.aklein.swap.security.DefaultConfigurationBuilder;
import org.aklein.swap.security.XXmlConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Factory class to create configuration objects as {@link CombinedConfiguration}s
 * 
 * @author Alexander Klein
 * 
 */
public class ConfigurationFactory {
    private static Log log = LogFactory.getLog(ConfigurationFactory.class);

    @Autowired
    @Qualifier("messageBus")
    private MessageBus bus;

    @Autowired
    @Qualifier("configurationChannel")
    private DefaultChannel channel;

    @Autowired(required = false)
    @Qualifier("log4jProperties")
    private String log4jProperties = "log4j.properties";

    private Map<Configuration, File> reverseCache = new WeakHashMap<Configuration, File>();

    /**
     * Create a {@link CombinedConfiguration} by the loadFile.<br/> It also registers the ${message:class#key} lookup
     * for i18n messages.
     * 
     * @param loadFile
     *                The file to load the configuration from
     * @param basePackage
     *                To shorten the key length in the ${message:class#key} statements, this string is the prefix for
     *                the 'class' part when looking for the resources.
     * @return
     * @throws ConfigurationException
     * @see {@link MessageLookup}
     */
    public CombinedConfiguration createInstance(File loadFile, String basePackage) throws ConfigurationException {
        return createInstance(loadFile, basePackage, null);
    }
    /**
     * Create a {@link CombinedConfiguration} by the loadFile.<br/> It also registers the ${message:class#key} lookup
     * for i18n messages.
     * 
     * @param loadFile
     *                The file to load the configuration from
     * @param basePackage
     *                To shorten the key length in the ${message:class#key} statements, this string is the prefix for
     *                the 'class' part when looking for the resources.
     * @return
     * @throws ConfigurationException
     * @see {@link MessageLookup}
     */
    public CombinedConfiguration createInstance(File loadFile, String basePackage, ExpressionEngine engine) throws ConfigurationException {
	PropertyConfigurator.configure(log4jProperties);
	try {
	    // Register the ${message:...} lookup to load i18n messages.
	    ConfigurationInterpolator.registerGlobalLookup("message", new MessageLookup(basePackage));
	    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
		builder.addConfigurationProvider("configuration", new ConfigurationBuilderProvider()); 
		builder.addConfigurationProvider("xml", new DefaultConfigurationBuilder.FileConfigurationProvider(XXmlConfiguration.class)); 
	    builder.setFile(loadFile);
	    CombinedConfiguration cfg = builder.getConfiguration(true);
	    if(engine != null)
	        cfg.setExpressionEngine(engine);
	    reverseCache.put(cfg, loadFile);
	    // Inform modules about a loaded configuration
	    bus.broadcast(channel, new DefaultMessage("Configuration loaded"));
	    return cfg;
	} catch (ConfigurationException e) {
	    log.error("Could not read Configuration", e);
	    throw e;
	}
    }

    public File getBaseDirOfConfiguration(Configuration config) {
	return reverseCache.get(config);
    }
}
