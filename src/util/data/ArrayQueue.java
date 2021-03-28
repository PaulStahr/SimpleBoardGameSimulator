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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import util.ArrayUtil;

public class ArrayQueue<E> implements Queue<E>{
	private Object data[] = new Object[2];
	private int bitmask = 1;
	private int begin=0, end=0;

	public void push(E object){
		if (((end +2) & bitmask) == begin){
			Object tmp[] = new Object[data.length*2];
			bitmask = tmp.length - 1; 
			if (begin < end){
				System.arraycopy(data, begin, tmp, 0, end-begin);
			}else{
				System.arraycopy(data, begin, tmp, 0, data.length-begin);
				System.arraycopy(data, 0, tmp, data.length-begin, end);				
			}
			data = tmp;
			end -= begin;
			begin = 0;
		}
		data[end++] = object;
	}
	
	@Override
	@SuppressWarnings({ "unchecked"})
	public E peek(){
		if (isEmpty())
			return null;
		return (E)data[begin++];
	}
	
	@Override
	public boolean isEmpty(){
		return begin==end;
	}
	
	@Override
	public int size() {
		return (end-begin+data.length)&bitmask;
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0) {
		for (E elem : arg0)
		{
			add(elem);
		}
		return true;
	}

	@Override
	public void clear() {
		if (begin < end)
		{
			Arrays.fill(data, begin, end, null);
		}
		else
		{
			Arrays.fill(data, begin, data.length, null);
			Arrays.fill(data, 0, end, null);
		}
		begin = end = 0;
	}

	@Override
	public boolean contains(Object value) {
		if (begin < end)
		{
			return ArrayUtil.linearSearch(data, begin, end, value) >= 0;
		}
		return ArrayUtil.linearSearch(data, begin, data.length, value) >= 0 || ArrayUtil.linearSearch(data, 0, end, value) >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object  o: arg0)
		{
			if (!contains(o))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object value) {
		if (begin < end)
		{
			int index = ArrayUtil.linearSearch(data, begin, end, value);
			if (index < 0)
			{
				return false;
			}
			System.arraycopy(data, index + 1, data, index, end - index - 1);
			--end;
		}
		return ArrayUtil.linearSearch(data, begin, data.length, value) >= 0 || ArrayUtil.linearSearch(data, 0, end, value) >= 0;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		for (Object elem : arg0)
		{
			remove(elem);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		Object result[] = new Object[size()];
		return toArray(result);
	}

	@Override
	public <T> T[] toArray(T[] result) {
		if (begin < end)
		{
			System.arraycopy(data, begin, result, 0, end - begin);
		}
		else
		{
			System.arraycopy(data, begin, result, 0, data.length - begin);
			System.arraycopy(data, begin, result, data.length - begin, end);
		}
		return result;
	}

	@Override
	public boolean add(E arg0) {
		push(arg0);
		return true;
	}

	@Override
	public E element() {
		if (size() == 0)
		{
			throw new NoSuchElementException();
		}
		@SuppressWarnings("unchecked")
        E head = (E)data[begin];
		++begin;
		return head;
	}

	@Override
	public boolean offer(E arg0) {
		return add(arg0);
	}

	@Override
	public E poll() {
		if (size() == 0)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
        E head = (E)data[begin];
		++begin;
		return head;
	}

	@Override
	public E remove() {
		if (size() == 0)
		{
			throw new NoSuchElementException();
		}
		@SuppressWarnings("unchecked")
        E head = (E)data[begin];
		++begin;
		return head;
	}
}
