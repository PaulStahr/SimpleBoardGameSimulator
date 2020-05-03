package util.stream;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWrapper extends OutputStream {
	private final OutputStream out;
	public OutputStreamWrapper(OutputStream out) {
		this.out = out;
	}

	@Override
	public void write(int arg0) throws IOException {
		out.write(arg0);
	}
	
	@Override
	public void close() throws IOException{
		out.close();
	}

	@Override
	public void flush() throws IOException
	{
		out.flush();
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		out.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
	}

}
