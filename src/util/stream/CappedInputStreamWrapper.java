package util.stream;

import java.io.IOException;
import java.io.InputStream;

public class CappedInputStreamWrapper extends InputStream {
	private final InputStream in;
	private int remaining;
	private static final byte[] skipBuf = new byte[0x1000];
	  
	public CappedInputStreamWrapper(InputStream in, int size) {
		this.in = in;
		this.remaining = size;
	}
	
	public void setCap(int size)
	{
		this.remaining = size;
	}
	
	public int availible() throws IOException
	{
		return Math.min(in.available(), remaining);
	}

	@Override
	public int read() throws IOException {
		if(remaining == 0)
		{
			return -1;
		}
		--remaining;
		return in.read();
	}
	
	@Override
	public void close() throws IOException{	}
	
	@Override
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (remaining == 0)
		{
			return -1;
		}
		len = Math.min(remaining, len);
		int read = in.read(b, off, len);
		remaining -= read;
		return read;
	}
	
	

	public void drain() throws IOException{
		while (remaining != 0)
		{
			skip(remaining);
		}	
	}
	
	@Override
	public long skip(long n) throws IOException
	{
		final long origN = n;
		while (n > 0L)
		{
			int numread = read(skipBuf, 0, n > skipBuf.length ? skipBuf.length : (int) n);
			if (numread <= 0)
				break;
			n -= numread;
		}
		return origN - n;
	}
}
