/*
 * Created on 05.06.2006
 *
 */
package org.aklein.swap.util.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.JTextComponent;

import org.aklein.swap.util.MessageKeyProvider;
import org.aklein.swap.util.ScriptingWrapper;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.view.ValidationComponentUtils;

/**
 * {@link Validator} that is configured by configuration
 * 
 * see conf/validation.xml for more information
 * 
 * @author AKlein
 * 
 * @param <T>
 */
public class CustomValidator<T, V extends MessageKeyProvider> implements Validator<T> {
    public static final String ERROR_TARGET = "error";
    public static final String INFO_TARGET = "info";
    private static Log log = LogFactory.getLog(CustomValidator.class);

    private String formKey;
    private V view;
    private List<RuleSet> rulesets;
    private Configuration config;    

    public CustomValidator(V view, String formKey, File file) throws ConfigurationException {
	DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
	builder.setFile(file);
	this.config = builder.getConfiguration(true);
	this.view = view;
	this.formKey = formKey;
	reloadRuleSets();
    }

    public CustomValidator(V view, String formKey, Configuration configuration) {
	this.config = configuration;
	this.view = view;
	this.formKey = formKey;
	reloadRuleSets();
    }

    public void reloadRuleSets() {
	rulesets = new ArrayList<RuleSet>();
	fillRuleSets();
    }

    public ValidationResult validate() {
	return validate(null);
    }

    /**
     * Method to validate against all RuleSets by running all validation RuleSets
     * 
     * @param validationTarget
     *            not used
     */
    public ValidationResult validate(T validationTarget) {
	if (rulesets.size() < 1)
	    return QueueRegistry.getQueue(ERROR_TARGET, view);
	// traverse all RuleSets
	for (RuleSet ruleset : rulesets) {
	    // Do not validate invisible, disabled, and notEditable components
	    if (!ruleset.getComponent().isVisible() || !ruleset.getComponent().isEnabled()
		    || ((ruleset.getComponent() instanceof JTextComponent) && !((JTextComponent) ruleset.getComponent()).isEditable())
		    || ((ruleset.getComponent() instanceof JFormattedTextField) && !((JFormattedTextField) ruleset.getComponent()).isEditable()))
		continue;

	    String key = ruleset.getKey();
	    // traverse all Rules in RuleSet
	    for (Rule rule : ruleset.rules) {
		// remove all existing messages of this Rule
		if (QueueRegistry.checkForMessage(ERROR_TARGET, view, key, rule.getKey()))
		    QueueRegistry.removeMessage(ERROR_TARGET, view, key, rule.getKey());

		// run the corresponding validationscript
		if ((rule.getValidator() != null) && (rule.getValidator().length() > 0)) {
		    try {
			// set script variables
			ScriptingWrapper bsm = new ScriptingWrapper(rule.getLanguage());
			bsm.put("view", view);
			bsm.put("component", ruleset.getComponent());
			bsm.put("key", key);
			bsm.put("ruleKey", rule.getKey());
			bsm.put("message", rule.getMessage());
			if ((rule.getInit() != null) && (rule.getInit().length() > 0))
			    bsm.eval(rule.getInit(),"Rule-Init: "+rule.getKey());
			bsm.eval(rule.getValidator(), "Rule-Validator: "+rule.getKey());
		    } catch (Exception e) {
			log.error("Error running Validation-Script: " + key, e);
		    }
		}
	    }
	}
	return QueueRegistry.getQueue(ERROR_TARGET, view);
    }

    @SuppressWarnings("unchecked")
    private void fillRuleSets() {
	if (config != null) {
	    // get all rules for the form
	    String prefix = "validation." + formKey + ".ruleset";
	    List<String> list = (List<String>) config.getList(prefix + ".key", new ArrayList<String>());
	    for (int i = 0; i < list.size(); i++) {
		// create new RuleSet and add it to the list of RuleSets
		RuleSet ruleset = RuleSet.parse(prefix + "(" + i + ")", formKey, view, config);
		if (ruleset != null)
		    rulesets.add(ruleset);
	    }
	}
    }

    /**
     * Specification of a logical RuleSet, e.g. a set validating a field or logical unit
     * 
     * @author Alexander Klein
     * 
     */
    public static class RuleSet {
	String key;
	JComponent view;
	String hint;
	List<Rule> rules;

