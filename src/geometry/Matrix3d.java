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

import maths.algorithm.Calculate;
import util.data.DoubleArrayList;
import util.data.DoubleList;

public class Matrix3d implements Matrixd, DoubleList{
	public double m00, m01, m02, m10, m11, m12, m20, m21, m22;

	public Matrix3d(){this(1,0,0,0,1,0,0,0,1);}
	
    public Matrix3d(Matrix3d m){
        this.m00 = m.m00;this.m01 = m.m01;this.m02 = m.m02;
        this.m10 = m.m10;this.m11 = m.m11;this.m12 = m.m12;
        this.m20 = m.m20;this.m21 = m.m21;this.m22 = m.m22;
    }

	public Matrix3d(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;
	}

    public final void setColMajor(final double mat[]){
        m00 = mat[0];  m01 = mat[1];  m02 = mat[2];
        m10 = mat[3];  m11 = mat[4];  m12 = mat[5];
        m20 = mat[6];  m21 = mat[7];  m22 = mat[8];
    }

    public final void setColMajor(final double mat[], int pos, int stride){
        m00 = mat[pos]; m01 = mat[pos+1]; m02 = mat[pos+2];pos += stride;
        m10 = mat[pos]; m11 = mat[pos+1]; m12 = mat[pos+2];pos += stride;
        m20 = mat[pos]; m21 = mat[pos+1]; m22 = mat[pos+2];
    }
	
    public final void getColMajor(final double mat[], int begin, int stride){
        mat[begin] = m00; mat[begin+1] = m01; mat[begin+2] = m02;begin += stride;
        mat[begin] = m10; mat[begin+1] = m11; mat[begin+2] = m12;begin += stride;
        mat[begin] = m20; mat[begin+1] = m21; mat[begin+2] = m22;
    }
    
	public void setIdentity()
	{
		this.m00 = this.m11 = this.m22 = 1;
		this.m01 = this.m02 = this.m10 = this.m12 = this.m20 = this.m21 = 0;
	}
	
	public final void transform(Vector3f v){
		final float x = v.x, y = v.y, z = v.z;
		v.x = (float)(m00 * x + m10 * y + m20 * z);
		v.y = (float)(m01 * x + m11 * y + m21 * z);
		v.z = (float)(m02 * x + m12 * y + m22 * z);
	}
	
	public final double getNewX(double x, double y, double z){return m00 * x + m10 * y + m20 * z;}
	public final double getNewY(double x, double y, double z){return m01 * x + m11 * y + m21 * z;}
	public final double getNewZ(double x, double y, double z){return m02 * x + m12 * y + m22 * z;}
	
