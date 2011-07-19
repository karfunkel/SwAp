package org.aklein.swap.examples.security;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EventObject;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.aklein.swap.security.XXmlConfiguration;
import org.aklein.swap.util.AlternativeResourceResourceManager;
import org.aklein.swap.util.spring.SpringContextFactory;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContextTool;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

public class SecurityManager extends SingleFrameApplication {
    private static Log log = LogFactory.getLog(SecurityManager.class);
    private static String sessionFile = null;

    public SecurityManager() {
	super();
	ApplicationContextTool.setResourceManager(getContext(), new AlternativeResourceResourceManager(getContext()));
    }

    public static void main(String[] args) {
	launch(SecurityManager.class, args);
    }

    private void initLookAndFeel() {
	// Use Window-Look of Look & Feel
	JFrame.setDefaultLookAndFeelDecorated(true);
	JDialog.setDefaultLookAndFeelDecorated(true);
	System.setProperty("sun.awt.noerasebackground", "true");

	try {
	    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
	} catch (Exception e) {
	    log.error("Could not initialize Look and Feel", e);
	}
    }

    @Override
    protected void startup() {
	initLookAndFeel();
	// try
	// {
	// XXmlConfiguration.getCryptTool().loadKeys(new BufferedReader(new FileReader(new
	// File("conf/security/keys/licence.keys"))));
	// }
	// catch (Exception e1)
	// {
	// log.error("Error loading keys", e1);
	// }
	SpringContextFactory.getContext("conf/security/spring");
	CombinedConfiguration config = (CombinedConfiguration) SpringContextFactory.getBean("config");
	((XMLConfiguration) config.getConfiguration("user")).setAutoSave(true);

	final ResourceMap resource = getContext().getResourceMap();
	addExitListener(new ExitListener() {
	    public boolean canExit(EventObject e) {
		boolean bOkToExit = false;
		Component source = (Component) e.getSource();
		bOkToExit = JOptionPane.showConfirmDialog(source, resource.getString("doExit.text"), resource.getString("doExit.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		return bOkToExit;
	    }

	    public void willExit(EventObject event) {
	    }
	});

	show((JPanel) SpringContextFactory.getBean("mainPanel"));
	getMainFrame().setBounds(getMainFrame().getX(), getMainFrame().getY(), resource.getInteger("width"), resource.getInteger("height"));

	try {
	    getContext().getSessionStorage().restore(getMainFrame(), getContext().getResourceMap().getString("sessionFile"));
	} catch (IOException e) {
	    log.warn("couldn't restore session", e);
	}
    }

    @Override
    protected void shutdown() {
	try {
	    getContext().getSessionStorage().save(getMainFrame(), getContext().getResourceMap().getString("sessionFile"));
	} catch (IOException e) {
	    log.warn("couldn't save session", e);
	}
    }
}