	/**
	 * Parsing data from configuration
	 */
	@SuppressWarnings("unchecked")
	public static RuleSet parse(String base, String ruleSetBase, MessageKeyProvider view, Configuration config) {
	    String prefix = base;
	    RuleSet ruleset = null;
	    String key = config.getString(prefix + ".key");
	    if (key != null)
		key = ruleSetBase + "_" + config.getString(prefix + ".key");
	    else {
		String[] p = prefix.split("\\.");
		key = p[1] + "_" + ruleSetBase;
	    }
	    String hint = config.getString(prefix + ".hint");
	    String language = config.getString(prefix + ".language", "groovy");
	    boolean mandatory = config.getBoolean(prefix + ".mandatory", false);
	    String cscript = config.getString(prefix + ".component");
	    QueueRegistry.removeMessage(INFO_TARGET, view, key);
	    JComponent comp = null;
	    if ((cscript != null) && (cscript.length() > 0)) {
		ScriptingWrapper bsm = new ScriptingWrapper(language);
		try {
		    bsm.put("view", view);
		    bsm.put("key", key);
		    bsm.put("hint", hint);
		    comp = (JComponent) bsm.eval(cscript, "Rule: "+key);
		    if (comp != null) {
			ruleset = new RuleSet(key, comp, hint);
			if (mandatory)
			    ValidationComponentUtils.setMandatory(comp, true);
		    } else
			return null;
		} catch (Exception e) {
		    log.error("Error running RuleSet-Script: " + prefix, e);
		}

		List<String> rules = (List<String>) config.getList(prefix + ".rule.key");
		for (int j = 0; j < rules.size(); j++) {
		    String rulekey = key + "_" + (String) rules.get(j);
		    String vscript = config.getString(prefix + ".rule(" + j + ").validator");
		    String init = config.getString(prefix + ".rule(" + j + ").init");
		    String message = config.getString(prefix + ".rule(" + j + ").message");
		    ruleset.addRule(rulekey, vscript, init, message, config.getString(prefix + ".rule(" + j + ").language", "groovy"));
		}
	    }
	    return ruleset;
	}

	protected RuleSet(String key, JComponent view, String hint) {
	    this.key = key;
	    this.view = view;
	    this.hint = hint;
	    ValidationComponentUtils.setInputHint(view, hint);
	    this.rules = new ArrayList<Rule>();
	}

	public void addRule(String key, String validator, String init, String message, String language) {
	    Rule r = new Rule(key, validator, init, message, language);
	    ValidationComponentUtils.setMessageKey(view, this.key);
	    rules.add(r);

	}

	public JComponent getComponent() {
	    return view;
	}

	public void setComponent(JComponent view) {
	    this.view = view;
	}

	public String getHint() {
	    return hint;
	}

	public void setHint(String hint) {
	    this.hint = hint;
	    ValidationComponentUtils.setInputHint(view, hint);
	}

	public String getKey() {
	    return key;
	}

	public void setKey(String key) {
	    this.key = key;
	}

	public List<Rule> getRules() {
	    return rules;
	}

	public String toString() {
	    StringBuilder buffer = new StringBuilder();
	    buffer.append("[RuleSet:");
	    buffer.append(" key: ");
	    buffer.append(key);
	    buffer.append(" view: ");
	    buffer.append(view);
	    buffer.append(" hint: ");
	    buffer.append(hint);
	    buffer.append(" rules: ");
	    buffer.append(rules);
	    buffer.append("]");
	    return buffer.toString();
	}

    }

    /**
     * Representation of a Rule
     * 
     * @author Alexander Klein
     * 
     */
    public final static class Rule {
	String key;
	String validator;
	String init;
	String message;
	String language;

	public Rule(String key, String validator, String init, String message) {
	    this(key, validator, init, message, "groovy");
	}

	public Rule(String key, String validator, String init, String message, String language) {
	    this.key = key;
	    this.validator = validator;
	    this.init = init;
	    this.message = message;
	    this.language = language;
	}

	public String getInit() {
	    return init;
	}

	public void setInit(String init) {
	    this.init = init;
	}

	public String getKey() {
	    return key;
	}

	public void setKey(String key) {
	    this.key = key;
	}

	public String getMessage() {
	    return message;
	}

	public void setMessage(String message) {
	    this.message = message;
	}

	public String getValidator() {
	    return validator;
	}

	public void setValidator(String validator) {
	    this.validator = validator;
	}

	public String getLanguage() {
	    return language;
	}

	public void setLanguage(String language) {
	    this.language = language;
	}

	public String toString() {
	    StringBuilder buffer = new StringBuilder();
	    buffer.append("[Rule:");
	    buffer.append(" key: ");
	    buffer.append(key);
	    buffer.append(" validator: ");
	    buffer.append(validator);
	    buffer.append(" init: ");
	    buffer.append(init);
	    buffer.append(" message: ");
	    buffer.append(message);
	    buffer.append(" language: ");
	    buffer.append(language);
	    buffer.append("]");
	    return buffer.toString();
	}

    }

}
