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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

public class ArrayTools {
	public static final <T> UnmodifiableArrayList<T> unmodifiableList(T o[]){
		return new UnmodifiableArrayList<T>(o, o.length);
	}

	public static final <T> UnmodifiableArrayList<T> unmodifiableList(T o[], int length){
		return new UnmodifiableArrayList<T>(o, length);
	}

	public static final <T> List<T> unmodifiableSortedList(T o[]) {
		return new UnmodifiableSortedArrayList<T>(o, o.length);
	}

	public static final <T> List<T> unmodifiableSortedList(T o[], int length) {
		return new UnmodifiableSortedArrayList<T>(o, length);
	}
	
	public static boolean allTrue(boolean[] array)
	{
	    for(boolean b : array) if(!b) return false;
	    return true;
	}
	
	public static final <E> E[] push_front(E data[], E obj)
	{
		@SuppressWarnings("unchecked")
		E res[] = (E[])Array.newInstance(data.getClass().getComponentType(), data.length + 1);
		res[0] = obj;
		System.arraycopy(data, 0, res, 1, data.length);
		return res;
	}
	
	public static final <E> E[] insert(E data[], E obj, int position)
	{
		@SuppressWarnings("unchecked")
		E res[] = (E[])Array.newInstance(data.getClass().getComponentType(), data.length + 1);
		System.arraycopy(data, 0, res, 0, position);
		res[position] = obj;
		System.arraycopy(data, position, res, position + 1, data.length - position);
		return res;
	}
	
	public static final int[] insert(int data[], int obj, int position)
	{
		int res[] = new int[data.length + 1];
		System.arraycopy(data, 0, res, 0, position);
		res[position] = obj;
		System.arraycopy(data, position, res, position + 1, data.length - position);
		return res;
	}
	
	public static final <E> E[] push_back(E data[], E obj)
	{
		E res[] = Arrays.copyOf(data, data.length + 1);
		res[data.length] = obj;
		return res;
	}
	
	public static final byte[] push_back(byte data[], int size, byte val)
	{
		if (data.length == size)
		{
			data = Arrays.copyOf(data, data.length * 2 + 1);
		}
		data[size] = val;
		return data;
	}
	
	public static final int[] push_back(int data[], int obj)
	{
		int res[] = Arrays.copyOf(data, data.length + 1);
		res[data.length] = obj;
		return res;
	}
	
	public static final <E> E[] deleteElem(E data[], E obj)
	{
		int count = 0;
		for (int i = 0; i < data.length; ++i)
		{
			if (data[i] == obj)
			{
				++count;
			}
		}
		@SuppressWarnings("unchecked")
		E res[] = (E[])Array.newInstance(data.getClass().getComponentType(), data.length - count);
		count = 0;
		for (int i = 0; i < data.length; ++i)
		{
			if (data[i] == obj)
			{
				res[count++] = data[i];
			}
		}
		return res;
	}	
	
	public static final <E> E[] delete(E data[], int index)
	{
		@SuppressWarnings("unchecked")
		E res[] = (E[])Array.newInstance(data.getClass().getComponentType(), data.length - 1);
		System.arraycopy(data, 0, res, 0, index);
		System.arraycopy(data, index + 1, res, index, data.length - index - 1);
		return res;
	}
	
	
	public static final <E> E[] delete(E data[], E object)
	{
		int index = find(data, 0, data.length, object);
		if (index >= 0)
		{
			return delete(data, index);
		}
		else
		{
			return data;
		}
	}
	
	public static final int[] delete(int data[], int index)
	{
		int res[] = new int[data.length - 1];
		System.arraycopy(data, 0, res, 0, index);
		System.arraycopy(data, index + 1, res, index, data.length - index - 1);
		return res;
	}
	
	public static final <E> E[] add(E data[], int size, E obj)
	{
		if (size == data.length)
		{
			data = Arrays.copyOf(data, size * 2 + 1);
		}
		data[size] = obj;
		return data;
	}
	
	public static final <E> E remove(E data[], int size, int index)
	{
	    E r = data[index];
	    if (index != --size)
	    	System.arraycopy(data, index + 1, data, index, size - index);
	    data[size] = null;
	    return r;
	}

	public static final <E> int remove(E data[], int size, E obj)
	{
		int write = 0;
		for (int read = 0; read < size; ++read)
		{
			if (data[read] != obj)
			{
				data[write] = data[read];
			}
		}
		Arrays.fill(data, write, size, null);
		return write;
	}
	
	public static <E> E[] add(E[] data, int size, int index, E e)
	{
	    if (size == data.length)
	    {
	    	if (size == data.length)
			{
				data = Arrays.copyOf(data, size * 2 + 1);
			}
	    }
	    if (index != size)
	    	System.arraycopy(data, index, data, index + 1, size - index);
	    data[index] = e;
	    return data;
	}
	
