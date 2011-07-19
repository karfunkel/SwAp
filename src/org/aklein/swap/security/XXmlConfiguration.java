package org.aklein.swap.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.aklein.swap.security.CryptTool.ReaderInputStream;
import org.aklein.swap.security.CryptTool.WriterOutputStream;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XXmlConfiguration extends XMLConfiguration
{
	protected static final Map<File, Key> encryptedMap = new HashMap<File, Key>();
	protected boolean origEncrypted = false;
	private static Log log = LogFactory.getLog(XXmlConfiguration.class);
	private static CryptTool cryptTool = new CryptTool();

	public XXmlConfiguration()
	{
		super();
	}

	public XXmlConfiguration(HierarchicalConfiguration c)
	{
		super(c);
		clearReferences(getRootNode());
	}

	public XXmlConfiguration(String fileName) throws ConfigurationException
	{
		super(fileName);
	}

	public XXmlConfiguration(File file) throws ConfigurationException
	{
		super(file);
	}

	public XXmlConfiguration(URL url) throws ConfigurationException
	{
		super(url);
	}

	public static CryptTool getCryptTool()
	{
		return cryptTool;
	}

	public static void setCryptTool(CryptTool cryptTool)
	{
		XXmlConfiguration.cryptTool = cryptTool;
	}

	public boolean isOrigEncrypted()
	{
		return origEncrypted;
	}

	public void setOrigEncrypted(boolean origEncrypted)
	{
		this.origEncrypted = origEncrypted;
	}

	public static boolean isEncrypted(File file)
	{
		Key k = encryptedMap.get(file);
		if (k == null)
			return false;
		return true;
	}

	public boolean isEncrypted()
	{
		return encryptedMap.get(this.getFile()) != null;
	}

	public void load(InputStream in) throws ConfigurationException
	{
		if(!in.markSupported()){
			in = new BufferedInputStream(in);
		}
		if (cryptTool.isEncrypted(in) && (cryptTool.getKeyCount() > 0))
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			Key key = cryptTool.decrypt(in, buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toByteArray());
			buffer = null;
			encryptedMap.put(this.getFile(), key);
			super.load(bis);
		}
		else
		{
			encryptedMap.remove(this.getFile());
			super.load(in);
		}
	}

	public void load(Reader in) throws ConfigurationException
	{
		load(new ReaderInputStream(in));
	}

	public static void setEncrypting(File file, Key key)
	{
		if (key == null)
			encryptedMap.remove(file);
		else
			encryptedMap.put(file, key);
	}

	public void save(Writer writer) throws ConfigurationException
	{
		Key key = encryptedMap.get(this.getFile());
		if (!save(writer, key))
			encryptedMap.remove(this.getFile());
	}

	public boolean save(OutputStream target, Key key) throws ConfigurationException
	{
		return save(new OutputStreamWriter(target), key);
	}

	public boolean save(Writer writer, Key key) throws ConfigurationException
	{
		if (key != null)
		{
			StringWriter buffer = new StringWriter();
			super.save(buffer);
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer.toString().getBytes());
			buffer = null;
			cryptTool.encrypt(bis, key, new WriterOutputStream(writer), true);			
			return true;
		}
		else
		{
			super.save(writer);
			return false;
		}
	}
}
