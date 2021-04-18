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
package util.data;

import java.nio.DoubleBuffer;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.function.Predicate;

import util.ArrayUtil;
import util.Buffers;

public class DoubleArrayList extends AbstractList<Double> implements DoubleList{
	private double data[];
	private int length;

	public void fill(DoubleBuffer buf){Buffers.fillDoubleBuffer(buf, data, length);}

	public DoubleArrayList(){this(5);}

	public DoubleArrayList(int initialElements){data = new double[initialElements];}

	public double pop(){
		double last = data[length - 1];
		--length;
		return last;
	}

	@Override
	public Double set(int index, Double value){
		double old = data[index];
		data[index] = value;
		return old;
	}

	public double set(int index, double value){
		double old = data[index];
		data[index] = value;
		return old;
	}

	@Override
	public boolean add(Double value){return add((double)value);}

	public void add(DoubleList dl){
		int size = dl.size();
		ensureSize(this.length + size);
		for (int i = 0; i < dl.size(); ++i)
		{
			data[length++] = dl.getD(i);
		}
	}
	
	private void ensureSize(int size)
	{
		if (size >= data.length){
			data = Arrays.copyOf(data, Math.max(size, data.length * 2));
		}
	}
	
	public boolean add(double value){
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		data[length++] = value;
		return true;
	}
	
	public boolean add(double value0, double value1, double value2){
		if (length + 2 >= data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 3, data.length * 2));
		}
		data[length++] = value0;
		data[length++] = value1;
		data[length++] = value2;
		return true;
	}
	
	@Override
	public Double get(int index) {
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}
	
	@Override
    public double getD(int index){
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}
	
	public final double sum()
	{
		double sum = 0;
		for (int i = 0; i < length; ++i)
		{
			sum += data[i];
		}
		return sum;
	}
	
	public final double diffSumQ(double avarage)
	{
		double var = 0;
		for (int i = 0; i < length; ++i)
		{
			double diff = data[i] - avarage;
			var += diff * diff;
		}
		return var;
	}

	@Override
	public int size() {return length;}

	public boolean contains(Double value){return indexOf((double)value) != -1;}

	public boolean contains(double value){return indexOf(value) != -1;}

	public int indexOf(Double value){return indexOf((double)value);}

	public int indexOf(double value){return ArrayUtil.linearSearch(data, 0, length, value);}

	public double[] toArrayD() {return Arrays.copyOf(data, length);}

	@Override
	public void clear(){length = 0;}

	public void setSize(int i) {
		length = i;
		if (data.length < length)
		{
			data = Arrays.copyOf(data, length);
		}
	}

	@Override
	public void setElem(int index, double value) {data[index] = value;}

    @Override
    public boolean removeIf(Predicate<? super Double> predicate) {
        int oldLength = length;
        length = ArrayUtil.removeIf(data, 0, length, predicate);
        return oldLength != length;
    }

	public double average() {
		return sum() / size();
	}

}