	@Override
	public final double get(int x, int y) {
		switch(x){
			case 0:switch(y){case 0:return m00;case 1:return m01;case 2:return m02;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:return m10;case 1:return m11;case 2:return m12;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:return m20;case 1:return m21;case 2:return m22;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}
	
   @Override
    public final int size() {return 9;}

	@Override
	public final int cols() {return 3;}
	
	@Override
	public final int rows() {return 3;}

	
	@Override
	public final void setRowMajor(final double mat[][]){
		m00 = mat[0][0]; m01 = mat[1][0]; m02 = mat[2][0];
		m10 = mat[0][1]; m11 = mat[1][1]; m12 = mat[2][1];
		m20 = mat[0][2]; m21 = mat[1][2]; m22 = mat[2][2];
	}

	
	@Override
	public final void set(int x, int y, double value){
		switch(x){
			case 0:switch(y){case 0:m00 = value;return;case 1:m01 = value;return;case 2:m02 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:m10 = value;return;case 1:m11 = value;return;case 2:m12 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:m20 = value;return;case 1:m21 = value;return;case 2:m22 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}

	public final void set(Matrix3d o) {
		this.m00 = o.m00;this.m01 = o.m01;this.m02 = o.m02;
		this.m10 = o.m10;this.m11 = o.m11;this.m12 = o.m12;
		this.m20 = o.m20;this.m21 = o.m21;this.m22 = o.m22;
	}

	
	@Override
	public void set(Matrixd o) {
		if (o instanceof Matrix3d)
		{
			set((Matrix3d) o);
		}
		else
		{
			int cols = Math.min(o.cols(), cols()), rows = Math.min(o.rows(), rows());
			for (int i = 0; i < rows; ++i)
			{
				for (int j = 0; j < cols; ++j)
				{
					set(i, j, o.get(i, j));
				}
			}
		}
	}
	
	public final void transformAffine(Vector2d v){
		final double x = v.x, y = v.y;
		v.x = m00 * x + m10 * y + m20;
		v.y = m01 * x + m11 * y + m21;
	}

	public final double ldotAffineX(double x, double y) {return m00 * x + m10 * y + m20;}
	public final double ldotAffineY(double x, double y) {return m01 * x + m11 * y + m21;}

	public void set(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22){
		this.m00 = m00;this.m01 = m01;this.m02 = m02;
		this.m10 = m10;this.m11 = m11;this.m12 = m12;
		this.m20 = m20;this.m21 = m21;this.m22 = m22;
	}

	public void setRowMajor(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22){
		this.m00 = m00;this.m01 = m01;this.m02 = m02;
		this.m10 = m10;this.m11 = m11;this.m12 = m12;
		this.m20 = m20;this.m21 = m21;this.m22 = m22;
	}

    public final void preTranslate(double x, double y)
    {
        m02 += x * m00 + y * m01;
        m12 += x * m10 + y * m11;
        m22 += x * m20 + y * m21;
    }
    
    public final void postTranslate(double x, double y){
        m00 += m20 * x; m10 += m20 * y;
        m01 += m21 * x; m11 += m21 * y;
        m02 += m22 * x; m12 += m22 * y;
    }

	public final void scale(double d) {
		this.m00 *= d;this.m01 *= d; this.m02 *= d;
		this.m10 *= d;this.m11 *= d; this.m12 *= d;
		this.m20 *= d;this.m21 *= d; this.m22 *= d;
	}

	public final void affineScale(double s)
    {
        m00 *= s; m10 *= s;
        m01 *= s; m11 *= s;
    }

    public final void preScale(double x, double y) {
        m00 *= x; m01 *= y;
        m10 *= x; m11 *= y;
        m20 *= x; m21 *= y;
    }

    public final void postScale(double x, double y)
    {
        m00 *= x; m01 *= x; m02 *= x;
        m10 *= y; m11 *= y; m12 *= y;
    }

    public final void rdot(Vector3d vector){
        final double x = vector.x, y = vector.y, z = vector.z;
        vector.x = m00 * x + m10 * y + m20 * z;
        vector.y = m01 * x + m11 * y + m21 * z;
        vector.z = m02 * x + m12 * y + m22 * z;
    }

    public final void ldot(Vector3d vector){
        final double x = vector.x, y = vector.y, z = vector.z;
        vector.x = m00 * x + m01 * y + m02 * z;
        vector.y = m10 * x + m11 * y + m12 * z;
        vector.z = m20 * x + m21 * y + m22 * z;
    }

    public final void ldotAffine(Vector2d vector)                       {ldotAffine(vector.x, vector.y, vector);} 
    public final void rdotAffine(Vector2d vector)                       {rdotAffine(vector.x, vector.y, vector);}
    public void rdotAffine(float[] position, int i, Vector2d result)    {rdotAffine(position[i], position[i + 1], result);}
    public void rdotAffine(double[] position, int i, Vector2d result)   {rdotAffine(position[i], position[i + 1], result);}
    public void rdotAffine(DoubleList position, int i, Vector2d result) {rdotAffine(position.getD(i), position.getD(i + 1), result);}
    public final void ldot(Vector2d vector)                             {ldot(vector.x, vector.y, vector);}
    
    public final void rdotAffine(double x, double y, Vector2d vector){
        vector.x = m00 * x + m01 * y + m02;
        vector.y = m10 * x + m11 * y + m12;
    }
    
    public final void ldot(double x, double y, Vector2d vector)
    {
        vector.x = m00 * x + m10 * y;
        vector.y = m01 * x + m11 * y;
    }
    
    public final void rdot(double x, double y, Vector2d result)
    {       
        result.x = m00 * x + m01 * y;
        result.y = m10 * x + m11 * y;
    }
    
    public void rdot(double x, double y, float[] out, int i) {
        out[i++] = (float)(m00 * x + m01 * y);
        out[i++] = (float)(m10 * x + m11 * y);
    }
    
    public void rdot(Vector2d in, float result[], int i)    {rdot(in.x, in.y, result, i);}
    public void rdot(Vector2d in, double result[], int i)   {rdot(in.x, in.y, result, i);}
    public void rdot(Vector2d in, DoubleList result, int i) {rdot(in.x, in.y, result, i);}
    public void rdot(Vector2d in, Object result, int i)     {rdot(in.x, in.y, result, i);}
    
    public void rdot(double x, double y, double[] out, int i) {
        out[i++] = m00 * x + m01 * y;
        out[i++] = m10 * x + m11 * y;
    }
    
    public void rdot(double x, double y, DoubleList out, int i) {
        out.setElem(i++, m00 * x + m01 * y);
        out.setElem(i++, m10 * x + m11 * y);
    }
    
    public final void rdot(Vector2d vector){rdot(vector.x, vector.y, vector);}
    
    public final void rdot(double x, double y, Object out, int pos)
    {
        if (out instanceof float[])        {rdot(x, y, out, pos);}
        else if (out instanceof double[])  {rdot(x, y, out, pos);}
        else if (out instanceof DoubleList){rdot(x, y, out, pos);}
        else                               {throw new IllegalArgumentException(out.getClass().toString());}
    }
    
    public final void rdot(Object in, int pos, Vector2d out)
    {
        if (in instanceof float[])         {rdot((float[])in, pos, out);}
        else if (in instanceof double[])   {rdot((double[])in, pos, out);}
        else if (in instanceof DoubleList) {rdot((DoubleList)in, pos, out);}
        else                               {throw new IllegalArgumentException(in.getClass().toString());}
    }
    
    public final void rdot(float in[], int index, Vector2d vector)     {rdot(in[index], in[index + 1], vector);}
    public final void rdot(double in[], int index, Vector2d vector)    {rdot(in[index], in[index + 1], vector);}
    public final void rdot(DoubleList in, int index, Vector2d vector)  {rdot(in.getD(index), in.getD(index + 1), vector);}
    
    public final void rdotAffine(Object in, int pos, Vector2d out)
    {
        if      (in instanceof float[])     {rdotAffine(in, pos, out);}
        else if (in instanceof double[])    {rdotAffine(in, pos, out);}
        else if (in instanceof DoubleList)  {rdotAffine(in, pos, out);}
        else{throw new IllegalArgumentException(in.getClass().toString());}
    }
    
    public final void rdotAffine(Vector2d in, Object out, int pos)
    {
        if      (out instanceof float[])            {rdotAffine(in, (float[])out, pos);}
        else if (out instanceof double[])   {rdotAffine(in, (double[])out, pos);}
        else if (out instanceof DoubleList) {rdotAffine(in, (DoubleList)out, pos);}
        else    {throw new IllegalArgumentException(in.getClass().toString());}
    }

    public final void ldotAffine(Vector2d vector, Vector2d out)             {ldotAffine(vector.x, vector.y, out);}
    public final void rdotAffine(Vector2d vector, Vector2d out)             {rdotAffine(vector.x, vector.y, out);}
    public final void rdotAffine(Vector2d vector, float out[], int index)   {rdotAffine(vector.x, vector.y, out, index);}
    public final void rdotAffine(Vector2d vector, double out[], int index)  {rdotAffine(vector.x, vector.y, out, index);}
    public final void rdotAffine(Vector2d vector, DoubleList out, int index){rdotAffine(vector.x, vector.y, out, index);}
    public final void ldotAffine(Vector2d vector, float out[], int index)   {ldotAffine(vector.x, vector.y, out, index);}
    
    public final void ldotAffine(double x, double y, Vector2d out){
        out.x = m00 * x + m10 * y + m20;
        out.y = m01 * x + m11 * y + m21;
    }
        
    public final void ldotAffine(double x, double y, float out[], int index){
        out[index]   = (float)(m00 * x + m10 * y + m20);
        out[++index] = (float)(m01 * x + m11 * y + m21);
    }
    
    public final void rdotAffine(double x, double y, float out[], int index){
        out[index]   = (float)(m00 * x + m01 * y + m02);
        out[++index] = (float)(m10 * x + m11 * y + m12);
    }
    
    public final void rdotAffine(double x, double y, double out[], int index){
        out[index]   = m00 * x + m01 * y + m02;
        out[++index] = m10 * x + m11 * y + m12;
    }
    
    public final void rdotAffine(double x, double y, DoubleList out, int index){
        out.setElem(index++,m00 * x + m01 * y + m02);
        out.setElem(index++,m10 * x + m11 * y + m12);
    }
    
    public final void ldotAffine(DoubleArrayList in, int inIndex, float[] out, int outIndex){ldotAffine(in.getD(inIndex), in.getD(++inIndex), out, outIndex);}
    public final void rdotAffine(DoubleArrayList in, int inIndex, float[] out, int outIndex){rdotAffine(in.getD(inIndex), in.getD(++inIndex), out, outIndex);}
    public final void rdotAffine(float in[], int inIndex, float[] out, int outIndex)        {rdotAffine(in[inIndex], in[inIndex + 1], out, outIndex);}
    public final void rdotAffine(double in[], int inIndex, float[] out, int outIndex)       {rdotAffine(in[inIndex], in[inIndex + 1], out, outIndex);}
    
    public final void ldot(double x, double y, double z, float out[], int index){
        out[index]   = (float)(m00 * x + m10 * y + m20 * z);
        out[++index] = (float)(m01 * x + m11 * y + m21 * z);
        out[++index] = (float)(m02 * x + m12 * y + m22 * z);
    }
    
    public final void rdot(double x, double y, double z, float out[], int index){
        out[index]   = (float)(m00 * x + m01 * y + m02 * z);
        out[++index] = (float)(m10 * x + m11 * y + m12 * z);
        out[++index] = (float)(m20 * x + m21 * y + m22 * z);
    }
	
	public final void rotateZ(double value)
	{
		double sin = Math.sin(value), cos = Math.cos(value);
		{double tmp0 = cos * this.m00 + sin * this.m01;
		double tmp1 = -sin * this.m00 + cos * this.m01;
		this.m00 = tmp0; this.m01 = tmp1;}
		{double tmp0 = cos * this.m10 + sin * this.m11;
		double tmp1 = -sin * this.m10 + cos * this.m11;
		this.m10 = tmp0; this.m11 = tmp1;}
		{double tmp0 = cos * this.m20 + sin * this.m21;
		double tmp1 = -sin * this.m20 + cos * this.m21;
		this.m20 = tmp0; this.m21 = tmp1;}
	}

	public final boolean invert(){return invert(this);}

	public final boolean invert(Matrix3d read)
    {
        double [] mat = new double[size() * 2];
        read.getColMajor(mat, 0, 6);
        mat[3] = mat[10] = mat[17] = 1;
        if (Calculate.toRREF(mat, 3) != 3){return false;}
        setColMajor(mat, 3, 6);
        return true;
    }

   @Override
    public final String toString(){
        StringBuilder strB = new StringBuilder(24);
        strB.append(m00).append(' ').append(m01).append(' ').append(m02).append(' ').append('\n');
        strB.append(m10).append(' ').append(m11).append(' ').append(m12).append(' ').append('\n');
        strB.append(m20).append(' ').append(m21).append(' ').append(m22).append(' ').append('\n');
        return strB.toString();
    }
   
   @Override
   public final double getD(int index) {
       switch(index)
       {
       case 0: return m00; case 1: return m01; case 2: return m02;
       case 3: return m10; case 4: return m11; case 5: return m12;
       case 6: return m20; case 7: return m21; case 8:return m22;
       default:throw new ArrayIndexOutOfBoundsException(index);
       }
   }
   
   
   @Override
   public final void setElem(int i, double value)
   {
       switch(i) {
       case 0: m00 = value;return;case 1: m01 = value;return;case 2: m02 = value;return;
       case 3: m10 = value;return;case 4: m11 = value;return;case 5: m12 = value;return;
       case 6: m20 = value;return;case 7: m21 = value;return;case 8:m22 = value;return;
       }
       throw new ArrayIndexOutOfBoundsException(i);
   }
   
   public final void dotl(Matrix3d lhs)
   {
       double x = lhs.m00 * m00 + lhs.m01 * m10 + lhs.m02 * m20;
       double y = lhs.m10 * m00 + lhs.m11 * m10 + lhs.m12 * m20;
       double z = lhs.m20 * m00 + lhs.m21 * m10 + lhs.m22 * m20;
            m00 = x;       m10 = y;       m20 = z;
              x = lhs.m00 * m01 + lhs.m01 * m11 + lhs.m02 * m21;
              y = lhs.m10 * m01 + lhs.m11 * m11 + lhs.m12 * m21;
              z = lhs.m20 * m01 + lhs.m21 * m11 + lhs.m22 * m21;
            m01 = x;       m11 = y;       m21 = z;
              x = lhs.m00 * m02 + lhs.m01 * m12 + lhs.m02 * m22;
              y = lhs.m10 * m02 + lhs.m11 * m12 + lhs.m12 * m22;
              z = lhs.m20 * m02 + lhs.m21 * m12 + lhs.m22 * m22;
            m02 = x;       m12 = y;       m22 = z;
   }
   
   public final void dotr(Matrix3d rhs)
   {
       double v0 = m00 * rhs.m00 + m01 * rhs.m10 + m02 * rhs.m20;
       double v1 = m00 * rhs.m01 + m01 * rhs.m11 + m02 * rhs.m21;
       double v2 = m00 * rhs.m02 + m01 * rhs.m12 + m02 * rhs.m22;
                   m00 = v0;      m01 = v1;      m02 = v2;
              v0 = m10 * rhs.m00 + m11 * rhs.m10 + m12 * rhs.m20;
              v1 = m10 * rhs.m01 + m11 * rhs.m11 + m12 * rhs.m21;
              v2 = m10 * rhs.m02 + m11 * rhs.m12 + m12 * rhs.m22;
                   m10 = v0;      m11 = v1;      m12 = v2;
              v0 = m20 * rhs.m00 + m21 * rhs.m10 + m22 * rhs.m20;
              v1 = m20 * rhs.m01 + m21 * rhs.m11 + m22 * rhs.m21;
              v2 = m20 * rhs.m02 + m21 * rhs.m12 + m22 * rhs.m22;
                   m20 = v0;      m21 = v1;      m22 = v2;
   }

   public final void dot(Matrix3d lhs, Matrix3d rhs) {
       if (lhs == this){dotl(rhs);return;}
       if (rhs == this){dotr(lhs);return;}
       m00 = lhs.m00 * rhs.m00 + lhs.m01 * rhs.m10 + lhs.m02 * rhs.m20;
       m01 = lhs.m00 * rhs.m01 + lhs.m01 * rhs.m11 + lhs.m02 * rhs.m21;
       m02 = lhs.m00 * rhs.m02 + lhs.m01 * rhs.m12 + lhs.m02 * rhs.m22;
       m10 = lhs.m10 * rhs.m00 + lhs.m11 * rhs.m10 + lhs.m12 * rhs.m20;
       m11 = lhs.m10 * rhs.m01 + lhs.m11 * rhs.m11 + lhs.m12 * rhs.m21;
       m12 = lhs.m10 * rhs.m02 + lhs.m11 * rhs.m12 + lhs.m12 * rhs.m22;
       m20 = lhs.m20 * rhs.m00 + lhs.m21 * rhs.m10 + lhs.m22 * rhs.m20;
       m21 = lhs.m20 * rhs.m01 + lhs.m21 * rhs.m11 + lhs.m22 * rhs.m21;
       m22 = lhs.m20 * rhs.m02 + lhs.m21 * rhs.m12 + lhs.m22 * rhs.m22;
   }

   public void transpose(Matrix3d other)
   {
       if (other == this)
       {
           transpose();
       }
       else
       {
           m00 = other.m00; m01 = other.m10; m02 = other.m20;
           m10 = other.m01; m11 = other.m11; m12 = other.m21;
           m20 = other.m02; m21 = other.m12; m22 = other.m22;
       }
   }

   public void transpose() {
        double tmp = m01; m10 = m01; m01 = tmp;
               tmp = m02; m20 = m02; m02 = tmp;
               tmp = m12; m21 = m12; m12 = tmp;
}
}