	public static final int[] copyOrClone(int origin[], int destination[])
	{
		if (origin.length == destination.length)
		{
			System.arraycopy(origin, 0, destination, 0, origin.length);
			return destination;
		}
		else
		{
			return origin.clone();
		}
	}

	public static class UnmodifiableArrayList<E>extends AbstractList<E> implements RandomAccess{
		private final E[] array;
		private final int length;
		private UnmodifiableArrayList(E[] array, int length){
			this.array = array;
			this.length = length;
		}
		
		@Override
		public final E get(int elem) {
			if (elem > length)
				throw new ArrayIndexOutOfBoundsException(elem);
			return array[elem];
		}
		
		@Override
		public final int size() {
			return length;
		}
		
		@Override
		public final boolean contains(Object o){
			return indexOf(o) > -1;
		}
		
		public final int indexOfEqual(Object value)
		{
			return ArrayUtil.firstEqualIndex(array, 0, length, value);
		}
		
		@Override
		public final int indexOf(Object value){
			return ArrayUtil.linearSearch(array, 0, length, value);
		}

		
		@Override
		public final int lastIndexOf(Object value){
			return ArrayUtil.reverseLinearSearch(array, 0, length, value);
		}
	}
	
	public static interface ObjectToIntTransform<T>
	{
		public int toInt(T o);
	}
	
	
	public static <T> int binarySearch(List<T> a, int fromIndex, int toIndex, int key, ObjectToIntTransform<T> tr) {
		int low = fromIndex;
		int high = toIndex - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = tr.toInt(a.get(mid));
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid;
		}
		return -(low + 1);
	}
	
	public static <T> int binarySearch(List<T> a, int key, ObjectToIntTransform<T> tr) {
		return binarySearch(a, 0, a.size(), key, tr);
	}
	
	public static <T> int binarySearch(T[] a, int fromIndex, int toIndex, int key, ObjectToIntTransform<T> tr) {
		int low = fromIndex;
		int high = toIndex - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int midVal = tr.toInt(a[mid]);
			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid;
		}
		return -(low + 1);
	}
	
	private static class UnmodifiableSortedArrayList<E>extends AbstractList<E> implements RandomAccess{
		private final E[] array;
		private final int length;
		private UnmodifiableSortedArrayList(E[] array, int length){
			this.array = array;
			this.length = length;
		}
	
		
		@Override
		public final E get(int elem) {
			if (elem > length)
				throw new ArrayIndexOutOfBoundsException();
			return array[elem];
		}

		
		@Override
		public final int size() {
			return length;
		}
		
		
		@Override
		public final boolean contains(Object o){
			return indexOf(o) > -1;
		}
		
		
		@Override
		public final int indexOf(Object object){
			return Arrays.binarySearch(array, 0, length, object);
		}

		
		@Override
		public final int lastIndexOf(Object object){
			return Arrays.binarySearch(array, 0, length, object);
		}
	}

	public static int find(int[] ids, int begin, int end, int id) {
		for (int i = begin; i < end; ++i)
		{
			if (ids[i] == id)
			{
				return i;
			}
		}
		return -1;
	}

	public static int find(Object[] data, int begin, int end, Object elem) {
		for (int i = begin; i < end; ++i)
		{
			if (data[i] == elem)
			{
				return i;
			}
		}
		return -1;
	}

	public static int find(Object[] data, Object elem) {
		return find (data, 0, data.length, elem);
	}

	
	public static final <E> int removeReferences(WeakReference<?> data[], int size, E obj)
	{
		int write = 0;
		for (int read=0;read<size;read++){
			if (data[read].get() != obj){
				data[write++] = data[read];
			}
		}
		Arrays.fill(data, write, size, null);
		return write;
	}

	/*private static class UnmodifiableSortedArrayListWithComparator<E>extends AbstractList<E> implements RandomAccess{
		private final E[] array;
		private int length;
		private Comparator<E> comp;
		private UnmodifiableSortedArrayListWithComparator(E[] array, int length, Comparator<E> comp){
			this.array = array;
			this.length = length;
			this.comp = comp;
		}
	
		
		@Override
		public final E get(int elem) {
			if (elem > length)
				throw new ArrayIndexOutOfBoundsException();
			return array[elem];
		}

		
		@Override
		public final int size() {
			return length;
		}
		
		
		@Override
		public final boolean contains(Object o){
			return indexOf(o) > -1;
		}
		
		
		@Override
		public final int indexOf(Object object){
			return Arrays.binarySearch(array, 0, length, object, comp);
		}

		
		@Override
		public final int lastIndexOf(Object object){
			return Arrays.binarySearch(array, 0, length, object, null);
		}
	}*/
}
