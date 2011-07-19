package org.aklein.swap.security;

import java.awt.Container;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Map;

import org.aklein.swap.ui.ViewController;
import org.apache.commons.configuration.ConfigurationException;

public interface KeyWallet
{
	public boolean exists(String data);

	public boolean isLoaded();

	public String keyInWallet(String data);

	public void addToWallet(String key, String data);

	public void setWalletValue(String key, String data);

	public void removeFromWallet(String key);

	public void loadWallet(InputStream data);

	public void saveWallet(OutputStream target);

	public Key parseKey(String key) throws GeneralSecurityException, ConfigurationException, IOException;

	public ViewController<? extends Container> getParent();

	public Map<String, String> getEntries();

	public boolean isEncrypted();

	public void setEncrypted(Key key);

}
