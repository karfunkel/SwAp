package org.aklein.swap.util;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScriptingWrapper {
    private Class<?> scriptEngineManagerClass;
    private boolean useJSR223 = true;
    private Object scriptEngineManager;
    private Object scriptEngine;
    private String language;
    private BSFManager bsfMgr = new BSFManager();
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(ScriptingWrapper.class);

    public ScriptingWrapper(String language) {
	this.language = language;
	try {
	    scriptEngineManagerClass = Class.forName("javax.script.ScriptEngineManager");
	    scriptEngineManager = scriptEngineManagerClass.newInstance();
	    Method getEngineByExtension = scriptEngineManagerClass.getMethod("getEngineByName", String.class);
	    scriptEngine = getEngineByExtension.invoke(scriptEngineManager, language);
	} catch (Exception e) {
	    useJSR223 = false;
	}
    }

    public void put(String key, Object value) {
	try {
	    if (useJSR223) {
		Method put = scriptEngine.getClass().getMethod("put", String.class, Object.class);
		put.invoke(scriptEngine, key, value);
	    } else {
		bsfMgr.declareBean(key, value, value.getClass());
	    }
	} catch (Exception e) {
	    log.error("Error putting value into context", e);
	}
    }

    public Object get(String key) {
	try {
	    if (useJSR223) {
		Method get = scriptEngine.getClass().getMethod("get", String.class);
		return get.invoke(scriptEngine, key);
	    } else {
		return bsfMgr.lookupBean(key);
	    }
	} catch (Exception e) {
	    log.error("Error getting value from context", e);
	    return null;
	}
    }

    public Object eval(String script, String name) throws Exception {
	try {
	    if (useJSR223) {
		Method eval = scriptEngine.getClass().getMethod("eval", String.class);
		return eval.invoke(scriptEngine, script);
	    } else {
		return bsfMgr.eval(language, name, -1, -1, script);
	    }
	} catch (Exception e) {
	    log.error("Error evaluating script: " + name, e);
	    return null;
	}
    }

}
