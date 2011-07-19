package org.aklein.swap.examples.security.resources;

import java.awt.Window;
import java.io.File;

public interface SecurityUserManager 
{
	public void setParent(Window window);
	
	public void setKeyFolder(File folder);
	
	public void setUserFolder(File folder);	
}
