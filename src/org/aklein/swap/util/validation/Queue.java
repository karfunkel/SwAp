/*
 * Created on 21.04.2006
 *
 */
package org.aklein.swap.util.validation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aklein.swap.util.MessageKeyProvider;
import org.aklein.swap.util.binding.SwingBinder;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationMessage;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.view.ValidationResultListAdapter;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import com.jgoodies.validation.view.ValidationResultViewFactory.MessageStateChangeHandler;

/**
 * A {@link ValidationResultModel} handling errors, warnings, and infos
 * 
 * @author Alexander Klein
 * TODO: Implementation should be made more flexible
 * 
 */
public class Queue extends DefaultValidationResultModel
{
	private static Log log = LogFactory.getLog(Queue.class);

	private MultiKeyMap messages = new MultiKeyMap();
	private String description = "";
	private boolean openPopup = false;

	public Queue()
	{
		super();
	}

	public boolean isOpenPopup()
	{
		return openPopup;
	}

	public void setOpenPopup(boolean openPopup)
	{
		this.openPopup = openPopup;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		String old = description;
		this.description = description;
		firePropertyChange("description", old, description);
	}

	public void removeMessage(String scope, String key)
	{
		removeMessage(scope, key, "");
	}

	public void removeMessage(MessageKeyProvider component, String key)
	{
		if (component != null)
			removeMessage(component, key, "");
	}

	public void removeMessage(String scope, String key, String ruleKey)
	{
		messages.remove(scope, key, ruleKey);
		refresh(scope);
	}

	public void removeMessage(MessageKeyProvider component, String key, String ruleKey)
	{
		if (component != null)
			removeMessage(component.getMessageKey(), key, ruleKey);
	}

	public void removeMessages(String scope, Severity severity)
	{
		for (MapIterator it = messages.mapIterator(); it.hasNext();)
		{
			MultiKey multi = (MultiKey) it.next();
			if (multi.size() >= 1 && (scope != null ? scope.equals(multi.getKey(0)) : multi.getKey(0) == null))
			{
				CustomValidationMessage m = (CustomValidationMessage) it.getValue();
				if (severity == null || (m.severity().equals(severity)))
					removeMessage((String) multi.getKey(0), (String) multi.getKey(1));
			}
		}
		refresh(scope);
	}

	public void removeMessages(MessageKeyProvider component, Severity severity)
	{
		if (component != null)
			removeMessages(component.getMessageKey(), severity);
	}

	public void addMessage(String scope, String key, String ruleKey, Severity severity, String message)
	{
		if (message == null)
		{
			log.debug("removeMessage: " + scope + ": " + key);
			removeMessage(scope, key, ruleKey);
		}
		else
		{
			log.debug("addMessage: " + scope + ": " + key + " :" + message);
			CustomValidationMessage<String> m = new CustomValidationMessage<String>(message, severity, key, ruleKey);
			messages.put(scope, key, ruleKey, m);
		}
		refresh(scope);
	}

	public void addMessage(MessageKeyProvider component, String key, String ruleKey, Severity severity, String message)
	{
		if (component != null)
			addMessage(component.getMessageKey(), key, ruleKey, severity, message);
	}

	public void clearMessages()
	{
		messages.clear();
	}

	public void clearMessages(String scope)
	{
		messages.removeAll(scope);
		refresh(scope);
	}

	public void clearMessages(MessageKeyProvider component)
	{
		if (component != null)
			clearMessages(component.getMessageKey());
	}

	public void addInfo(String scope, String key, String ruleKey, String message)
	{
		addMessage(scope, key, ruleKey, Severity.WARNING, (message == null) ? null : "###" + message);
	}

	public void addInfo(MessageKeyProvider component, String key, String ruleKey, String message)
	{
		if (component != null)
			addInfo(component.getMessageKey(), key, ruleKey, message);
	}

	public void addWarning(String scope, String key, String ruleKey, String message)
	{
		addMessage(scope, key, ruleKey, Severity.WARNING, message);
	}

	public void addWarning(MessageKeyProvider component, String key, String ruleKey, String message)
	{
		if (component != null)
			addWarning(component.getMessageKey(), key, ruleKey, message);
	}

