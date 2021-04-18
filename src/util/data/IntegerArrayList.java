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
import java.util.function.Predicate;

import util.ArrayUtil;
import util.Buffers;

public class IntegerArrayList extends AbstractList<Integer> implements IntegerList{
	private int data[];
	private int length;
	
	public IntegerArrayList(){this(5);}

	public IntegerArrayList(int initialElements){data = new int[initialElements];}

	public IntegerArrayList(String stringList){
		this(0);
		if (!stringList.equals("")) {
			String[] SplitString = stringList.split(",");
			for (String str : SplitString) {
				add(Integer.valueOf(str));
			}
		}

	}

	public void add(int index, int value)
	{
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		System.arraycopy(data, index, data, index+1, length - index);
		data[index] = value;
		++length;
	}
	
	public int removeI(int index)
	{
		int tmp = data[index];
		System.arraycopy(data, index + 1, data, index, length - index - 1);
		--length;
		return tmp;
	}

	public void fill(IntBuffer buf){Buffers.fillIntBuffer(buf, data, length);}

	@Override
	public Integer remove(int index){return removeI(index);}

	@Override
	public void add(int index, Integer value){add(index, (int)value);}

	public int pop(){
		int last = data[length - 1];
		--length;
		return last;
	}

	@Override
	public Integer set(int index, Integer value){return set(index, (int)value);}

	public void set(IntegerArrayList list){
		if (this.data.length < list.size())   {this.data = Arrays.copyOf(list.data, list.length);}
		else                                  {System.arraycopy(list.data, 0, this.data, 0, list.length);}
		this.length = list.length;
	}
	
	public int set(int index, int value){
		int old = data[index];
		data[index] = value;
		return old;
	}

	@Override
	public void setElem(int index, int value){data[index] = value;}

	@Override
	public boolean add(Integer value){return add((int)value);}

	public boolean add(int value){
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		data[length] = value;
		++length;
		return true;
	}

	public void add(IntegerArrayList objectIds) {add(objectIds.data, 0, objectIds.length);}

	private void enlargeTo(int length)
	{
		if (length > data.length){this.data = Arrays.copyOf(this.data, Math.max(this.data.length, data.length * 2));}
	}

	public void add(int data[], int begin, int end)
	{
		enlargeTo(length + end - begin);
		System.arraycopy(data, begin, this.data, length, end - begin);
		length += end - begin;
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
	
	@Override
	public int getI(int index){
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}

	@Override
	public int size() {return length;}

	public boolean contains(Integer value){return indexOf((int)value) != -1;}

	public boolean contains(int value){return indexOf(value) != -1;}

	public int indexOf(Integer value){return indexOf((int)value);}

	public int indexOf(int value){return ArrayUtil.linearSearch(data, 0, length, value);}

	public int[] toArrayI() {return Arrays.copyOf(data, length);}

	@Override
	public void clear(){length = 0;}

	public int last() {return data[length - 1];}

	public ReadOnlyIntegerArrayList readOnly() {return new ReadOnlyIntegerArrayList();}

	public class ReadOnlyIntegerArrayList extends AbstractList<Integer> implements IntegerList
	{
		@Override
		public Integer get(int index) {
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}
		
		@Override
		public int getI(int index){
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}

		@Override
		public int size() {return length;}
		
		@Override
		public final void setElem(int index, int elem){throw new RuntimeException();}

		public boolean contains(Integer value){return indexOf((int)value) != -1;}

		public boolean contains(int value){return indexOf(value) != -1;}

		public int indexOf(Integer value){return indexOf((int)value);}

		public int indexOf(int value){return ArrayUtil.linearSearch(data, 0, length, value);}

		public int[] toArrayI() {return Arrays.copyOf(data, length);}
	}

	public void write(int[] out, int begin) {System.arraycopy(data, 0, out, begin, size());}

	public StringBuilder toString(StringBuilder strB) {
	    if (size() == 0){return strB;}
	    strB.append(getI(0));
	    for (int i = 1; i < size(); ++i) {
	        strB.append(',').append(getI(i));
	    }
	    return strB;
	}

	@Override
    public String toString(){return toString(new StringBuilder()).toString();}

    @Override
    public boolean removeIf(Predicate<? super Integer> predicate) {
        int oldLength = length;
        length = ArrayUtil.removeIf(data, 0, length, predicate);
        return oldLength != length;
    }

    @Override
    public void removeRange(int begin, int end) {
        System.arraycopy(data, end, data, begin, length - end);
        length = end;
    }
}
