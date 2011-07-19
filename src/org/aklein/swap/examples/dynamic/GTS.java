package org.aklein.swap.examples.dynamic;

import java.awt.Component;
import java.io.IOException;
import java.util.EventObject;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.aklein.swap.util.AlternativeResourceResourceManager;
import org.aklein.swap.util.spring.SpringContextFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.ApplicationContextTool;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;

public class GTS extends SingleFrameApplication {
    private static Log log = LogFactory.getLog(GTS.class);

    public GTS() {
	super();
	ApplicationContextTool.setResourceManager(getContext(), new AlternativeResourceResourceManager(getContext()));
    }

    public static void main(String[] args) {
	launch(GTS.class, args);
    }

    private void initLookAndFeel(Configuration config) {
	// Use Window-Look of Look & Feel
	if (config.getBoolean("theme.decorated")) {
	    JFrame.setDefaultLookAndFeelDecorated(true);
	    JDialog.setDefaultLookAndFeelDecorated(true);
	    System.setProperty("sun.awt.noerasebackground", "true");
	}

	// Use Substance-L&F and read theme from config
	try {
	    UIManager.setLookAndFeel(config.getString("theme.lookandfeel", "javax.swing.plaf.metal.MetalLookAndFeel"));
	} catch (Exception e) {
	    log.error("Could not initialize Look and Feel", e);
	}
    }

    @Override
    protected void startup() {
	SpringContextFactory.getContext("conf/dynamic/spring");

	initLookAndFeel((Configuration) SpringContextFactory.getBean("config"));

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
