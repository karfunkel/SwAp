package org.aklein.swap.bus.message;

import java.util.EventObject;

/**
 * BusMessage transporting an event along with a textual message.
 * 
 * @author Alexander Klein
 * 
 */
public class EventMessage extends DefaultMessage
{
	private static final long serialVersionUID = -4244501896253182327L;
	private EventObject event = null;

	public EventMessage(EventObject event)
	{
		this(event, null);
	}

	public EventMessage(EventObject event, String message)
	{
		super(message);
		this.event = event;
	}

	public java.util.EventObject getEvent()
	{
		return event;
	}
}
