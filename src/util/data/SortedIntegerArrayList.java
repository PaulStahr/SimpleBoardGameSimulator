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

import java.util.AbstractList;
import java.util.Arrays;

public class SortedIntegerArrayList extends AbstractList<Integer> implements SortedIntegerList{
    public static final ReadOnlySortedIntegerArrayList EMPTY_LIST = new SortedIntegerArrayList().readOnly();
	private int data[];
	private int length;
	
	public SortedIntegerArrayList(){
		data = UniqueObjects.EMPTY_INT_ARRAY;
	}
	
	public SortedIntegerArrayList(int initialElements){
		data = new int[initialElements];
	}
	
	public int pop(){
		int last = data[length - 1];
		--length;
		return last;
	}
	
	@Override
	public Integer set(int index, Integer value){
		throw new UnsupportedOperationException();
	}
	
	public int set(int index, int value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean add(Integer value){
		return add((int)value);
	}
	
	public boolean add(int value){
		int index = indexOf(value);
		if (index >= 0)
		{
			return false;
		}
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		//System.out.println("Add " + value + ' ' + Arrays.toString(data) + ' ' +(-1 -index) + ' ' + (-index) + ' ' + (length + index + 1));
		System.arraycopy(data, - 1 - index, data, -index, length + index + 1);
		data[-1-index] = value;
		++length;
		return true;
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
	public int size() {
		return length;
	}

	public boolean contains(Integer value){
		return indexOf((int)value) >= 0;
	}
	
	public boolean contains(int value){
		return indexOf(value) >= 0;
	}
	
	public int indexOf(Integer value){
		return indexOf((int)value);
	}
	
	public int indexOf(int value){
		return Arrays.binarySearch(data, 0, length, value);
	}

	public int[] toArrayI() {
		return Arrays.copyOf(data, length);
	}
	

	public void removeObject(int id) {
		int index = Arrays.binarySearch(data, 0, length, id);
		if (index >= 0)
		{
			System.arraycopy(data, index + 1, data, index, length - index - 1);
			--length;
		}
	}
	
	@Override
	public void clear(){
		length = 0;
	}
	
	public final int countMatches(int data[], int begin, int end)
	{
		if (end == begin || this.length == 0)
		{
			return 0;
		}
		int i = 0, j = begin, matches = 0;
		while (true)
		{
			if (this.data[i] < data[j])
			{
				if (++i == this.length)
				{
					return matches;
				}
			}
			else if (this.data[i] > data[j])
			{
				if (++j == end)
				{
					return matches;
				}
			}
			else
			{
				++matches;
				if (++i == this.length || ++j == end)
				{
					return matches;
				}
			}
		}
	}
	
	public final int countMatches(SortedIntegerArrayList other)
	{
		return countMatches(other.data, 0, other.length);
	}
	
	@Override
	public final boolean hasMatch(int data[], int begin, int end)
	{
		if (end == begin || this.length == 0)
		{
			return false;
		}
		int i = 0, j = begin;
		while (true)
		{
			if (this.data[i] < data[j])
			{
				if (++i == this.length)
				{
					return false;
				}
			}
			else if (this.data[i] > data[j])
			{
				if (++j == end)
				{
					return false;
				}
			}
			else
			{
				return true;
			}
		}
	}

	public final boolean hasMatch(SortedIntegerArrayList other)
	{
		return hasMatch(other.data, 0, other.length);
	}

	public ReadOnlySortedIntegerArrayList readOnly() {
		return new ReadOnlySortedIntegerArrayList();
	}
	

	@Override
	public void setElem(int index, int value) {
		throw new RuntimeException();
	}
	
	public class ReadOnlySortedIntegerArrayList extends AbstractList<Integer> implements SortedIntegerList
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
		public int size() {
			return length;
		}

		public boolean contains(Integer value){
			return indexOf((int)value) >= 0;
		}
		
		public boolean contains(int value){
			return indexOf(value) >= 0;
		}
		
		public int indexOf(Integer value){
			return indexOf((int)value);
		}
		
		public int indexOf(int value){
			return Arrays.binarySearch(data, 0, length, value);
		}

		public int[] toArrayI() {
			return Arrays.copyOf(data, length);
		}

		@Override
		public boolean hasMatch(int[] data, int begin, int end) {
			return SortedIntegerArrayList.this.hasMatch(data, 0, length);
		}

		@Override
		public void setElem(int index, int value) {
			throw new RuntimeException();
		}
	}

}
