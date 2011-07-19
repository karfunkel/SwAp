package org.aklein.swap.util.validation;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aklein.swap.util.MessageKeyProvider;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;

/**
 * Registry to register and work with {@link Queue}s <br/>
 * 
 * @author Alexander Klein
 * 
 */
public class QueueRegistry
{
	private static final String GLOBAL_KEY = "__global__";
	private static Map<String, Set<WeakReference<Queue>>> queues = new HashMap<String, Set<WeakReference<Queue>>>();
	private static Map<String, Set<WeakReference<PropertyChangeListener>>> listeners = new HashMap<String, Set<WeakReference<PropertyChangeListener>>>();

	public static Queue createQueue(String target, String description, boolean openPopup)
	{
		Queue queue = new Queue();
		queue.setDescription(description);
		queue.setOpenPopup(openPopup);
		registerQueue(target, queue);
		return queue;
	}

	public static void registerQueue(String target, Queue queue)
	{
		if (queue == null)
			return;
		Set<WeakReference<Queue>> queueset = getQueueSet(target);
		if (queueset == null)
		{
			queueset = new HashSet<WeakReference<Queue>>();
			queues.put(target, queueset);
		}
		queueset.add(new WeakReference<Queue>(queue));
		addPropertyChangeListeners(queue);
	}

	public static void unregisterQueue(String target, Queue queue)
	{
		if (queue == null)
			return;
		Set<WeakReference<Queue>> queueset = getQueueSet(target);
		if (queueset == null)
			return;
		for (WeakReference<Queue> ref : queueset)
		{
			if (ref.get().equals(queue))
				queueset.remove(ref);
		}
		removePropertyChangeListeners(queue);
	}

	public static List<Queue> getQueues(String target)
	{
		Set<WeakReference<Queue>> queueset = getQueueSet(target);
		List<Queue> out = new ArrayList<Queue>();
		for (WeakReference<Queue> ref : queueset)
		{
			if (ref.get() != null)
				out.add(ref.get());
		}
		return out;
	}

	public static List<Queue> getQueues()
	{
		List<Queue> out = new ArrayList<Queue>();
		for (String target : queues.keySet())
		{
			Set<WeakReference<Queue>> queueset = getQueueSet(target);
			for (WeakReference<Queue> ref : queueset)
				if (ref.get() != null)
					out.add(ref.get());
		}
		return out;
	}

	public static void unregisterAll(String target)
	{
		Set<WeakReference<Queue>> queueset = getQueueSet(target);
		for (WeakReference<Queue> ref : queueset)
			removePropertyChangeListeners(ref.get());
		queues.remove(target);
	}

	public static void unregisterAll()
	{
		for (String target : queues.keySet())
			unregisterAll(target);
	}

	private static Set<WeakReference<Queue>> getQueueSet(String target)
	{
		Set<WeakReference<Queue>> queueset = queues.get(target);
		if (queueset == null)
		{
			queueset = new HashSet<WeakReference<Queue>>();
			queues.put(target, queueset);
		}
		return queueset;
	}

	public static void addError(String target, String scope, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addError(scope, key, ruleKey, message);
	}

	public static void addError(String target, MessageKeyProvider component, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addError(component, key, ruleKey, message);
	}

	public static void addInfo(String target, String scope, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addInfo(scope, key, ruleKey, message);
	}

	public static void addInfo(String target, MessageKeyProvider component, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addInfo(component, key, ruleKey, message);
	}

	public static void addMessage(String target, String scope, String key, String ruleKey, Severity severity, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addMessage(scope, key, ruleKey, severity, message);
	}

	public static void addMessage(String target, MessageKeyProvider component, String key, String ruleKey, Severity severity, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addMessage(component, key, ruleKey, severity, message);
	}

	public static void addWarning(String target, String scope, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addWarning(scope, key, ruleKey, message);
	}

