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
package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import util.data.DoubleArrayList;
import util.data.IntegerArrayList;

public class StringUtils {
   public static final String EMPTY = "";
   private final ArrayList<String> al = new ArrayList<>();
   private final StringBuilder strB = new StringBuilder();

   public static final String getFileType(String str){
       final int index = str.lastIndexOf('.');
       return index == -1 ? null : str.substring(index+1);
   }
   

	public static final char[] writeAndReset(BufferedWriter outBuf, StringBuilder strB, char chBuf[]) throws IOException
	{
		outBuf.write(chBuf = StringUtils.getChars(strB, chBuf), 0, strB.length());
		strB.setLength(0);
		return chBuf;
	}
  
	public static final int indexOfOrEnd(String s, int begin, int end, char c)
	{
		int i = s.indexOf(c, begin);
		if (i > end)
		{
			return end;
		}
		return i < 0 ? end : i;
	}
	
	public static final char[] writeLineAndReset(BufferedWriter outBuf, StringBuilder strB, char chBuf[]) throws IOException
	{
		outBuf.write(chBuf = StringUtils.getChars(strB, chBuf), 0, strB.length());
		outBuf.newLine();
		strB.setLength(0);
		return chBuf;
	}
   
	public static final int lastIndexOf(final CharSequence str, int begin, int end, char c)
	{
		for (--end; end != begin; --end)
		{
			if (str.charAt(end) == c)
			{
				return end;
			}
		}
		return -1;
	}
	
	public static double parseDouble(String s, int begin, int end) throws NumberFormatException
	{
		return Double.parseDouble(s.substring(begin, end));
	}
	
	public static float parseFloat(String s, int begin, int end) throws NumberFormatException
	{
		return Float.parseFloat(s.substring(begin, end));
	}
	
    public static int parseInt(String s, int begin, int end, int radix) throws NumberFormatException
    {
	    if (s == null) {
	        throw new NullPointerException("null");
	    }
	
	    if (radix < Character.MIN_RADIX) {
	        throw new NumberFormatException("radix " + radix +" less than Character.MIN_RADIX");
	    }
	
	    if (radix > Character.MAX_RADIX) {
	        throw new NumberFormatException("radix " + radix +" greater than Character.MAX_RADIX");
	    }
	    if (begin >= end) {
	    	throw new NumberFormatException(s.substring(begin, end));
	    }
	    int result = 0;
	    boolean negative = false;
		int limit = -Integer.MAX_VALUE;
	    char firstChar = s.charAt(begin);
	
	    if (firstChar < '0') { // Possible leading "+" or "-"
	        if (firstChar == '-') {
	            negative = true;
	            limit = Integer.MIN_VALUE;
	        } else if (firstChar != '+')
	            throw new NumberFormatException(s.substring(begin, end));
	        if (end - begin == 1) // Cannot have lone "+" or "-"
	            throw new NumberFormatException(s.substring(begin, end));
	        begin++;
	    }
	
	    int multmin = limit / radix;
	    while (begin < end) {
	    	int digit = Character.digit(s.charAt(begin++),radix);
	        if (digit < 0) {
	            throw new NumberFormatException(s);
	        }
	        if (result < multmin) {
	            throw new NumberFormatException(s);
	        }
	        result *= radix;
	        if (result < limit + digit) {
	            throw new NumberFormatException(s);
	        }
	        result -= digit;
	    }
	   
	    return negative ? result : -result;
	}

	public static final boolean equals(String str0, int begin, int end, String str1)
	{
		if (end- begin != str1.length())
		{
			return false;
		}
		for (int i = 0; begin < end; ++i, ++begin)
		{
			if (str0.charAt(begin) != str1.charAt(i))
			{
				return false;
			}
		}
		return true;
   }
   
	public static final boolean equals(CharSequence str0, int begin, int end, String str1)
	{
		if (str0 instanceof String)
		{
			return equals((String)str0, begin, end, str1);
		}
		if (end- begin != str1.length())
		{
			return false;
		}
		for (int i = 0; begin < end; ++i, ++begin)
		{
			if (str0.charAt(begin) != str1.charAt(i))
			{
				return false;
			}
		}
		return true;
   }
   
