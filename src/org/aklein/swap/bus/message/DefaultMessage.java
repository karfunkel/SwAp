package org.aklein.swap.bus.message;

import org.aklein.swap.bus.BusMessage;

/**
 * BusMessage transporting a textual message
 * 
 * @author Alexander Klein
 * 
 */
public class DefaultMessage implements BusMessage
{
	private static final long serialVersionUID = -6710018724676297082L;
	private String message = null;

	public DefaultMessage(String message)
	{
		super();
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
