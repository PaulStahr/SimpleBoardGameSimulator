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

import java.nio.IntBuffer;
import java.util.AbstractList;
import java.util.Arrays;

import util.Buffers;

public class IntegerArrayList extends AbstractList<Integer> implements IntegerList{
	private int data[];
	private int length;
	
	public IntegerArrayList(){
		this(5);
	}
	
	public IntegerArrayList(int initialElements){
		data = new int[initialElements];
	}
	
	public void fill(IntBuffer buf)
	{
		Buffers.fillIntBuffer(buf, data, length);
	}
	
	public int pop(){
		int last = data[length - 1];
		--length;
		return last;
	}
	
	@Override
	public Integer set(int index, Integer value){
		return set(index, (int)value);
	}
	
	public int set(int index, int value){
		int old = data[index];
		data[index] = value;
		return old;
	}
	
	public void setElem(int index, int value)
	{
		data[index] = value;
	}
	
	@Override
	public boolean add(Integer value){
		return add((int)value);
	}
	
	public boolean add(int value){
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		data[length] = value;
		++length;
		return true;
	}
	
	public void add(int value0, int value1, int value2)
	{
		if (length + 2 >= data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 3, data.length * 2));
		}
		data[length++] = value0;
		data[length++] = value1;
		data[length++] = value2;
	}
	
	@Override
	public Integer get(int index) {
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}
	
	public int getI(int index){
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}

	@Override
	public int size() {
		return length;
	}

	public boolean contains(Integer value){
		return indexOf((int)value) != -1;
	}
	
	public boolean contains(int value){
		return indexOf(value) != -1;
	}
	
	public int indexOf(Integer value){
		return indexOf((int)value);
	}
	
	public int indexOf(int value){
		for (int i=0;i<length;++i){
			if (data[i] == value){
				return i;
			}
		}
		return -1;
	}

	public int[] toArrayI() {
		return Arrays.copyOf(data, length);
	}
	
	@Override
	public void clear(){
		length = 0;
	}
	

	public int last() {
		return data[length - 1];
	}

	public ReadOnlyIntegerArrayList readOnly() {
		return new ReadOnlyIntegerArrayList();
	}
	
	public class ReadOnlyIntegerArrayList extends AbstractList<Integer> implements IntegerList
	{
		@Override
		public Integer get(int index) {
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}
		
		public int getI(int index){
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}

		@Override
		public int size() {
			return length;
		}
		
		@Override
		public final void setElem(int index, int elem)
		{
			throw new RuntimeException();
		}

		public boolean contains(Integer value){
			return indexOf((int)value) != -1;
		}
		
		public boolean contains(int value){
			return indexOf(value) != -1;
		}
		
		public int indexOf(Integer value){
			return indexOf((int)value);
		}
		
		public int indexOf(int value){
			for (int i=0;i<length;++i){
				if (data[i] == value){
					return i;
				}
			}
			return -1;
		}

		public int[] toArrayI() {
			return Arrays.copyOf(data, length);
		}
	}

	public void write(int[] out, int begin) {
		System.arraycopy(data, 0, out, begin, size());
	}

}
