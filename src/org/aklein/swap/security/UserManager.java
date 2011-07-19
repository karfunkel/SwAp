package org.aklein.swap.security;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import org.aklein.swap.security.User;

public interface UserManager<T extends User>
{
	public Key getMasterKey(String name, InputStream user);

	public Boolean isValid(String name, InputStream user, Key masterKey, boolean force);

	public String getUserInfos(String name, InputStream user, Key masterKey);

	public String getUserInfos(T user, Key masterKey);

	public T createUser(Key masterKey);

	public boolean editUser(String name, InputStream user, Key masterKey);

	public boolean removeUser(String name, Key masterKey);

	public boolean saveUser(T user, OutputStream target, Key masterKey);

	public T getUser(String name, InputStream user, Key masterKey);
	
	public T authenticateUser(String name, InputStream user, Key key);

}
