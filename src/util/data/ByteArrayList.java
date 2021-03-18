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

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.function.Predicate;

import util.ArrayUtil;
import util.Buffers;

public class ByteArrayList extends AbstractList<Byte> implements ByteList{
	private byte data[];
	private int length;
	
	public ByteArrayList(){
		this(5);
	}
	
	public ByteArrayList(int initialElements){
		data = new byte[initialElements];
	}
	
	public void fill(ByteBuffer buf)
	{
		Buffers.fillByteBuffer(buf, data, length);
	}
	
	public void add(int index, byte value)
	{
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		System.arraycopy(data, index, data, index+1, length - index);
		data[index] = value;
		++length;
	}
	
	public byte removeB(int index)
	{
		byte tmp = data[index];
		System.arraycopy(data, index + 1, data, index, length - index - 1);
		--length;
		return tmp;
	}
	
	@Override
	public Byte remove(int index)
	{
		return removeB(index);
	}
	
	@Override
	public void add(int index, Byte value)
	{
		add(index, (byte)value);
	}
	
	public int pop(){
		int last = data[length - 1];
		--length;
		return last;
	}
	
	@Override
	public Byte set(int index, Byte value){
		return set(index, (byte)value);
	}
	
	public void set(ByteArrayList list){
		if (this.data.length < list.size())
		{
			this.data = Arrays.copyOf(list.data, list.length);
		}
		else
		{
			System.arraycopy(list.data, 0, this.data, 0, list.length);
			this.length = list.length;
		}
	}
	
	public byte set(int index, byte value){
		byte old = data[index];
		data[index] = value;
		return old;
	}
	
	@Override
	public void setElem(int index, byte value)
	{
		data[index] = value;
	}
	
	@Override
	public boolean add(Byte value){
		return add((byte)value);
	}
	
	public boolean add(byte value){
		if (length == data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 1, data.length * 2));
		}
		data[length] = value;
		++length;
		return true;
	}
	

	public void add(ByteArrayList objectIds) {
		add(objectIds.data, 0, objectIds.length);
	}
	
	private void enlargeTo(int length)
	{
		if (length > data.length)
		{
			this.data = Arrays.copyOf(this.data, Math.max(this.data.length, data.length * 2));
		}
	}
	
	public void add(byte data[], int begin, int end)
	{
		enlargeTo(length + end - begin);
		System.arraycopy(data, begin, this.data, length, end - begin);
		length += end - begin;
	}
	
	public void add(byte value0, byte value1, byte value2)
	{
		if (length + 2 >= data.length){
			data = Arrays.copyOf(data, Math.max(data.length + 3, data.length * 2));
		}
		data[length++] = value0;
		data[length++] = value1;
		data[length++] = value2;
	}
	
	@Override
	public Byte get(int index) {
		if (index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
		return data[index];
	}
	
	@Override
	public byte getB(int index){
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
	
	public int indexOf(byte value){
		return ArrayUtil.linearSearch(data, 0, length, value);
	}

	public byte[] toArrayB() {
		return Arrays.copyOf(data, length);
	}
	
	@Override
	public void clear(){
		length = 0;
	}
	

	public int last() {
		return data[length - 1];
	}

	public ReadOnlyByteArrayList readOnly() {
		return new ReadOnlyByteArrayList();
	}
	
	public class ReadOnlyByteArrayList extends AbstractList<Byte> implements ByteList
	{
		@Override
		public Byte get(int index) {
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}
		
		@Override
		public byte getB(int index){
			if (index >= length)
				throw new ArrayIndexOutOfBoundsException(index);
			return data[index];
		}

		@Override
		public int size() {
			return length;
		}
		
		@Override
		public final void setElem(int index, byte elem)
		{
			throw new RuntimeException();
		}

		public boolean contains(Byte value){
			return indexOf((byte)value) != -1;
		}
		
		public boolean contains(byte value){
			return indexOf(value) != -1;
		}
		
		public int indexOf(Byte value){
			return indexOf((byte)value);
		}
		
		public int indexOf(byte value){
			return ArrayUtil.linearSearch(data, 0, length, value);
		}

		public byte[] toArrayB() {
			return Arrays.copyOf(data, length);
		}
	}

	public void write(byte[] out, int begin) {
		System.arraycopy(data, 0, out, begin, size());
	}

    @Override
    public boolean removeIf(Predicate<? super Byte> predicate) {
        int write = 0;
        int read;
        for (read = 0; read < size(); ++read)
        {
            if (!predicate.test(data[read]))
            {
                data[write++] = data[read];
            }
        }
        length = write;
        return write != read;
    }
}
