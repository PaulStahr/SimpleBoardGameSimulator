package util.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import util.StringUtils;
import util.data.DoubleArrayList;

public class IOUtil {
	public static final DoubleArrayList readPositionFile(String file) throws IOException
	{
		FileReader reader = new FileReader(file);
		BufferedReader inBuf = new BufferedReader(reader);
		final DoubleArrayList ial = new DoubleArrayList();
		String line;
		ArrayList<String> al = new ArrayList<String>();
		char seperators[] = new char[] {' ','\t',','};
		while ((line = inBuf.readLine()) != null)
		{
			if (line.length() == 0 || line.charAt(0) == '#')
			{
				continue;
			}
			try
			{
				StringUtils.parseDoubles(line, 0, line.length(), al, ial, seperators);		
			}catch (NumberFormatException e1) {
				inBuf.close();
				reader.close();
				throw new IOException(e1);
			}
		}
		inBuf.close();
		reader.close();
		return ial;
	}
	

	
	public static final void writeColumnTable(String rownames[], Object []columns, BufferedWriter outBuf) throws IOException
	{
		StringBuilder strB = new StringBuilder();
		int numCols = rownames.length;
		char chBuf[] = new char[1024];
		for (int i = 0; i < numCols; ++i)
		{
			outBuf.write(rownames[i]);
			outBuf.write(' ');
		}
		for (int i = 0; true; ++i)
		{
			for (int j = 0; j < numCols; ++j)
			{
				Object current = columns[j];
				try
				{
					if (current instanceof double[])
					{
						strB.append(((double[])current)[i]);
					}
					else if (current instanceof float[])
					{
						strB.append(((float[])current)[i]);
					}
					else if (current instanceof int[])
					{
						strB.append(((int[])current)[i]);
					}
					else if (current instanceof long[])
					{
						strB.append(((long[])current)[i]);
					}
					else if (current instanceof short[])
					{
						strB.append(((short[])current)[i]);
					}
					else if (current instanceof byte[])
					{
						strB.append(((byte[])current)[i]);					
					}
					else if (current instanceof boolean[])
					{
						strB.append(((boolean[])current)[i]);					
					}
					else if (current instanceof Object[])
					{
						strB.append(((Object[])current)[i]);					
					}
					strB.append(' ');
				}catch (ArrayIndexOutOfBoundsException e)
				{
					return;
				}
			}
			outBuf.newLine();
			chBuf = StringUtils.writeAndReset(outBuf, strB, chBuf);
		}
	}

	public static final void copy(InputStream in, OutputStream out) throws IOException{
		byte data[] = new byte[4096];
        int len = 0;
        while ((len = in.read(data, 0, 4096)) > 0)
        {
        	out.write(data, 0, len);
        }
	}
}
