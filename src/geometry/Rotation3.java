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

import util.data.DoubleList;

/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public final class Rotation3 implements Vectorf, Vectori, Vectord
{
	public static final float MULT_INT_TO_DEG = -(float)(180./Integer.MIN_VALUE);
	public static final float MULT_INT_TO_RAD = -(float)(Math.PI/Integer.MIN_VALUE);
	public static final double DMULT_INT_TO_RAD = -(double)(Math.PI/Integer.MIN_VALUE);
	public static final float MULT_DEG_TO_INT = -(float)(Integer.MIN_VALUE/180.);
	public static final float MULT_RAD_TO_INT = -(float)(Integer.MIN_VALUE/Math.PI);
	public static final double DMULT_RAD_TO_INT = -(double)(Integer.MIN_VALUE/Math.PI);
    private int x,y,z;

    public Rotation3(){}

    public Rotation3(Rotation3 rotation){
        set(rotation);
    }

    public Rotation3 (final float x, final float y, final float z, boolean deg){
    	if (deg)
    		setDegrees(x, y, z);
    	else
    		setRadians(x, y, z);
    }

    public final float getXDegrees(){
        return MULT_INT_TO_DEG*x;
    }

    public final float getXRadians(){
        return MULT_INT_TO_RAD*x;
    }

    public final float getYDegrees(){
        return MULT_INT_TO_DEG*y;
    }

    public final float getYRadians(){
        return MULT_INT_TO_RAD*y;
    }

    public final float getZDegrees(){
        return MULT_INT_TO_DEG*z;
    }

    public final float getZRadians(){
        return MULT_INT_TO_RAD*z;
    }

    public final void addDegrees(final float x,final float y,final float z){
    	this.x += (int)(MULT_DEG_TO_INT*x);
    	this.y += (int)(MULT_DEG_TO_INT*y);
    	this.z += (int)(MULT_DEG_TO_INT*z);
    }

    public final void addXDegrees(final float x){
    	this.x += (int)(MULT_DEG_TO_INT*x);
    }

    public final void addYDegrees(final float y){
    	this.y += (int)(MULT_DEG_TO_INT*y);
    }

    public final void addZDegrees(final float z){
    	this.z += (int)(MULT_DEG_TO_INT*z);
    }

    public final void addXInt(final int x){
    	this.x += x;
    }
    
    public final void addYInt(final int y){
    	this.y += y;
    }
    
    public final void addZInt(final int z){
    	this.z += z;
    }
    
    public final void setDegreesX (final float x){
    	this.x = (int)(MULT_DEG_TO_INT*x);
    }

    public final void setDegreesY (final float y){
    	this.y = (int)(MULT_DEG_TO_INT*y);
    }

    public final void setDegreesZ (final float z){
    	this.z = (int)(MULT_DEG_TO_INT*z);
    }

    public final void setDegrees(final float x, final float y, final float z){
    	this.x = (int)(MULT_DEG_TO_INT*x);
    	this.y = (int)(MULT_DEG_TO_INT*y);
    	this.z = (int)(MULT_DEG_TO_INT*z);
    }

    public final void setRadians(final float x, final float y, final float z){
    	this.x = (int)(MULT_RAD_TO_INT*x);
    	this.y = (int)(MULT_RAD_TO_INT*y);
    	this.z = (int)(MULT_RAD_TO_INT*z);
    }
    
    public final void setRadians(DoubleList data, int index)
    {
    	this.x = (int)(MULT_RAD_TO_INT * data.getD(index));
    	this.y = (int)(MULT_RAD_TO_INT * data.getD(index + 1));
    	this.z = (int)(MULT_RAD_TO_INT * data.getD(index + 2));
    }
    
    public final void set(final Rotation3 r){
        x = r.x;
        y = r.y;
        z = r.z;
    }
    
    public final String toString()
    {
    	return toString(new StringBuilder()).toString();    	
    }
    
    public final StringBuilder toString(StringBuilder strB){
    	return strB.append('(').append(getXRadians())
    			   .append(',').append(getYRadians())
    			   .append(',').append(getZRadians()).append(')');
    }

	@Override
	public int size() {
		return 3;
	}

	@Override
	public void setElem(int index, float value) {
		int ivalue = (int)(MULT_RAD_TO_INT * value);
		switch(index)
		{
		case 0: this.x = ivalue;return;
		case 1: this.y = ivalue;return;
		case 2: this.z = ivalue;return;
		}
		throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public void setElem(int index, int value) {
		switch(index)
		{
		case 0: this.x = value;return;
		case 1: this.y = value;return;
		case 2: this.z = value;return;
		}
		throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public void setElem(int index, double value) {
		int ivalue = (int)(DMULT_RAD_TO_INT * value);
		switch(index)
		{
		case 0: this.x = ivalue;return;
		case 1: this.y = ivalue;return;
		case 2: this.z = ivalue;return;
		}
		throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public int getI(int index) {
		switch (index)
		{
		case 0: return this.x;
		case 1: return this.y;
		case 2: return this.z;
		default: throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	

	@Override
	public double getD(int index) {
		switch (index)
		{
		case 0: return this.x * DMULT_INT_TO_RAD;
		case 1: return this.y * DMULT_INT_TO_RAD;
		case 2: return this.z * DMULT_INT_TO_RAD;
		default: throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	@Override
	public final boolean equals(Object other)
	{
		if (other instanceof Rotation3)
		{
			Rotation3 rot = (Rotation3)other;
			return rot.x == x && rot.y == y && rot.z == z;
		}
		return false;
	}

	@Override
	public final double dot() {
		double xd = x * x, yd = y * y, zd = z * z;
		return xd + yd * yd + zd * zd;
	}

	@Override
	public void add(double[] data, int index) {
		this.x += (int)(MULT_RAD_TO_INT*data[index]);
    	this.y += (int)(MULT_RAD_TO_INT*data[index + 1]);
    	this.z += (int)(MULT_RAD_TO_INT*data[index + 2]);
	}
}