	public static void addWarning(String target, MessageKeyProvider component, String key, String ruleKey, String message)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.addWarning(component, key, ruleKey, message);
	}

	public static void clearMessages(String target, String scope)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.clearMessages(scope);
	}

	public static void clearMessages(String target, MessageKeyProvider component)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.clearMessages(component);
	}

	public static void removeMessage(String target, String scope, String key, String ruleKey)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessage(scope, key, ruleKey);
	}

	public static void removeMessage(String target, String scope, String key)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessage(scope, key);
	}

	public static void removeMessage(String target, MessageKeyProvider component, String key, String ruleKey)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessage(component, key, ruleKey);
	}

	public static void removeMessage(String target, MessageKeyProvider component, String key)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessage(component, key);
	}

	public static void removeMessages(String target, String scope, Severity severity)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessages(scope, severity);
	}

	public static void removeMessages(String target, MessageKeyProvider component, Severity severity)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.removeMessages(component, severity);
	}

	public static boolean checkForMessage(String target, String scope, String key, String ruleKey)
	{
		return getMessage(target, scope, key, ruleKey) != null;
	}

	public static boolean checkForMessage(String target, MessageKeyProvider component, String key, String ruleKey)
	{
		return getMessage(target, component, key, ruleKey) != null;
	}

	public static CustomValidationMessage<String> getMessage(String target, String scope, String key, String ruleKey)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
		{
			CustomValidationMessage<String> msg = queue.getMessage(scope, key, ruleKey);
			if (msg != null)
				return msg;
		}
		return null;
	}

	public static CustomValidationMessage<String> getMessage(String target, MessageKeyProvider component, String key, String ruleKey)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
		{
			CustomValidationMessage<String> msg = queue.getMessage(component, key, ruleKey);
			if (msg != null)
				return msg;
		}
		return null;
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, String scope, Severity severity)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(scope, severity));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, String scope, String key)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(scope, key));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, String scope)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(scope));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, MessageKeyProvider component, Severity severity)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(component, severity));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, MessageKeyProvider component, String key)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(component, key));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static List<CustomValidationMessage<String>> getMessages(String target, MessageKeyProvider component)
	{
		Set<CustomValidationMessage<String>> out = new HashSet<CustomValidationMessage<String>>();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			out.addAll(queue.getMessages(component));
		return new ArrayList<CustomValidationMessage<String>>(out);
	}

	public static ValidationResult getQueue(String target, String scope)
	{
		ValidationResult result = new ValidationResult();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			result.addAllFrom(queue.getQueue(scope));
		return result;
	}

	public static ValidationResult getQueue(String target, MessageKeyProvider component)
	{
		ValidationResult result = new ValidationResult();
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			result.addAllFrom(queue.getQueue(component));
		return result;
	}

	public static void refresh(String target, String scope)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.refresh(scope);
	}

	public static void refresh(String target, MessageKeyProvider component)
	{
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
			queue.refresh(component);
	}

	public static void addPropertyChangeListener(String target, PropertyChangeListener listener)
	{
		addPropertyChangeListener(GLOBAL_KEY, listener);
	}

	public static void addPropertyChangeListener(String target, String propertyName, PropertyChangeListener listener)
	{
		Set<WeakReference<PropertyChangeListener>> listenerset = listeners.get(propertyName);
		if (listenerset == null)
		{
			listenerset = new HashSet<WeakReference<PropertyChangeListener>>();
			listeners.put(propertyName, listenerset);
		}
		for (WeakReference<PropertyChangeListener> ref : listenerset)
		{
			PropertyChangeListener l = ref.get();
			if (l == null)
			{
				listeners.remove(ref);
				continue;
			}
			if (l.equals(listener))
				return;
		}
		listenerset.add(new WeakReference<PropertyChangeListener>(listener));
		List<Queue> list = getQueues(target);
		for (Queue queue : list)
		{
			if (propertyName == GLOBAL_KEY)
				queue.addPropertyChangeListener(listener);
			else
				queue.addPropertyChangeListener(propertyName, listener);
		}
	}

	public static PropertyChangeListener[] getPropertyChangeListeners(String target)
	{
		return getPropertyChangeListeners(GLOBAL_KEY);
	}

	public static PropertyChangeListener[] getPropertyChangeListeners(String target, String propertyName)
	{
		List<WeakReference<PropertyChangeListener>> listenerlist = new ArrayList<WeakReference<PropertyChangeListener>>(listeners.get(propertyName));
		if (listenerlist == null)
		{
			return new PropertyChangeListener[0];
		}
		for (WeakReference<PropertyChangeListener> ref : listenerlist)
		{
			PropertyChangeListener l = ref.get();
			if (l == null)
			{
				listeners.remove(ref);
				continue;
			}
		}
		PropertyChangeListener[] out = new PropertyChangeListener[listenerlist.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = listenerlist.get(i).get();
		return out;
	}

	public static void removePropertyChangeListener(String target, PropertyChangeListener listener)
	{
		removePropertyChangeListener(GLOBAL_KEY, listener);
	}

	public static void removePropertyChangeListener(String target, String propertyName, PropertyChangeListener listener)
	{
		Set<WeakReference<PropertyChangeListener>> listenerset = listeners.get(propertyName);
		if (listenerset == null)
			return;
		for (WeakReference<PropertyChangeListener> ref : listenerset)
		{
			PropertyChangeListener l = ref.get();
			if (l == null)
			{
				listeners.remove(ref);
				continue;
			}
			List<Queue> list = getQueues(target);
			for (Queue queue : list)
			{
				if (propertyName == GLOBAL_KEY)
					queue.removePropertyChangeListener(listener);
				else
					queue.removePropertyChangeListener(propertyName, listener);
			}
			if (l.equals(listener))
			{
				listenerset.remove(ref);
				return;
			}
		}
	}

	public static void removePropertyChangeListeners(Queue queue)
	{
		for (String propertyName : listeners.keySet())
		{
			Set<WeakReference<PropertyChangeListener>> listenerset = listeners.get(propertyName);

			for (WeakReference<PropertyChangeListener> ref : listenerset)
			{
				PropertyChangeListener l = ref.get();
				if (l == null)
				{
					listeners.remove(ref);
					continue;
				}
				if (propertyName == GLOBAL_KEY)
					queue.removePropertyChangeListener(l);
				else
					queue.removePropertyChangeListener(propertyName, l);
			}
		}
	}

	public static void addPropertyChangeListeners(Queue queue)
	{
		for (String propertyName : listeners.keySet())
		{
			Set<WeakReference<PropertyChangeListener>> listenerset = listeners.get(propertyName);
			for (WeakReference<PropertyChangeListener> ref : listenerset)
			{
				PropertyChangeListener l = ref.get();
				if (l == null)
				{
					listeners.remove(ref);
					continue;
				}
				if (propertyName == GLOBAL_KEY)
					queue.addPropertyChangeListener(l);
				else
					queue.addPropertyChangeListener(propertyName, l);
			}
		}
	}
}
