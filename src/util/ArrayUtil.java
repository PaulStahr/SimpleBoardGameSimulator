package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

import util.data.DoubleList;
import util.data.IntegerArrayList;
import util.data.IntegerList;

public class ArrayUtil {
	public static final int compareTo(char first[], char second[]) {
        final int len1 = first.length;
        final int len2 = second.length;
        final int lim = Math.min(len1, len2);
       
        for (int k = 0;k < lim; ++k) {
            final char c1 = first[k];
            final char c2 = second[k];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }
	
	public static final int compareTo(CharSequence first, char second[]) {
        final int len1 = first.length();
        final int len2 = second.length;
        final int lim = Math.min(len1, len2);
       
        for (int k = 0;k < lim; ++k) {
            final char c1 = first.charAt(k);
            final char c2 = second[k];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }
	
	public static final int compareTo(String first, char second[]) {
        final int len1 = first.length();
        final int len2 = second.length;
        final int lim = Math.min(len1, len2);
       
        for (int k = 0;k < lim; ++k) {
            final char c1 = first.charAt(k);
            final char c2 = second[k];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

	public static final boolean allEqual(Object[] data, Object object) {
		for (int i = 0; i < data.length; ++i)
		{
			if (data[i] != object)
			{
				return false;
			}
		}
		return true;
	}
	
	public static final double max(double data[])
	{
		double max = Double.NEGATIVE_INFINITY;
		for (double val : data)
		{
			if (val > max)
			{
				max = val;
			}
		}
		return max;
	}
	
	public static final double min(double data[])
	{
		double min = Double.POSITIVE_INFINITY;
		for (double val : data)
		{
			if (val < min)
			{
				min = val;
			}
		}
		return min;
	}
	
	public static final float max(float data[])
	{
		float max = Float.NEGATIVE_INFINITY;
		for (float val : data)
		{
			if (val > max)
			{
				max = val;
			}
		}
		return max;
	}
	
	public static final float min(float data[])
	{
		float min = Float.POSITIVE_INFINITY;
		for (float val : data)
		{
			if (val < min)
			{
				min = val;
			}
		}
		return min;
	}

	public static final int max(int data[])
	{
		int max = Integer.MIN_VALUE;
		for (int val : data)
		{
			if (val > max)
			{
				max = val;
			}
		}
		return max;
	}
	
	public static final int min(int data[])
	{
		int min = Integer.MAX_VALUE;
		for (int val : data)
		{
			if (val < min)
			{
				min = val;
			}
		}
		return min;
	}

	public static final int minIndex(int data[], int begin, int end)
	{
		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i = begin; i < end; ++i)
		{
			if (data[i] < min)
			{
				min = data[i];
				index = i;
			}
		}
		return index;
	}
	
	public void increaseMin(int array[], int begin, int end)
	{
		int idx = minIndex(array, begin, end);
		++array[idx];
	}
	
	public static final void swap(double[] data, int begin0, int begin1, int len) {
		for (int j = 0; j < len; ++j)
		{
			final int id0 = j + begin0, id1 = j + begin1;
         	final double tmp = data[id0];
         	data[id0] = data[id1];
         	data[id1] = tmp;
        }
	}

	public static int reverseLinearSearch(Object data[], int begin, int end, Object value) {
		for (int i = end - 1; i >= begin; --i)
		{
			if (data[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

	public static final int linearSearch(Object data[], Object value)
	{
		return linearSearch(data, 0, data.length, value);
	}
	
	public static final int linearSearch(Object data[], int begin, int end, Object value)
	{
		for (int i = begin; i < end; ++i)
		{
			if (data[i] == value)
			{
				return i;
			}
		}
		return -1;
	}
	
	public static final int linearSearch(int data[], int begin, int end, int value)
	{
		for (; begin < end; ++begin)
		{
			if (data[begin] == value)
			{
				return begin;
			}
		}
		return -1;
	}
	
	public static final int linearSearch(byte data[], int begin, int end, byte value)
	{
		for (; begin < end; ++begin)
		{
			if (data[begin] == value)
			{
				return begin;
			}
		}
		return -1;
	}
	
	public static final int firstEqualIndex(Object data[], Object value)
	{
		return firstEqualIndex(data, 0, data.length, value);
	}

    
    public static int firstEqualIndex(Object data[], int begin, int end, Object key)
    {
        for (; begin < end; ++begin)
        {
            if (data[begin].equals(key))
            {
                return begin;
            }
        }
        return -1;
    }
	
	public static final float[] setToLength(float[] data, int length) {return data.length == length ? data : new float[length];}

	public static float[]	ensureLength(float[] data, int size) 	{return data.length >= size ? data : new float[size];}
    public static byte[]    ensureLength(byte[] data, int size)     {return data.length >= size ? data : new byte[size];}
    public static long[]    ensureLength(long[] data, int size)     {return data.length >= size ? data : new long[size];}

	public static int max(int[] imageColorArray, int begin, int end) {
		int max = Integer.MIN_VALUE;
		for (;begin != end; ++begin)
		{
			max = Math.max(max, imageColorArray[begin]);
		}
		return max;
	}
	

	public static final float max(float[] imageColorArray, int begin, int end) {
		float max = Float.NEGATIVE_INFINITY;
		for (;begin != end; ++begin)
		{
			max = Math.max(max, imageColorArray[begin]);
		}
		return max;
	}
	

	public static void addTo(int[] in, int begin, int end, int[] out, int outBegin) {
		for (; begin < end; ++begin, ++outBegin)
		{
			out[outBegin] += in[begin];
		}
	}
	

	public static void addTo(float[] in, int begin, int end, float[] out, int outBegin) {
		for (; begin < end; ++begin, ++outBegin)
		{
			out[outBegin] += in[begin];
		}
	}
	
	public static void addTo(int[] in, int begin, int end, int[] out, int outBegin, int mult)
	{
		for (; begin < end; ++begin, ++outBegin)
		{
			out[outBegin] += in[begin] * mult;
		}
	}
	
	public static final void divide(int[] in, int begin, int end, int[] out, int outBegin, int div)
	{
		for (; begin < end; ++begin, ++outBegin)
		{
			out[outBegin] = in[begin] / div;
		}
	}

	public static void normalizeTo(int[] imageColorArray, int begin, int end, int to) {
		int max = max(imageColorArray, begin, end);
		if (max == 0)
		{
			return;
		}
		double mult = (double)to / max;
		for (;begin < end; ++begin)
		{
			imageColorArray[begin] = (int)(imageColorArray[begin] * mult) ;			
		}
		/*long mult = ((long)to * (long)Integer.MAX_VALUE) / max;
		for (; begin != end; ++begin)
		{
			imageColorArray[begin] = (int)(((long)imageColorArray[begin] * mult) >> 31) ;
		}*/
	}

	/*
	 * Normalizes the array in a way, that the highest value is equal to to
	 * Returns the scaling factor which was needed to achive this
	 * 
	 */
	public static float normalizeTo(float[] imageColorArray, int begin, int end, float to) {
		float max = max(imageColorArray, begin, end);
		if (max == 0)
		{
			return 0;
		}
		float mult = to / max;
		mult(imageColorArray, begin, end, mult);
		return mult;
	}

	public static float normQ(float data[], int begin, int end, float init)
	{
	    for (; begin < end; ++begin)
	    {
	        init += data[begin] * data[begin];
	    }
	    return init;
	}

	public static void multAdd(float[] in, int iBegin, int iEnd, int[] out, int oBegin, int mult) {
		for (; iBegin < iEnd; ++iBegin, ++oBegin)
		{
			out[oBegin] += mult * in[iBegin];
		}
	}

	public static void multAdd(double[] in, int iBegin, int iEnd, double[] out, int oBegin, double mult) {
		for (; iBegin < iEnd; ++iBegin, ++oBegin)
		{
			out[oBegin] += mult * in[iBegin];
		}	
	}

	public static int count(Object[] object, int objectBegin, int objectEnd,Object o) {
		int res = 0;
		for (int i = objectBegin; i < objectEnd; ++i)
		{
			if (object[i] == o)
			{
				++res;
			}
		}
		return res;
	}

	public static int firstUnsameIndex(Object data[], int begin, int end, Object key)
	{
		for (; begin < end; ++begin)
		{
			if (data[begin] != key)
			{
				return begin;
			}
		}
		return end;
	}

	public static void setTo(int[] data0, int begin0, int end0, float[] data1, int begin1, float mult) {
		for (; begin0 != end0; ++begin0, ++begin1)
		{
			data1[begin1] = data0[begin0] * mult;
		}
	}
	
	public static void setTo(float[] input, int ibegin, int oend, int[] output, int obegin, float mult) {
		if (input == null)
		{
			throw new NullPointerException("InputArray IsNull");
		}
		if (output == null)
		{
			throw new NullPointerException("OutputArray Is Null");
		}
		for (; ibegin != oend; ++ibegin, ++obegin)
		{
			output[obegin] = (int)(input[ibegin] * mult);
		}
	}

	public static void write(int[] data, int begin, int end, DataOutputStream outBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			outBuf.writeInt(data[i]);
		}
	}
	
	public static void write(float[] data, int begin, int end, DataOutputStream outBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			outBuf.writeFloat(data[i]);
		}
	}

	public static void write(IntegerList ial, int begin, int end, DataOutputStream outBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			outBuf.writeInt(ial.getI(i));
		}
	}
	
	
	public static void write(DoubleList dal, int begin, int end, DataOutputStream outBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			outBuf.writeDouble(dal.getD(i));
		}
	}

	public static void readDoubles(DoubleList mat, int begin, int end, DataInputStream inBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			mat.setElem(i,inBuf.readDouble());			
		}
	}

	public static void readIntegers(int[] data, int begin, int end, DataInputStream inBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			data[i] = inBuf.readInt();
		}
	}

	public static void readFloats(float[] data, int begin, int end, DataInputStream inBuf) throws IOException {
		for (int i = begin; i < end; ++i)
		{
			data[i] = inBuf.readFloat();
		}
	}

	public static final void iota(int[] data) {
		for (int i = 0; i < data.length; ++i)
		{
			data[i] = i;
		}
	}

	public static int countNonzero(int[] data) {
		int res = 0;
		for (int elem : data)
		{
			if (elem != 0)
			{
				++res;
			}
		}
		return res;
	}

	public static void invert(int[] data, int offset) {
		for (int i = 0; i < data.length; ++i)
		{
			data[i] = offset - data[i];
		}
	}

	public static void sqrt(double[] result, int begin, int end) {
		for (int i = begin; i < end; ++i)
		{
			result[i] = Math.sqrt(result[i]);
		}
	}

	public static int countNonzero(byte[] data) {
		int res = 0;
		for (int elem : data)
		{
			if (elem != 0)
			{
				++res;
			}
		}
		return res;
	}

	public static double[] fillEquidistant(double begin, double end, double[] data) {
		double mult = (end - begin) / (data.length - 1);
    	for (int i = 0; i < data.length; ++i)
    	{
    		data[i] = begin + i * mult;
    	}
    	return data;
	}

	public static final void mult(double[] data, int from, int to, double mult) {
		for (; from < to; ++from)
		{
			data[from] *= mult;
		}
	}
	public static final void mult(float[] data, int from, int to, float mult) {
		for (; from < to; ++from)
		{
			data[from] *= mult;
		}
	}

	public static void arraycopy(float[] input, int inputBegin, int[] output, int outputBegin, int size) {
		for (size += inputBegin; inputBegin < size; ++inputBegin, ++outputBegin)
		{
			output[outputBegin] = (int)input[inputBegin];
		}
	}

	public static void setLementsAt(byte[] output, int[] inputIndices, byte[] inputValues) {
		for (int i = 0; i < inputIndices.length; ++i)
		{
			output[inputIndices[i]] = inputValues[i];
		}
	}

	public static <E> void sortWeak(ArrayList<E> tmp, Comparator<E> comparator) {
		for (int i = 0; i < tmp.size(); ++i)
		{
			E current_min = tmp.get(i);
			int current_min_index = i;
			for (int j = i + 1; j < tmp.size(); ++j)
			{
				if (comparator.compare(tmp.get(j), current_min) > 0)
				{
					current_min = tmp.get(j);
					current_min_index = j;
				}
			}
			tmp.set(current_min_index, tmp.get(i));
			tmp.set(i, current_min);
		}
	}

	public static void iota(Integer[] data) {
		for (int i = 0; i < data.length; ++i){data[i] = i;}
	}

    public static double qdist(DoubleList l0, int begin0, DoubleList l1, int begin1, int size) {
        double result = 0;
        while (size != 0)
        {
            double diff = l0.getD(begin0++) - l1.getD(begin1++);
            result += diff * diff;
            --size;
        }
        return result;
    }

	public static void arraycopy(float[] source, int inputBegin, double[] dest, int outputBegin, int size) {
		if (inputBegin + size > source.length) {throw new ArrayIndexOutOfBoundsException("Index " + size + inputBegin + " out of bounds for length " + source.length);}
		if (outputBegin + size > dest.length)	{throw new ArrayIndexOutOfBoundsException("Index " + size + outputBegin + " out of bounds for length " + dest.length);}
		for (size += inputBegin; inputBegin < size; ++inputBegin, ++outputBegin)
		{
			dest[outputBegin] = source[inputBegin];
		}
	}

	public static int linearSearch(double[] data, int begin, int end, double value) {
		for (int i = begin; i < end; ++i)
		{
			if (data[i] == value)
			{
				return i;
			}
		}
		return -1;
	}

    public static double distanceQ(float[] data0, int begin0, float[] data1, int begin1, int size) {
        double result = 0;
        for (size += begin0; begin0 < size; ++begin0, ++begin1)
        {
            double diff = data0[begin0] - data1[begin1];
            result += diff * diff;
        }
        return result;
    }

    public static void unifySorted(IntegerArrayList ial) {
        if (ial.isEmpty()) {return;}
        int current = ial.getI(0);
        int write = 0;
        for (int read = 1; read < ial.size(); ++read)
        {
            if (current != ial.getI(read)){
                ial.set(write++, current);
                current = ial.getI(read);
            }
        }
        ial.set(write, current);
        ial.removeRange(write, ial.size());
    }

    public static final int removeIf(byte[] data, int begin, int end, Predicate<? super Byte> predicate) {
        int write = 0;
        for (; begin < end; ++begin)
        {
            if (!predicate.test(data[begin]))
            {
                data[write++] = data[begin];
            }
        }
        return write;
    }
    
    public static final int removeIf(int[] data, int begin, int end, Predicate<? super Integer> predicate) {
        int write = 0;
        for (; begin < end; ++begin)
        {
            if (!predicate.test(data[begin]))
            {
                data[write++] = data[begin];
            }
        }
        return write;
    }
    
    public static final int removeIf(double[] data, int begin, int end, Predicate<? super Double> predicate) {
        int write = 0;
        for (; begin < end; ++begin)
        {
            if (!predicate.test(data[begin]))
            {
                data[write++] = data[begin];
            }
        }
        return write;
    }
}
