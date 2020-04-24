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
import java.util.ArrayList;
import java.util.List;

import util.keyfunction.KeyFunctionInt;
import util.keyfunction.KeyFunctionLong;

public class ListTools {	
	public static final <T> void clean(ArrayList<WeakReference<T> > list)
	{
		removeAll(list, null);
	}
	
	public static final <T> void removeAll(ArrayList<WeakReference<T>> list, T obj) {
		int writeIndex = 0;
		for (int i = 0; i< list.size(); ++i)
		{
			WeakReference<T> ref = list.get(i);
			if (ref.get() != obj)
			{
				list.set(writeIndex++, ref);
			}
		}
		for (int i = list.size() - 1; i >= writeIndex; --i)
		{
			list.remove(i);
		}
	}
	
	public static <T, O> int binarySearch(T[] a, int fromIndex, int toIndex, O key, HeterogenousComparator<? super T, ? super O> c)
	{
        int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = a[mid];
            int cmp = c.compare(midVal, key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
	}
	
	public static <T, O> int binarySearch(List<T> a, int fromIndex, int toIndex, O key, HeterogenousComparator<? super T, ? super O> c)
	{
        int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            T midVal = a.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
	}
	
	public static <T> int binarySearch(T[] a, int fromIndex, int toIndex, long key, KeyFunctionLong<? super T> keyfunction)
	{
		int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final long cmp = keyfunction.getKey(a[mid]);
            if (cmp < key)
                low = mid + 1;
            else if (cmp > key)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
	}
	
	public static <T> int binarySearch(T[] a, int fromIndex, int toIndex, int key, KeyFunctionInt<? super T> keyfunction)
	{
        int low = fromIndex;
        int high = toIndex - 1;
        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final int cmp = keyfunction.getKey(a[mid]);
            if (cmp < key)
                low = mid + 1;
            else if (cmp > key)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
	}

	public static final <T> void removeRange(ArrayList<T> list, int begin, int end) {
		while (end != list.size())
		{
			list.set(begin, list.get(end));
			++begin;
			++end;
		}
		while (end != begin)
		{
			--end;
			list.remove(end);
		}
	}
}
