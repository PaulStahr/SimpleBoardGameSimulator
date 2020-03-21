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
import java.util.EmptyStackException;

public class Stack<E> {
	private Object data[];
	private int size = 0;
	
	public Stack(int length){
		data = new Object[length];
	}
	
	public Stack(){
		this(5);
	}
	
	public final void push(E o){
		if (size == data.length)
			data = Arrays.copyOf(data, data.length*2);
		data[size++] = o;
	}
	
	@SuppressWarnings("unchecked")
	public final E pop(){
		if (size == 0)
			return null;
		E erg = (E)data[--size];
		data[size] = null;
		return erg;
	}
	
	@SuppressWarnings("unchecked")
	public final E popChecked(){
		if (size == 0)
			throw new EmptyStackException();
		return (E)data[--size];
	}
	
	public final boolean isEmpty(){
		return size == 0;
	}
	
	public final int size(){
		return size;
	}	
}