	public void addError(String scope, String key, String ruleKey, String message)
	{
		addMessage(scope, key, ruleKey, Severity.ERROR, message);
	}

	public void addError(MessageKeyProvider component, String key, String ruleKey, String message)
	{
		if (component != null)
			addError(component.getMessageKey(), key, ruleKey, message);
	}

	@SuppressWarnings("unchecked")
	public CustomValidationMessage<String> getMessage(String scope, String key, String ruleKey)
	{
		return (CustomValidationMessage<String>) messages.get(scope, key, ruleKey);
	}

	public CustomValidationMessage<String> getMessage(MessageKeyProvider component, String key, String ruleKey)
	{
		return (component == null) ? null : getMessage(component.getMessageKey(), key, ruleKey);
	}

	@SuppressWarnings("unchecked")
	public List<CustomValidationMessage<String>> getMessages(String scope)
	{
		List<CustomValidationMessage<String>> list = new ArrayList<CustomValidationMessage<String>>();
		for (MapIterator it = messages.mapIterator(); it.hasNext();)
		{
			MultiKey multi = (MultiKey) it.next();
			if (multi.size() > 0 && (scope != null ? scope.equals(multi.getKey(0)) : multi.getKey(0) == null))
				list.add((CustomValidationMessage<String>) it.getValue());
		}
		return list;
	}

