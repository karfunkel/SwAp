package org.aklein.swap.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.Key;
import java.util.HashSet;
import java.util.Set;

import org.aklein.swap.security.CryptTool.ReaderInputStream;
import org.aklein.swap.security.CryptTool.WriterOutputStream;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultConfigurationBuilder extends org.apache.commons.configuration.DefaultConfigurationBuilder {
	private static Set<File>	configFiles	= new HashSet<File>();
	private static Log			log			= LogFactory.getLog(DefaultConfigurationBuilder.class);

	@Override
	public void load(InputStream in) throws ConfigurationException {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		CryptTool cryptTool = XXmlConfiguration.getCryptTool();
		XXmlConfiguration.setEncrypting(this.getFile(), null);
		if (cryptTool.isEncrypted(in) && (cryptTool.getKeyCount() > 0)) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			Key key = cryptTool.decrypt(in, buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toByteArray());
			buffer = null;
			super.load(bis);
			XXmlConfiguration.setEncrypting(this.getFile(), key);
		}
		else
			super.load(in);
	}

	@Override
	public void load(Reader in) throws ConfigurationException {
		load(new ReaderInputStream(in));
	}

	@Override
	public void save(Writer writer) throws ConfigurationException {
		save(writer, XXmlConfiguration.isEncrypted(this.getFile()));
	}

	public void save(Writer writer, boolean encrypted) throws ConfigurationException {
		CryptTool cryptTool = XXmlConfiguration.getCryptTool();
		if (encrypted && (cryptTool.getDefaultEncryptionKey() != null)) {
			StringWriter pw = new StringWriter();
			super.save(pw);
			ByteArrayInputStream bis = new ByteArrayInputStream(pw.toString().getBytes());
			cryptTool.interactiveEncryption(bis, new WriterOutputStream(writer), cryptTool.getDefaultEncryptionKey());
		}
		else {
			super.save(writer);
		}
	}

	@Override
	protected FileConfigurationDelegate createDelegate() {
		return new FileConfigurationDelegate() {
			@Override
			public void load(Reader in) throws ConfigurationException {
				DefaultConfigurationBuilder.this.load(in);
			}

			@Override
			public void save(Writer out) throws ConfigurationException {
				DefaultConfigurationBuilder.this.save(out);
			}
		};
	}

	@Override
	public CombinedConfiguration getConfiguration(boolean load) throws ConfigurationException {
		CombinedConfiguration cfg = super.getConfiguration(load);
		configFiles.add(getDelegate().getFile());
		return cfg;
	}

	public static Set<File> getConfigFiles() {
		return configFiles;
	}

}
