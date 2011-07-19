package org.aklein.swap.util;

import java.text.MessageFormat;

import org.jdesktop.application.Application;

public class MessageUtil {

    public static String formatString(Class<?> cls, String key, Object... parameter) {
	String pattern = Application.getInstance().getContext().getResourceMap(cls).getString(key);
	return MessageFormat.format(pattern, parameter);
    }
}
