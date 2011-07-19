package org.aklein.swap.util.spring;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Utility class to access the {@link ApplicationContext} of the current spring configuration.
 * 
 * @author Alexander Klein
 */
public class SpringContextFactory {
	private static ApplicationContext	context					= null;
	private static List<String>			preInstantiableBeans	= new ArrayList<String>();
	private static boolean				lazyInstantiating		= false;

	private SpringContextFactory() {
	}

	/**
	 * Get the {@link ApplicationContext} instance initialized with configuration in "conf/spring". All files in this
	 * directory will be loaded.
	 * 
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return SpringContextFactory.getContext(new File("conf/spring"));
	}

	/**
	 * Get the {@link ApplicationContext} instance initialized with the given configuration. If springConfig is a
	 * directory, all files in this directory will be loaded.
	 * 
	 * @param springConfig the location of the Spring configuration, may be a directory or a single file
	 * @return the context
	 */
	public static ApplicationContext getContext(String springConfig) {
		return SpringContextFactory.getContext(new File(springConfig));
	}

	/**
	 * Get the {@link ApplicationContext} instance initialized with configuration. If springConfig is a directory, all
	 * files in this directory will be loaded.
	 * 
	 * @param springConfig the location of the Spring configuration, may be a directory or a single file
	 * @return the context
	 */
	public static ApplicationContext getContext(File springConfig) {
		if (context == null) {
			String[] sources;
			File dir = springConfig.getAbsoluteFile();
			if (dir.isDirectory()) {
				File[] files = dir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}
				});
				sources = new String[files.length];
				for (int i = 0; i < files.length; i++)
					sources[i] = files[i].getAbsolutePath();
			}
			else
				sources = new String[] { dir.getAbsolutePath() };
			context = new FileSystemXmlApplicationContext() {
				@Override
				protected void loadBeanDefinitions(XmlBeanDefinitionReader aReader) throws IOException {
					super.loadBeanDefinitions(aReader);
					if (lazyInstantiating) {
						String[] beanDefinitionNames = aReader.getBeanFactory().getBeanDefinitionNames();
						for (String name : beanDefinitionNames) {
							AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) aReader.getBeanFactory().getBeanDefinition(name);
							if (!preInstantiableBeans.contains(name))
								beanDefinition.setLazyInit(true);
						}
					}
				}
			};
			((FileSystemXmlApplicationContext)context).setConfigLocations(sources);
			((FileSystemXmlApplicationContext)context).refresh();
		}
		return context;
	}

	/**
	 * @see BeanFactory#getBean(String)
	 */
	public static Object getBean(String s) {
		return context.getBean(s);
	}

	/**
	 * @see BeanFactory#getBean(String, Class)
	 */
	public static Object getBean(String s, Class<?> cls) {
		return context.getBean(s, cls);
	}

	/**
	 * @see BeanFactory#getBean(String, Object[])
	 */
	public static Object getBean(String s, Object... aobj) {
		return context.getBean(s, aobj);
	}

	/**
	 * @return the preInstantiableBeans
	 */
	public static List<String> getPreInstantiableBeans() {
		return preInstantiableBeans;
	}

	/**
	 * @return the lazyInstantiating
	 */
	public static boolean isLazyInstantiating() {
		return lazyInstantiating;
	}

	/**
	 * @param lazyInstantiating the lazyInstantiating to set
	 */
	public static void setLazyInstantiating(boolean lazyInstantiating) {
		SpringContextFactory.lazyInstantiating = lazyInstantiating;
	}

}