	public List<CustomValidationMessage<String>> getMessages(MessageKeyProvider component)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getMessages(component.getMessageKey());
	}

	@SuppressWarnings("unchecked")
	public List<CustomValidationMessage<String>> getMessages(String scope, String key)
	{
		List<CustomValidationMessage<String>> list = new ArrayList<CustomValidationMessage<String>>();
		for (MapIterator it = messages.mapIterator(); it.hasNext();)
		{
			MultiKey multi = (MultiKey) it.next();
			if (multi.size() > 0 && (scope != null ? scope.equals(multi.getKey(0)) : multi.getKey(0) == null) && (key != null ? key.equals(multi.getKey(1)) : multi.getKey(1) == null))
				list.add((CustomValidationMessage<String>) it.getValue());
		}
		return list;
	}

	public List<CustomValidationMessage<String>> getMessages(MessageKeyProvider component, String key)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getMessages(component.getMessageKey(), key);
	}

	@SuppressWarnings("unchecked")
	public List<CustomValidationMessage<String>> getMessages(String scope, Severity severity)
	{
		List<CustomValidationMessage<String>> scoped = getMessages(scope);
		List<CustomValidationMessage<String>> list = new ArrayList<CustomValidationMessage<String>>();

		for (Iterator iter = scoped.iterator(); iter.hasNext();)
		{
			CustomValidationMessage<String> m = (CustomValidationMessage<String>) iter.next();
			if (m.severity().equals(severity))
				list.add(0, m);
		}
		return list;
	}

	public List<CustomValidationMessage<String>> getMessages(MessageKeyProvider component, Severity severity)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getMessages(component.getMessageKey(), severity);
	}

	@SuppressWarnings("unchecked")
	public ValidationResult getQueue(String scope)
	{
		List<CustomValidationMessage<String>> scoped = getMessages(scope);
		List<CustomValidationMessage<String>> defaultScoped = getMessages((String) null);
		if ((scoped.size() > 0) || (defaultScoped.size() > 0))
		{
			//
		}
		Collections.sort(scoped, new Comparator<CustomValidationMessage<String>>()
		{
			public int compare(CustomValidationMessage<String> o1, CustomValidationMessage<String> o2)
			{
				int sc = o1.severity().compareTo(o2.severity());
				int dc = o2.getTimeStamp().compareTo(o1.getTimeStamp());
				if (sc != 0)
					return sc;
				return dc;
			}
		});

		ValidationResult result = new ValidationResult();
		for (Iterator iter = scoped.iterator(); iter.hasNext();)
			result.add((CustomValidationMessage<String>) iter.next());
		for (Iterator iter = defaultScoped.iterator(); iter.hasNext();)
			result.add((CustomValidationMessage<String>) iter.next());
		return result;
	}

	public ValidationResult getQueue(MessageKeyProvider component)
	{
		return (component == null) ? new ValidationResult() : getQueue(component.getMessageKey());
	}

	public List<CustomValidationMessage<String>> getInfos(MessageKeyProvider component)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getInfos(component.getMessageKey());
	}

	@SuppressWarnings("unchecked")
	public List<CustomValidationMessage<String>> getInfos(String scope)
	{
		List<CustomValidationMessage<String>> list = new ArrayList<CustomValidationMessage<String>>();
		List<CustomValidationMessage<String>> scoped = getMessages(scope);
		for (Iterator iter = scoped.iterator(); iter.hasNext();)
		{
			CustomValidationMessage<String> m = (CustomValidationMessage<String>) iter.next();
			if (m.formattedText().startsWith("###"))
				list.add(m);
		}
		return list;
	}

	public List<CustomValidationMessage<String>> getWarnings(MessageKeyProvider component)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getWarnings(component.getMessageKey());
	}

	@SuppressWarnings("unchecked")
	public List<CustomValidationMessage<String>> getWarnings(String scope)
	{
		List<CustomValidationMessage<String>> list = new ArrayList<CustomValidationMessage<String>>();
		List<CustomValidationMessage<String>> scoped = getMessages(scope, Severity.WARNING);
		for (Iterator iter = scoped.iterator(); iter.hasNext();)
		{
			CustomValidationMessage<String> m = (CustomValidationMessage<String>) iter.next();
			if (!m.formattedText().startsWith("###"))
				list.add(m);
		}
		return list;
	}

	public List<CustomValidationMessage<String>> getErrors(MessageKeyProvider component)
	{
		return (component == null) ? new ArrayList<CustomValidationMessage<String>>() : getErrors(component.getMessageKey());
	}

	public List<CustomValidationMessage<String>> getErrors(String scope)
	{
		return getMessages(scope, Severity.ERROR);
	}

	public void refresh(String scope)
	{
		setResult(getQueue(scope));
	}

	public void refresh(MessageKeyProvider component)
	{
		if (component != null)
			refresh(component.getMessageKey());
	}

	/**
	 * Method to bind a JLabel to this Queue
	 * 
	 * @param queue
	 * @param label
	 */
	public static void bindLabel(Queue queue, JLabel label)
	{
		bindLabel(queue, label, null);
	}

	/**
	 * Method to bind a JLabel to this Queue
	 * 
	 * @param queue
	 * @param label
	 * @param owner
	 */
	public static void bindLabel(Queue queue, JLabel label, Window owner)
	{
		ChangeHandler handler = new ChangeHandler(label);
		ChangeHandler.updateVisibilityAndIcon(label, queue.getSeverity());
		queue.addPropertyChangeListener("severity", handler);
		ChangeHandler.updateText(label, queue.getResult());
		queue.addPropertyChangeListener("result", handler);
		if (queue.isOpenPopup())
		{
			final Window frame = owner;
			final Queue iqueue = queue;
			final QueuePopup popup = new QueuePopup(iqueue, frame);
			label.addMouseListener(new MouseListener()
			{
				int height = 100;

				public void mouseClicked(MouseEvent e)
				{
					JLabel label = (JLabel) e.getSource();
					Point p = new Point(0, label.getHeight());
					SwingUtilities.convertPointToScreen(p, label);
					popup.setBounds(p.x, p.y - height, label.getSize().width, height);
					popup.popup();
				}

				public void mousePressed(MouseEvent e)
				{}

				public void mouseReleased(MouseEvent e)
				{}

				public void mouseEntered(MouseEvent e)
				{}

				public void mouseExited(MouseEvent e)
				{}
			});
		}
	}

	protected static class ChangeHandler implements PropertyChangeListener
	{
		private JLabel label;
		private final static String PROPERTY = "###message";

		protected ChangeHandler(JLabel label)
		{
			this.label = label;
		}

		private static void updateVisibilityAndIcon(JLabel aLabel, Severity severity)
		{
			if (severity == Severity.OK)
			{
				aLabel.setIcon(null);
				aLabel.setText(null);
			}
			else if (severity == Severity.ERROR)
				aLabel.setIcon(ValidationResultViewFactory.getErrorIcon());
			else if (severity == Severity.WARNING)
			{
				if (aLabel.getClientProperty(PROPERTY).equals(Boolean.TRUE))
					aLabel.setIcon(ValidationResultViewFactory.getInfoIcon());
				else
					aLabel.setIcon(ValidationResultViewFactory.getWarningIcon());
			}
		}

		private static void updateText(JLabel label, ValidationResult result)
		{
			label.putClientProperty(PROPERTY, Boolean.FALSE);
			if (result.getMessages().size() < 1)
				return;
			String message = ((ValidationMessage) result.getMessages().get(0)).formattedText();
			if (message.startsWith("###"))
			{
				message = message.substring(3);
				label.putClientProperty(PROPERTY, Boolean.TRUE);
			}
			label.setText(result.hasMessages() ? message : "");
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals("severity"))
				updateVisibilityAndIcon(label, (Severity) evt.getNewValue());
			if (evt.getPropertyName().equals("result"))
				updateText(label, (ValidationResult) evt.getNewValue());
		}
	}

	protected static class QueuePopup extends JWindow
	{
		private static final long serialVersionUID = -5562464266817421679L;
		JList list;
		JLabel label;
		Window frame;
		JButton button;
		Queue queue;

		// TODO: create another implementation, so popup will be moved along or closed when the window is moved
		public QueuePopup(Queue queue, Window owner)
		{
			super(owner);
			frame = owner;
			this.queue = queue;
			init();
		}

		public void popup()
		{
			SwingBinder.getSharedInstance().bindLabel(label, new PropertyAdapter<Queue>(queue, "description"));
			setVisible(true);
			button.requestFocusInWindow();
		}

		public void popup(ValidationResult result)
		{
			queue.setResult(result);
			popup();
		}

		private void init()
		{
			getRootPane().setBorder(new LineBorder(new Color(153, 153, 153)));
			list = new JList();
			list.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e)
				{
					JList list = (JList) e.getSource();
					if (!e.getValueIsAdjusting())
						list.getSelectionModel().clearSelection();
				}
			});
			list.setFocusable(false);
			list.setCellRenderer(new BasicValidationMessageCellRenderer());
			list.setModel(new ValidationResultListAdapter(queue));
			JScrollPane scrollPane = new JScrollPane(list);
			queue.addPropertyChangeListener("messages", new MessageStateChangeHandler(scrollPane));
			add(list, BorderLayout.CENTER);

			JPanel panel = new JPanel(new BorderLayout());
			panel.setBackground(new Color(222, 222, 222));
			button = new JButton(ValidationResultViewFactory.getSmallErrorIcon());
			button.setBorder(null);
			button.setOpaque(false);
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
				}
			});
			getRootPane().setDefaultButton(button);
			panel.add(button, BorderLayout.EAST);
			label = new JLabel("", SwingConstants.LEFT);
			label.setFont(new Font("", Font.BOLD, 12));
			panel.add(label, BorderLayout.CENTER);
			add(panel, BorderLayout.NORTH);

			addKeyListener(new KeyListener()
			{
				public void keyTyped(KeyEvent e)
				{}

				public void keyPressed(KeyEvent e)
				{
					QueuePopup.this.setVisible(false);
				}

				public void keyReleased(KeyEvent e)
				{}
			});
		}

		private static class BasicValidationMessageCellRenderer extends DefaultListCellRenderer
		{
			private static final long serialVersionUID = 2532795958778535292L;

			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, false, false);
				ValidationMessage message = (ValidationMessage) value;
				String msg = message.formattedText();
				if (message.severity().equals(Severity.ERROR))
					setIcon(ValidationResultViewFactory.getErrorIcon());
				else if (msg.startsWith("###"))
				{
					msg = msg.substring(3);
					setIcon(ValidationResultViewFactory.getInfoIcon());
				}
				else if (message.severity().equals(Severity.WARNING))
					setIcon(ValidationResultViewFactory.getWarningIcon());
				setText(msg);
				return this;
			}

			private BasicValidationMessageCellRenderer()
			{}

		}
	}
}
