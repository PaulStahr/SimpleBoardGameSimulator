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

import geometry.Vector3f;
import util.data.DoubleList;

/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public final class Vector3d implements Vectord
{
    public double x, y, z;
    
    /**
     * erzeugt einen neuen Vektor
     */
    public Vector3d(){}

    /**
     * erzeugt einen neuen Vektor
     * @param x L\u00E4nge des Vektors in x-Richtung
     * @param y L\u00E4nge des Vektors in y-Richtung
     * @param z L\u00E4nge des Vektors in z-Tichtung
     */
    public Vector3d(final double x,final double y,final double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
    public double getD(int index)
    {
    	switch (index)
    	{
	    	case 0: return x;
	    	case 1: return y;
	    	case 2: return z;
	    	default: throw new ArrayIndexOutOfBoundsException(index);
    	}
    }
    
    @Override
    public void setElem(int index, double value)
    {
    	switch (index)
    	{
	    	case 0: this.x = value; return;
	    	case 1: this.y = value; return;
	    	case 2: this.z = value; return;
	    	default: throw new ArrayIndexOutOfBoundsException(index);
    	}
    }

    /**
     * erzeugt einen neuen Vektor
     * @param vektor der Vektor dessen eigenschaften \u00FCbernommen werden
     */
    public Vector3d(final Vector3f vector){
       set(vector);
    }
        
    /**
     * erzeugt einen neuen Vektor
     * @param vektor der Vektor dessen eigenschaften \u00FCbernommen werden
     */
    public Vector3d(final Vector3d vector){
       set(vector);
    }
        
    @Override
    public final boolean equals(Object other)
    {
    	if (other instanceof Vector3d)
    	{
    		Vector3d vec = (Vector3d)other;
    		return vec.x == x && vec.y == y && vec.z == z;
    	}
    	return false;
    }
    
    /**
     * Subtrahiert den Vektor
     * @param vektor der Subtrahiert
     */
    public void sub(final Vector3f vektor){
        x -= vektor.x;
        y -= vektor.y;
        z -= vektor.z;
    }

   
    public final boolean containsNaN()
    {
    	return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }
    
/**
 * Subtrahiert den Vektor
 * @param vektor der Subtrahiert
 */
public void sub(final Vector3d vektor){
    x -= vektor.x;
    y -= vektor.y;
    z -= vektor.z;
}
    /**
     * erzeugt eine Normale
     */
    public static final Vector3f getNormal(final Vector3f v0, final Vector3f v1, final Vector3f v2){
        Vector3f vektor = new Vector3f();
        vektor.calcNormal (v0, v1, v2);
        return vektor;
    }

    /**
     * erzeugt eine Normale
     */
    public final void calcNormal(final Vector3f v0, final Vector3f v1, final Vector3f v2){
        final double ax = v0.x-v1.x, ay = v0.y-v1.y, az = v0.z-v1.z; 
        final double bx = v1.x-v2.x, by = v1.y-v2.y, bz = v1.z-v2.z;
        x = ay*bz - az*by;
        y = az*bx - ax*bz;
        z = ax*by - ay*bx;
    }

    /**
     * Setzt die L\u00E4nge des Vektors auf 1
     */
    public final void normalize(){
        multiply(1/getLength());
    }

    /**
     * Multipliziert die L\u00E4nge des Vektors
     * @param mult Multiplikator
     */
    public final void multiply(final double mult){
        x*=mult;
        y*=mult;
        z*=mult;
    }
    
    public final void set(float data[], int pos)
    {
    	x = data[pos];
    	y = data[pos + 1];
    	z = data[pos + 2];
    }
    
    public final void set(Object data, int pos)
    {
    	if (data instanceof float[])
    	{
    		set((float[])data, pos);
    	}
    	else if (data instanceof double[])
    	{
    		set((double[])data, pos);
    	}
    	else if (data instanceof DoubleList)
    	{
    		set((DoubleList)data, pos);
    	}
    	else
    	{
    		throw new IllegalArgumentException(data.getClass().toString());
    	}
    }
    
    public final void write(Object data, int pos)
    {
    	if (data instanceof float[])
    	{
    		write((float[])data, pos);
    	}
    	else if (data instanceof double[])
    	{
    		write((double[])data, pos);
    	}
    	else if (data instanceof DoubleList)
    	{
    		write((DoubleList)data, pos);
    	}
    	else
    	{
    		throw new IllegalArgumentException(data.getClass().toString());
    	}
    }
    
    public final void set(double data[], int pos)
    {
    	x = data[pos];
    	y = data[pos + 1];
    	z = data[pos + 2];
    }

    public final void write(final double data[], final int pos)
    {
    	data[pos] = x;
    	data[pos + 1] = y;
    	data[pos + 2] = z;
    }

    public final void write(float data[], int pos)
    {
    	data[pos] = (float)x;
    	data[pos + 1] = (float)y;
    	data[pos + 2] = (float)z;
    }

    /**
     * Setzt den Vektor auf eine Bestimmte L\u00E4nge
     * @param length die neue L\u00E4nge des Vektors
     */
    public final void setLength(double length){
        multiply(length / getLength());
    }

    /**
     * Berechnet die aktuelle L\u00E4nge des Vektors
     * @return L\u00E4nge des Vektors
     */
    public final double getLength(){
        return Math.sqrt(dot());
    }
    
    public final double dot() {
    	return x * x + y * y + z * z;
    }

    /**
     * Dreht einen Vektor um eine Drehung
     */
    public final void rotateXYZEuler(Rotation3 r){
    	rotateRadiansX(r.getXRadians());
    	rotateRadiansY(r.getYRadians());
    	rotateRadiansZ(r.getZRadians());
    }

    /**
     * Dreht einen Vektor um eine Drehung
     */
    public final void rotateReverseXYZEuler(Rotation3 r){
    	rotateRadiansZ(-r.getZRadians());
    	rotateRadiansY(-r.getYRadians());
    	rotateRadiansX(-r.getXRadians());
    }
    
    /**
     * Rotiert den Vektor.
     * @param x die Rotation um die x-Achse
     */
    public final void rotateRadiansX(double x){
        double sin = Math.sin(x);
        double cos = Math.cos(x);
        double tmp = cos*y-sin*z;
        z = sin*y+cos*z;
        y=tmp;
    }


    /**
     * Rotiert den Vektor.
     * @param y die Rotation um die y-Achse
     */
    public final void rotateRadiansY(double y){
    	double sin = Math.sin(y);
        double cos = Math.cos(y);
        double tmp = cos*x+sin*z;
        z = cos*z-sin*x;
        x = tmp;
    }


    /**
     * Rotiert den Vektor.
     * @param z die Rotation um die z-Achse
     */
    public final void rotateRadiansZ(double z){
        double sin = Math.sin(z);
        double cos = Math.cos(z);
        double tmp = cos*x-sin*y;
        y = sin*x+cos*y;
        x = tmp;
    }
    
    public final double dot(Vector3d other)
    {
    	return x * other.x + y * other.y + z * other.z;
    }

    public final double dot(final double x, final double y, final double z)
    {
    	return this.x * x + this.y * y + this.z * z;
    }

    /**
     * Setzt den Vektor auf die bestimmte Werte.
     * @param x x-Wert des Vektors
     * @param y y-Wert des Vektors
     * @param z z-Wert des Vektors
     */
    public final void set(final double x, final double y, final double z){
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public final void set(DoubleList list, int index)
    {
    	this.x = list.getD(index);
    	this.y = list.getD(index + 1);
    	this.z = list.getD(index + 2);
    }
    
    public final void write(DoubleList list, int index)
    {
    	list.setElem(index, this.x);
    	list.setElem(index + 1, this.y);
    	list.setElem(index + 2, this.z);
    }
    
    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void set(final Vector3f vektor){
        x = vektor.x;
        y = vektor.y;
        z = vektor.z;
    }

    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void set(final Vector2d vektor){
        x = vektor.x;
        y = vektor.y;
    }

    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void set(final Vector3d vektor){
        x = vektor.x;
        y = vektor.y;
        z = vektor.z;
    }
    public final void set(final Vector3d v, double s){
        x = v.x * s;
        y = v.y * s;
        z = v.z * s;
    }

    public final void set(final Vector3d v0, final Vector3d v1, double s1){
        x = v0.x + v1.x * s1;
        y = v0.y + v1.y * s1;
        z = v0.z + v1.z * s1;
    }

    public final void set(final double x, final double y, final double z, final Vector3d v1, final double s1){
        this.x = x + v1.x * s1;
        this.y = y + v1.y * s1;
        this.z = z + v1.z * s1;
    }

    public final void set(final Vector3d v0, double s0, final Vector3d v1, double s1){
        x = v0.x * s0 + v1.x * s1;
        y = v0.y * s0 + v1.y * s1;
        z = v0.z * s0 + v1.z * s1;
    }

    public final void set(final Vector3d v0, final Vector3d v1, double s1, final Vector3d v2, double s2){
        x = v0.x + v1.x * s1 + v2.x * s2;
        y = v0.y + v1.y * s1 + v2.y * s2;
        z = v0.z + v1.z * s1 + v2.z * s2;
    }

    public final void set(final double x, final double y, final double z, final Vector3d v1, final double s1, final Vector3d v2, final double s2){
        this.x = x + v1.x * s1 + v2.x * s2;
        this.y = y + v1.y * s1 + v2.y * s2;
        this.z = z + v1.z * s1 + v2.z * s2;
    }

    public final void set(final Vector3d v0, final Vector3d v1, final Vector3d v2, double s2){
        x = v0.x - v1.x + v2.x * s2;
        y = v0.y - v1.y + v2.y * s2;
        z = v0.z - v1.z + v2.z * s2;
    }

    public final void set(final Vector3d v0, double s0, final Vector3d v1, double s1, final Vector3d v2, double s2){
        x = v0.x * s0 + v1.x * s1 + v2.x * s2;
        y = v0.y * s0 + v1.y * s1 + v2.y * s2;
        z = v0.z * s0 + v1.z * s1 + v2.z * s2;
    }

    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void setDiff(final Vector3d v0, final Vector3d v1){
        x = v0.x - v1.x;
        y = v0.y - v1.y;
        z = v0.z - v1.z;
    }    
    
    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void setAdd(final Vector3d v0, final Vector3d v1){
        x = v0.x + v1.x;
        y = v0.y + v1.y;
        z = v0.z + v1.z;
    }

    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param vektor der Vektor der addiert werden soll
     */
    public final void add(final Vector2d vektor){
        x += vektor.x;
        y += vektor.y;
    }
    
    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param vektor der Vektor der addiert werden soll
     */
    public final void add(final Vector3f vektor){
        x += vektor.x;
        y += vektor.y;
        z += vektor.z;
    }
    
    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param vektor der Vektor der addiert werden soll
     */
    public final void add(final Vector3d vektor){
        x += vektor.x;
        y += vektor.y;
        z += vektor.z;
    }
    
    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param vektor der Vektor der addiert werden soll
     * @param scalar der Skalar mit dem der vektor vorher multipliziert wird
     */
    public final void add(final Vector3d vektor, double scalar){
        x += vektor.x * scalar;
        y += vektor.y * scalar;
        z += vektor.z * scalar;
    }
    
    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param vektor der Vektor der addiert werden soll
     * @param scalar der Skalar mit dem der vektor vorher multipliziert wird
     */
    public final void add(final Vector3d vektor, double scalar, Vector3d vector2, double scalar2){
        x += vektor.x * scalar + vector2.x * scalar2;
        y += vektor.y * scalar + vector2.x * scalar2;
        z += vektor.z * scalar + vector2.x * scalar2;
    }

    /**
     * Addiert zum Vektor bestimmte Werte.
     * @param x x-Wert der Addiert wird
     * @param y y-Wert der Addiert wird
     * @param z z-Wert der Addiert wird
     */
    public final void add(final double x, final double y, final double z){
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    public final void cross(Vector3d v0, Vector3d v1)
    {
    	x = v0.y * v1.z - v0.z * v1.y;
    	y = v0.z * v1.x - v0.x * v1.z;
    	z = v0.x * v1.y - v0.y * v0.x;
    }

	public final void reflect(Vector3d other) {
		add(other, -2 * dot(other)/other.dot());
	}
	
	public final void invert(Vector3d other)
	{
		x = -other.x;
		y = -other.y;
		z = -other.z;
	}
	
	public final void invert()
	{
		x = -x;
		y = -y;
		z = -z;
	}
	
   	public final double distanceQ(Vector3d other)
	{
		double xDiff = x - other.x;
		double yDiff = y - other.y;
		double zDiff = z - other.z;
		return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
	}
	
	public final double distanceQ(Vector3d other, double scale)
	{
		double xDiff = x - other.x * scale;
		double yDiff = y - other.y * scale;
		double zDiff = z - other.z * scale;
		return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
	}
	
	public final double distanceQ(final double scale, final double x, final double y, final double z)
	{
		double xDiff = scale * this.x - x;
		double yDiff = scale * this.y - y;
		double zDiff = scale * this.z - z;
		return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
	}
	

	public final double distanceQ(float[] data, int index) {
		double xDiff = x - data[index];
		double yDiff = y - data[index + 1];
		double zDiff = z - data[index + 2];
		return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
	}
	
	public final double distanceQ(double[] data, int index) {
		double xDiff = x - data[index];
		double yDiff = y - data[index + 1];
		double zDiff = z - data[index + 2];
		return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
	}
	
    public final String toString()
    {
    	return toString(new StringBuilder()).toString();    	
    }
    
    public final StringBuilder toString(StringBuilder strB)
    {
    	return strB.append('(').append(x).append(',').append(y).append(',').append(z).append(')');
    }

	@Override
	public int size() {
		return 3;
	}

	public void add(float[] data, int index) {
		x += data[index];
		y += data[index + 1];
		z += data[index + 2];
	}
	

	public void add(float[] data, int index, float scalar) {
		x += data[index] 	 * scalar;
		y += data[index + 1] * scalar;
		z += data[index + 2] * scalar;
	}

	public void add(double[] data, int index) {
		x += data[index];
		y += data[index + 1];
		z += data[index + 2];
	}

	public final double dot(double[] data, int i) {
		return data[i] * x + data[i + 1] * y + data[i + 2] * z;
	}

	public final void setAdd(final Vector3d vec, final double x, final double y, final double z) {
		this.x = vec.x + x;
		this.y = vec.y + y;
		this.z = vec.z + z;
	}

	public final double dot(Vector3d v0, Vector3d v1) {
		return x * (v0.x - v1.x) + y * (v0.y - v1.y) + z * (v0.z - v1.z);
	}

	public final double dot(float[] data, int i) {
		return data[i] * x + data[i + 1] * y + data[i + 2] * z;
	}

}
