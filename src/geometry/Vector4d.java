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

/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public final class Vector4d implements Vectord
{
    public double x, y, z, w;

    /**
     * erzeugt einen neuen Vektor
     */
    public Vector4d(){}

    /**
     * erzeugt einen neuen Vektor
     * @param x L\u00E4nge des Vektors in x-Richtung
     * @param y L\u00E4nge des Vektors in y-Richtung
     * @param z L\u00E4nge des Vektors in z-Tichtung
     */
    public Vector4d(final double x,final double y,final double z,final double w){
        this.x=x;
        this.y=y;
        this.z=z;
        this.w=w;
    }

    @Override
    public void setElem(int index, double value)
    {
    	switch (index)
    	{
	    	case 0: this.x = value; return;
	    	case 1: this.y = value; return;
	    	case 2: this.z = value; return;
	    	case 3: this.w = value; return;
	    	default: throw new ArrayIndexOutOfBoundsException(index);
    	}
    }

    @Override
    public final double getD(int index)
    {
    	switch (index)
    	{
    	case 0: return x;
    	case 1: return y;
    	case 2: return z;
    	case 3: return w;
    	default: throw new ArrayIndexOutOfBoundsException(index);
    	}
    }
    
    /**
     * erzeugt einen neuen Vektor
     * @param vektor der Vektor dessen eigenschaften \u00FCbernommen werden
     */
    public Vector4d(final Vector4d vector){
        set(vector);
    }
        
    /**
     * Subtrahiert den Vektor
     * @param vektor der Subtrahiert
     */
    public void sub(final Vector4d vektor){
        x -= vektor.x;
        y -= vektor.y;
        z -= vektor.z;
        w -= vektor.w;
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
        final double len = 1/getLength();
        x *= len;
        y *= len;
        z *= len;
        w *= len;
    }

    /**
     * Multipliziert die L\u00E4nge des Vektors
     * @param multi Multiplikator
     */
    public final void multiply(final double multi){
        x*=multi;
        y*=multi;
        z*=multi;
        w*=multi;
    }

    /**
     * Setzt den Vektor auf eine Bestimmte L\u00E4nge
     * @param length die neue L\u00E4nge des Vektors
     */
    public final void setLength(double length){
        length /= getLength();
        x *= length;
        y *= length;
        z *= length;
    }

    /**
     * Berechnet die aktuelle L\u00E4nge des Vektors
     * @return L\u00E4nge des Vektors
     */
    public final double getLength(){
        return Math.sqrt(dot());
    }
    
    /**
     * Rotiert den Vektor.
     * Die Rotation erfolgt in der Rihenfolge XYZ
     */
    public final void rotateXYZEulerDegrees (final double x, final double y, final double z){
        rotateRadiansX(Math.toRadians(x));
        rotateRadiansY(Math.toRadians(y));
        rotateRadiansZ(Math.toRadians(z));
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

    /**
     * Setzt den Vektor auf die bestimmte Werte.
     * @param x x-Wert des Vektors
     * @param y y-Wert des Vektors
     * @param z z-Wert des Vektors
     */
    public final void set(final double x, final double y, final double z, final double w){
        this.x=x;
        this.y=y;
        this.z=z;
        this.w=w;
    }

    /**
     * Setzt den Vektor auf bestimmte Werte.
     * @param vektor der Vektor auf den die Werte gesetzt werden sollen
     */
    public final void set(final Vector4d vektor){
        x = vektor.x;
        y = vektor.y;
        z = vektor.z;
        w = vektor.w;
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
     * @param x x-Wert der Addiert wird
     * @param y y-Wert der Addiert wird
     * @param z z-Wert der Addiert wird
     */
    public final void add(final double x, final double y, final double z){
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    @Override
    public final int size()
    {
    	return 4;
    }

    @Override
	public final double dot() {
		return x * x + y * y + z * z + w * w;
	}

	@Override
	public void add(double[] data, int index) {
		x += data[index];
		y += data[index + 1];
		z += data[index + 2];
		w += data[index + 3];
	}
}
