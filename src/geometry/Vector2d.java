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
package geometry;

/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public final class Vector2d implements Vectord{
	public double x, y;

	public Vector2d(){}
	
	public Vector2d(double x, double y)
	{
		this.x = x;this.y = y;
	}
	
	public Vector2d(Vector2d vec)
	{
		x = vec.x;y = vec.y;
	}
	
	@Override
	public final double getD(int index)
	{
		switch (index)
		{
		case 0: return x;
		case 1: return y;
		default: throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	public final void set(Vector2d v) {
		this.x = v.x;
		this.y = v.y;
	}
	
	public final void set(final float data[], final int index) {
		this.x = data[index];
		this.y = data[index + 1];
	}

    @Override
    public void setElem(int index, double value)
    {
    	switch (index)
    	{
	    	case 0: this.x = value; return;
	    	case 1: this.y = value; return;
	    	default: throw new ArrayIndexOutOfBoundsException(index);
    	}
    }

	public void multiply(double d) {
		x *= d;
		y *= d;
	}
	
	public void set(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void add(Vector2d v)
	{
		x += v.x;
		y += v.y;
	}
	
    @Override
	public final String toString()
    {
    	return toString(new StringBuilder()).toString();    	
    }
    
    public final StringBuilder toString(StringBuilder strB)
    {
    	return strB.append('(').append(x).append(',').append(y).append(')');
    }
    
    public final int size()
    {
    	return 2;
    }

	public void add(double x, double y) {
		this.x += x;
		this.y += y;
	}

	public final void write(final double[] data, final int i) {
		data[i] = x;
		data[i + 1] = y;
	}
	
	public final double dot()
	{
		return x * x + y * y;
	}

	@Override
	public void add(double[] data, int index) {
		x += data[index];
		y += data[index + 1];
	}

	public void addTo(double[] data, int index) {
		data[index] += x;
		data[index + 1] += y;
	}

	public final void write(final float[] data, final int i) {
		data[i] = (float)x;
		data[i + 1] = (float)y;
	}
}