   public static final boolean startsWith(String str0, int begin, int end, String str1)
   {
	   if (end- begin < str1.length())
	   {
		   return false;
	   }
	   for (int i = 0; i < str1.length(); ++i, ++begin)
	   {
		   if (str0.charAt(begin) != str1.charAt(i))
		   {
			   return false;
		   }
	   }
	   return true;
   }
   
   public static final boolean equals(String str0, int begin0, String str1, int begin1, int length)
   {
	   for (int i = begin0, j = begin1; i < length + begin0; ++i)
	   {
		   if (str0.charAt(i) != str1.charAt(j))
		   {
			   return false;
		   }
	   }
	   return true;
   }
   
   public static long getLongOfVersion(String version){
    	boolean beta = version.endsWith("beta");
    	if (beta)
    		version = version.substring(0, version.length()-4);
    	int shifting=0, erg=0;
    	int tmp=0;
    	for (int i=0;i<version.length();i++){
    		final char c = version.charAt(i);
    		if (c == '_' || c == ' ' || c == '.'){
    			if ((shifting +=16) == 64 || tmp > (Short.MAX_VALUE/2))
    				return -1;
    			erg += tmp << shifting;
    			tmp = 0;
    		}else if (c <= '9' && c>='0'){
    			tmp = tmp * 10 + c - '0';
    		}
    	}
    	return beta ? erg * 2 + 1 : erg * 2;
    }

	public static final int indexOf(CharSequence cs, int from, int to, char c){
		for (int i=from;i<to;++i)
			if (cs.charAt(i) == c)
				return i;
		return -1;
	}

	public static final int indexOf(String cs, int from, int to, char c){
		for (int i=from;i<to;++i)
			if (cs.charAt(i) == c)
				return i;
		return -1;
	}
	
	public static final int indexOf(String cs, int from, int to, char c[])
	{
		for (int i=from;i<to;++i)
			for (int j = 0; j < c.length; ++j)
				if (cs.charAt(i) == c[j])
					return i;
		return -1;
	}

   	public static final String toHtml(CharSequence ch){
   		StringBuilder builder = new StringBuilder(ch.length()*2);
   		builder.append("<html><body>");
   		for (int i=0;i<ch.length();i++){
   			final char c = ch.charAt(i);
   			switch(c){
   				case '\n': builder.append('"').append('<').append('b').append('r').append(' ').append('/').append('>');break;
   				default:builder.append(c);
   			}
   		}
		return builder.append("</body></html>").toString();
    }

   	public final String[] split(CharSequence name, int from, int to, char c) {
   		if (name instanceof String)
   		{
   			split((String)name, from, to, c, false, al);
   			String res[] = new String[al.size()];
   			res = al.toArray(res);
   			al.clear();
   			return res;
   		}
		for (int i = from; i < to; ++i)
		{
			if (name.charAt(i) == c)
			{
				al.add(strB.toString());
				strB.setLength(0);
			}
			else
			{
				strB.append(name.charAt(i));
			}
		}
		if (strB.length() == to - from)
		{
			return new String[] {strB.toString()};
		}
		al.add(strB.toString());
		strB.setLength(0);
		String[] res = al.toArray(new String[al.size()]);
		al.clear();
		return res;
	}

   	public static final ArrayList<String> split(String name, char c, ArrayList<String> al)
   	{
		int i = name.indexOf(c);
		if (i == -1)
		{
			al.add(name);
			return al;
		}
		al.add(name.substring(0, i++));
		while (true)
		{
			int index = name.indexOf(c, i);
			if (index == -1)
			{
				break;
			}			
			al.add(name.substring(i, index));
			i = index + 1;
		}
		al.add(name.substring(i, name.length()));
		return al;
   	}
   	
   	public static final void split(String name, char c, IntegerArrayList ial)
   	{
		int i = name.indexOf(c);
		ial.add(0);
		if (i == -1)
		{
			ial.add(name.length());
		}
		ial.add(i++);
		while (true)
		{
			int index = name.indexOf(c, i);
			if (index == -1)
			{
				break;
			}
			ial.add(i);
			ial.add(index);
			i = index + 1;
		}
		ial.add(name.length());
   	}
   	
