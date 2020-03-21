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

import java.nio.*;

import geometry.Vector3d;
/**
 * Diese Klasse enth\u00E4lt Methoden, die die Verwendung von Buffern erleichtern
 * 
 * @author Paul  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF ORStahr
 * @version 04.02.2012
 */
public abstract class Buffers
{
	public static final ByteBuffer NULL_POINTER = ByteBuffer.allocateDirect(0);
    private Buffers(){}

    /**
     * Erzeugt einen direkten Float Buffer
     * @param elements die Anzahl der Float Elemte
     */
    public static final FloatBuffer createFloatBuffer(int elements){
        return ByteBuffer.allocateDirect(elements<<2).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    /**
     * Erzeugt einen direkten Float Buffer
     * @param elements die Anzahl der Float Elemte
     */
    public static final FloatBuffer[] createFloatBuffer(int elements, int count){
        ByteBuffer bb =  ByteBuffer.allocateDirect((elements * count)<<2).order(ByteOrder.nativeOrder());
        FloatBuffer res[] = new FloatBuffer[count];
        for (int i = 0; i < count; ++i)
        {
        	bb.position((i * elements) << 2);
        	bb.limit(((i + 1) * elements) << 2);
        	res[i] = bb.asFloatBuffer();
        }
        return res;
    }
    
    public static final void put(IntBuffer buf, Vector3d vec, int pos)
    {
    	buf.put(pos++, (int)vec.x).put(pos++, (int)vec.y).put(pos++, (int)vec.z);
    }

    public static final void put(ShortBuffer buf, Vector3d vec, int pos)
    {
    	buf.put(pos++, (short)vec.x).put(pos++, (short)vec.y).put(pos++, (short)vec.z);
    }

    public static final void putRev(ShortBuffer buf, Vector3d vec, int pos)
    {
    	buf.put(pos++, (short)vec.z).put(pos++, (short)vec.y).put(pos++, (short)vec.x);
    }
    
    public static final void putRev(FloatBuffer buf, Vector3d vec, int pos)
    {
    	buf.put(pos++, (float)vec.z).put(pos++, (float)vec.y).put(pos++, (float)vec.x);
    }

    public static final void get(IntBuffer buf, Vector3d vec, int pos)
    {
    	vec.x = buf.get(pos++);
    	vec.y = buf.get(pos++);
    	vec.z = buf.get(pos++);
    }
    
    public static final void getRev(IntBuffer buf, Vector3d vec, int pos)
    {
    	vec.z = buf.get(pos++);
    	vec.y = buf.get(pos++);
    	vec.x = buf.get(pos++);
    }

    
    public static final void getRev(FloatBuffer buf, Vector3d vec, int pos)
    {
    	vec.z = buf.get(pos++);
    	vec.y = buf.get(pos++);
    	vec.x = buf.get(pos++);
    }

    public static final void get(ShortBuffer buf, Vector3d vec, int pos)
    {
    	vec.x = buf.get(pos++);
    	vec.y = buf.get(pos++);
    	vec.z = buf.get(pos++);
    }

    public static final void getRev(ShortBuffer buf, Vector3d vec, int pos)
    {
    	vec.z = buf.get(pos++);
    	vec.y = buf.get(pos++);
    	vec.x = buf.get(pos++);
    }

    /**
     * Erzeugt einen direkten FloatBuffer
     * @param data[] Inhalt des Buffers
     */
    public static final FloatBuffer createFloatBuffer(float... data){
        return fillFloatBuffer(createFloatBuffer(data.length), data);
    }
    
    public static final FloatBuffer ensureCapacity(int elements, FloatBuffer floatBuffer)
    {
    	if (floatBuffer == null || elements > floatBuffer.capacity())
    	{
    		return createFloatBuffer(elements);
    	}
    	floatBuffer.limit(elements);
    	return floatBuffer;
    }
    
	public static final IntBuffer ensureCapacity(int elements, IntBuffer intBuffer) {
    	if (intBuffer == null || elements > intBuffer.capacity())
    	{
    		return createIntBuffer(elements);
    	}
    	intBuffer.limit(elements);
    	return intBuffer;	
    }

    /**
     * F\u00FCllt einen Buffer mit Float Werten
     * @param buf der FloatBuffer der gef\u00FCllt werden soll
     * @param data[] die Daten mit denen der FloatBuffer gef\u00FCllt werden soll
     */
    public static final FloatBuffer fillFloatBuffer(FloatBuffer buf, float... data){
        for (int i=0;i<data.length;i++)
            buf.put(i,data[i]);        
        return buf;
    }

    /**
     * Erzeugt einen direkten Float Buffer
     * @param elements die Anzahl der Float Elemte
     */
    
    public static final DoubleBuffer createDoubleBuffer(int elements){
        return ByteBuffer.allocateDirect(elements<<3).order(ByteOrder.nativeOrder()).asDoubleBuffer();
    }

    /**
     * Erzeugt einen direkten FloatBuffer
     * @param data[] Inhalt des Buffers
     */
    public static final DoubleBuffer createDoubleBuffer(double data[]){
        return fillDoubleBuffer(createDoubleBuffer(data.length), data);
    }

    /**
     * F\u00FCllt einen Buffer mit Float Werten
     * @param buf der FloatBuffer der gef\u00FCllt werden soll
     * @param data[] die Daten mit denen der FloatBuffer gef\u00FCllt werden soll
     */
    public static final DoubleBuffer fillDoubleBuffer(DoubleBuffer buf, double data[]){
        for (int i=0;i<data.length;i++)
            buf.put(i,data[i]);        
        return buf;
    }


    /**
     * F\u00FCllt einen Buffer mit Float Werten
     * @param buf der FloatBuffer der gef\u00FCllt werden soll
     * @param data[] die Daten mit denen der FloatBuffer gef\u00FCllt werden soll
     */
    public static final DoubleBuffer fillDoubleBuffer(DoubleBuffer buf, double data[], int size){
        for (int i=0;i<size;i++)
            buf.put(i,data[i]);        
        return buf;
    }
    
    /**
     * Creates a direct int Buffer
     * @param elements number of ints
     */
    public static final IntBuffer createIntBuffer(int elements){
        return ByteBuffer.allocateDirect(elements<<2).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    /**
     * Creates a direct int Buffer
     * @param elements number of ints
     */
    public static final ShortBuffer createShortBuffer(int elements){
        return ByteBuffer.allocateDirect(elements<<1).order(ByteOrder.nativeOrder()).asShortBuffer();
    }
/**
     * Erzeugt einen direkten ByteBuffer
     * @param elements die Anzahl der Int Elemte
     */
    public static final ByteBuffer createByteBuffer(int elements){
        return ByteBuffer.allocateDirect(elements<<2).order(ByteOrder.nativeOrder());
    }

    /**
     * Erzeugt einen direkten IntBuffer
     * @param data[] Inhalt des Buffers
     */
    public static final IntBuffer createIntBuffer(int data[]){
        return fillIntBuffer(createIntBuffer(data.length), data);
    }
    
    /**
     * Erzeugt einen direkten ByteBuffer
     * @param data[] Inhalt des Buffers
     */
    public static final ByteBuffer createByteBuffer(byte data[]){
        return fillByteBuffer(createByteBuffer(data.length), data);
    }
    
    /**
     * F\u00FCllt einen Buffer mit Integer Werten
     * @param buf der IntBuffer der gef\u00FCllt werden soll
     * @param data[] die Daten mit denen der IntBuffer gef\u00FCllt werden soll
     */
    public static final IntBuffer fillIntBuffer(IntBuffer buf, int data[]){
        for (int i=0;i<data.length;i++)
            buf.put(i,data[i]);
        return buf;
    }

    public static final IntBuffer fillIntBuffer(IntBuffer buf, int data[], int size){
        for (int i=0;i<size;i++)
            buf.put(i,data[i]);
        return buf;
    }

    /**
     * F\u00FCllt einen Buffer mit Byte Werten
     * @param buf der ByteBuffer der gef\u00FCllt werden soll
     * @param data[] die Daten mit denen der ByteBuffer gef\u00FCllt werden soll
     */
    public static final ByteBuffer fillByteBuffer(ByteBuffer buf, byte data[]){
        for (int i=0;i<data.length;i++)
            buf.put(i,data[i]);        
        return buf;
    }

	public static final String toString(DoubleBuffer b) {
		StringBuilder strB = new StringBuilder(b.limit() * 2 + 2);
		strB.append('[');
		for (int i = 0; i < b.limit(); ++i)
		{
			if (i != 0)
			{
				strB.append(',');
			}
			strB.append(b.get(i));
		}
		strB.append(']');
		return strB.toString();
	}

	public static final String toString(IntBuffer b) {
		StringBuilder strB = new StringBuilder(b.limit() * 2 + 2);
		strB.append('[');
		for (int i = 0; i < b.limit(); ++i)
		{
			if (i != 0)
			{
				strB.append(',');
			}
			strB.append(b.get(i));
		}
		strB.append(']');
		return strB.toString();
	}

}
