package org.aklein.swap.examples.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

public class StoppingInputStream extends InputStream
{
	private PushbackInputStream delegate;
	private char stopChar;
	private boolean justStopped = false;

	public StoppingInputStream(PushbackInputStream delegate, char stopChar)
	{
		super();
		this.delegate = delegate;
		this.stopChar = stopChar;
	}

	public int available() throws IOException
	{
		return delegate.available();
	}

	public void close() throws IOException
	{
		delegate.close();
	}

	public boolean equals(Object obj)
	{
		return delegate.equals(obj);
	}

	public int hashCode()
	{
		return delegate.hashCode();
	}

	public void mark(int readlimit)
	{
		delegate.mark(readlimit);
	}

	public boolean markSupported()
	{
		return delegate.markSupported();
	}

	public int read() throws IOException
	{
		int c = delegate.read();
		if (c == stopChar)
			c = -1;
		return c;
	}

	public int read(byte[] b, int off, int len) throws IOException
	{
		if (justStopped)
		{
			justStopped = false;
			return -1;
		}
		int l = delegate.read(b, off, len);
		if (l < 0)
			return l;
		boolean stop = false;
		int length = l;
		for (int i = 0; i < l; i++)
		{
			if (stop)
			{
				b[i] = 0;
				justStopped = true;
			}
			else
			{
				if (b[i] == stopChar)
				{
					delegate.unread(b, i + 1, l - i - 1);
					stop = true;
					length = i;
					b[i] = 0;
				}
			}
		}
		return length;
	}

	public int read(byte[] b) throws IOException
	{
		if (justStopped)
		{
			justStopped = false;
			return -1;
		}
		int l = delegate.read(b);
		if (l < 0)
			return l;
		boolean stop = false;
		int length = l;
		for (int i = 0; i < l; i++)
		{
			if (stop)
			{
				b[i] = 0;
				justStopped = true;
			}
			else
			{
				if (b[i] == stopChar)
				{
					delegate.unread(b, i + 1, l - i - 1);
					stop = true;
					length = i;
					b[i] = 0;
				}
			}
		}
		return length;
	}

	public void reset() throws IOException
	{
		delegate.reset();
	}

	public long skip(long n) throws IOException
	{
		return delegate.skip(n);
	}

	public String toString()
	{
		return delegate.toString();
	}
}