   	public static final void split(String name, int from, int to, char c, boolean considerDoubles, ArrayList<String> al) {
		int i = indexOf(name, from, to, c);
		if (i < 0)
		{
			al.add(name.substring(from, to));
			return;
		}
		if (i != 0 || considerDoubles)
		{
			al.add(name.substring(from, i++));
		}
		while (true)
		{
			int index = indexOf(name, i, to, c);
			if (index < 0)
			{
				break;
			}
			if (index != i || considerDoubles)
			{
				al.add(name.substring(i, index));
			}
			i = index + 1;
		}
		if (i != to || considerDoubles)
		{
			al.add(name.substring(i, to));
		}
	}   	
   	
   	public static final void split(String name, int from, int to, char c, boolean considerDoubles, IntegerArrayList ial) {
		int i = indexOf(name, from, to, c);
		if (i < 0)
		{
			ial.add(from);
			ial.add(to);
			return;
		}
		if (i != 0 || considerDoubles)
		{
			ial.add(from);
			ial.add(i++);
		}
		while (true)
		{
			int index = indexOf(name, i, to, c);
			if (index < 0)
			{
				break;
			}
			if (index != i || considerDoubles)
			{
				ial.add(i);
				ial.add(index);			}
			i = index + 1;
		}
		if (i != to || considerDoubles)
		{
			ial.add(i);
			ial.add(to);
		}
	}
   	
   	public static final void split(String name, int from, int to, char c[], boolean considerDoubles, ArrayList<String> al) {
		int i = indexOf(name, from, to, c);
		if (i < 0)
		{
			al.add(name.substring(from, to));
		}
		if (i != 0 || considerDoubles)
		{
			al.add(name.substring(from, i++));
		}
		while (true)
		{
			int index = indexOf(name, i, to, c);
			if (index < 0)
			{
				break;
			}
			if (index != i || considerDoubles)
			{
				al.add(name.substring(i, index));
			}
			i = index + 1;
		}
		if (i != to || considerDoubles)
		{
			al.add(name.substring(i, to));
		}
	}
   	
   	public static final class StringSplitIterator implements Iterator<String>, CharSequence
   	{
   		int lhs = 0;
   		int rhs;
   		char c;
   		String str;
   		
   		public StringSplitIterator(String str, char c)
   		{
   			this.str = str;
   			this.c = c;
   			rhs = str.indexOf(c);
   			if (rhs == -1)
   			{
   				rhs = str.length();
   			}
   		}

		@Override
		public boolean hasNext() {
			return rhs != str.length();
		}
		
		@Override
		public String next() {
			String res = str.substring(lhs, rhs);
			increment();
			return res;
		}
		
		public int begin()
		{
			return lhs;
		}
		
		public int end()
		{
			return rhs;
		}
		
		public void increment()
		{
			lhs = rhs + 1;
			rhs = str.indexOf(lhs, c);	
		}

		@Override
		public char charAt(int index) {
			return str.charAt(lhs + index);
		}

		@Override
		public int length() {
			return rhs - lhs;
		}

		@Override
		public String subSequence(int start, int end) {
			return str.substring(start + lhs, end + lhs);
		}
   	}

   	
   	public static final class StringSplitIterator2 implements Iterator<String>, CharSequence
   	{
   		private String str;
   		private int to;
   		int lhs, rhs;
   		char c[];
   		boolean considerDoubles;
   	
  		public StringSplitIterator2()
   		{
   		}
   		
  		public StringSplitIterator2(String str, int from, int to, char c[], boolean considerDoubles)
   		{
   			this.str = str;
   			this.to = to;
   			this.lhs = from;
   			this.rhs = indexOf(str, from, to, c);
   			if (rhs == -1)
   			{
   				rhs = to;
   			}
   			this.c = c;
   			this.considerDoubles = considerDoubles;
   		}
   		
   		public void reset(String str, int from, int to, char c[], boolean considerDoubles)
   		{
   			this.str = str;
   			this.to = to;
   			this.lhs = from;
   			this.rhs = indexOf(str, from, to, c);
   			if (rhs == -1)
   			{
   				rhs = to;
   			}
   			this.c = c;
   			this.considerDoubles = considerDoubles;
   		}
   		
   		public final String getStr()
   		{
			return rhs >= 0 ? str.substring(lhs, rhs) : null;
   		}
   		
