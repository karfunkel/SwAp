package org.aklein.swap.security;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.DefaultConfigurationBuilder.ConfigurationDeclaration;
import org.apache.commons.configuration.DefaultConfigurationBuilder.ConfigurationProvider;

/**
 * A specialized configuration provider class that allows to include other configuration definition files.
 */
public class ConfigurationBuilderProvider extends ConfigurationProvider
{
	/**
	 * Creates a new instance of <code>ConfigurationBuilderProvider</code>.
	 */
	public ConfigurationBuilderProvider()
	{
		super(DefaultConfigurationBuilder.class);
	}

	/**
	 * Creates the configuration. First creates a configuration builder object. Then returns the configuration created by this builder.
	 * 
	 * @param decl
	 *            the configuration declaration
	 * @return the configuration
	 * @exception Exception
	 *                if an error occurs
	 */
	public AbstractConfiguration getConfiguration(ConfigurationDeclaration decl) throws Exception
	{
		DefaultConfigurationBuilder builder = (DefaultConfigurationBuilder) super.getConfiguration(decl);
		builder.addConfigurationProvider("configuration", new ConfigurationBuilderProvider());
		builder.addConfigurationProvider("xml", new DefaultConfigurationBuilder.FileConfigurationProvider(XXmlConfiguration.class));
		return builder.getConfiguration(true);
	}

	/**
	 * Returns an empty configuration in case of an optional configuration could not be created. This implementation returns an empty combined configuration.
	 * 
	 * @param decl
	 *            the configuration declaration
	 * @return the configuration
	 * @exception Exception
	 *                if an error occurs
	 * @since 1.4
	 */
	public AbstractConfiguration getEmptyConfiguration(ConfigurationDeclaration decl) throws Exception
	{
		return new CombinedConfiguration();
	}
	
	
}
