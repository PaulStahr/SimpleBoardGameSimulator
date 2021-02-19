/*******************************************************************************
 * Copyright (c) 2019 Paul Stahr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class StreamUtil {
	/*public static final String readStreamToString(InputStream stream) throws IOException
	{
		StringBuilder strB = new StringBuilder(stream.available());
		InputStreamReader reader = new InputStreamReader(stream);
		char ch[] = new char[4096];
		for (int len; (len = reader.read(ch))!=-1; strB.append(ch, 0, len));
		reader.close();
		return strB.toString();
	}*/
	
	
	
	public static final String readStreamToString(InputStream stream) throws IOException
	{
		InputStreamReader reader = new InputStreamReader(stream);
		char ch[] = new char[4096];
		int len = 0;
		int read = 0;
		while ((read = reader.read(ch, len, ch.length - len))!=-1)
		{
			len += read;
			if (ch.length - len == 0)
			{
				ch = Arrays.copyOf(ch, ch.length * 2);
			}
		}
		reader.close();
		return new String(ch, 0, len);
	}
	
	public static ObjectOutputStream toObjectStream(OutputStream input) throws IOException {
		return input instanceof ObjectOutputStream ? (ObjectOutputStream)input : new ObjectOutputStream(input);
	}	

	public static ObjectInputStream toObjectStream(InputStream input) throws IOException {
		return input instanceof ObjectInputStream ? (ObjectInputStream)input : new ObjectInputStream(input);
	}

	public static void skip(ObjectInputStream is, int i) throws IOException {
		while ((i -= is.skip(i)) != 0);
	}
	
	public static final void copy(InputStream in, OutputStream out) throws IOException{
		byte data[] = new byte[4096];
        int len = 0;
        while ((len = in.read(data, 0, 4096)) > 0)
        {
        	out.write(data, 0, len);
        }
	}

   public static byte[] toByteArray(InputStream stream, int cap) throws IOException{
        byte ch[] = new byte[Math.min(cap, Math.max(1, stream.available()))];
        int len = 0;
        int read = 0;
        while ((read = stream.read(ch, len, Math.min(ch.length, cap) - len))!=-1)
        {
            len += read;
            if (ch.length < Math.min(cap, len + stream.available()) || (stream.available() == 0 && ch.length == len))
            {
                ch = Arrays.copyOf(ch, Math.min(cap, Math.max(ch.length * 2, ch.length + stream.available())));
            }
        }
        return len == ch.length ? ch : Arrays.copyOf(ch, len);
    }

	public static byte[] toByteArray(InputStream stream) throws IOException{
		byte ch[] = new byte[Math.max(1, stream.available())];
		int len = 0;
		int read = 0;
		while ((read = stream.read(ch, len, ch.length - len))!=-1)
		{
			len += read;
			if (ch.length < len + stream.available() || (stream.available() == 0 && ch.length == len))
			{
				ch = Arrays.copyOf(ch, Math.max(ch.length * 2, ch.length + stream.available()));
			}
		}
		return len == ch.length ? ch : Arrays.copyOf(ch, len);
	}
}
