package util.stream;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream {
	private final InputStream in;
	public InputStreamWrapper(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}
	
	@Override
	public void close() throws IOException{
		in.close();
	}
	
	@Override
	public int read(byte[] b) throws IOException
	{
		return in.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return in.read(b, off, len);
	}
}