   		@Override
   		public final String toString()
   		{
   			return rhs >= 0 ? str.substring(lhs, rhs) : null;
   		}
   		
   		public void increment()
   		{
   			do
   			{
	   			lhs = rhs;
	   			//System.out.println(str + " " + rhs + " "+ to);
	   			rhs = indexOf(str, rhs +1, to, c);
	   			if (rhs == -1)
	   			{
	   				rhs = to;
	   			}
   			}while (considerDoubles && lhs + 1== rhs);
   		}

		@Override
		public char charAt(int index) {
			return str.charAt(lhs + index);
		}
		@Override
		public int length() {
			return rhs - lhs;
		}
		@Override
		public String subSequence(int start, int end) {
			return str.substring(start + lhs, end + lhs);
		}
		@Override
		public boolean hasNext() {
			return lhs != to;
		}
		@Override
		public String next() {
			String str = getStr();
			increment();
			return str;
		}
   	}
   	
   	public static final String[] splitToArray(String name, int from, int to, char c[], boolean considerDoubles, ArrayList<String> al) {
		int i = indexOf(name, from, to, c);
		if (i < 0)
		{
			return new String[] {name.substring(from, to)};
		}
		if (i != 0 || considerDoubles)
		{
			al.add(name.substring(from, i++));
		}
		while (true)
		{
			int index = indexOf(name, i, to, c);
			if (index < 0)
			{
				break;
			}
			if (index != i || considerDoubles)
			{
				al.add(name.substring(i, index));
			}
			i = index + 1;
		}
		if (i != to || considerDoubles)
		{
			al.add(name.substring(i, to));
		}
		String[] res = al.toArray(new String[al.size()]);
		al.clear();
		return res;
	}
   	
	public static String setFileEnding(String name, String ending) {
		if (name.toLowerCase().endsWith(ending) && name.length() > ending.length() + 1&& name.charAt(name.length() - ending.length() - 1) == '.')
		{
			return name;
		}
		return name + '.' + ending;
	}

	public static final char[] getChars(StringBuilder strB, char[] chBuf) {
		if (strB.length() > chBuf.length)
		{
			chBuf = new char[strB.length()];
		}
		strB.getChars(0, strB.length(), chBuf, 0);
		return chBuf;
	}


	public static final void parseDoubles(String line, int begin, int end, ArrayList<String> al, DoubleArrayList ial, char seperators[]) {
		StringUtils.split(line, begin, end, seperators, false, al);
		for (int i = 0; i < al.size(); ++i)
		{
			String str = al.get(i);
			if (str.equals("nan"))
			{
				ial.add(Double.NaN);
			}
			else
			{
				ial.add(Double.parseDouble(str));
			}
		}
		al.clear();
	}

	public static final void writeTapSeperated(double[] data, File output, int columns) throws IOException{
		FileWriter outStream = new FileWriter(output);
		BufferedWriter outBuf = new BufferedWriter(outStream);
		writeTapSeperated(data, outBuf, columns);
		outBuf.close();
		outStream.close();
	}

	public static final void writeTapSeperated(double[] data, BufferedWriter outBuf, int columns) throws IOException{
		char chBuf[] = new char[1024];
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < data.length; i += columns)
		{
			strB.append(data[i]);
			for (int j = 1; j < columns; ++j)
			{
				strB.append('\t').append(data[i + j]);
			}
			chBuf = StringUtils.writeAndReset(outBuf, strB, chBuf);
			outBuf.newLine();
		}
	}


	public static void writeTapSeperated(DoubleArrayList dal, BufferedWriter outBuf, int columns) throws IOException {
		char chBuf[] = new char[1024];
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < dal.size(); i += columns)
		{
			strB.append(dal.getD(i));
			for (int j = 1; j < columns; ++j)
			{
				strB.append('\t').append(dal.getD(i + j));
			}
			chBuf = StringUtils.writeAndReset(outBuf, strB, chBuf);
			outBuf.newLine();
		}
	}


	public static void writeTapSeperated(DoubleArrayList dal, File output, int columns) throws IOException{
		FileWriter writer = new FileWriter(output);
		BufferedWriter outBuf = new BufferedWriter(writer);
		writeTapSeperated(dal, outBuf, columns);
		outBuf.close();
		writer.close();

	}
}
