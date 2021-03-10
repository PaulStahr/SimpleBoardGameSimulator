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

public final class Matrix4d implements Matrixd, DoubleList{
	public double m00, m01, m02, m03;
	public double m10, m11, m12, m13;
	public double m20, m21, m22, m23;
	public double m30, m31, m32, m33;

	public Matrix4d(){this(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);}
	
	public Matrix4d(double diag){this(diag,0,0,0,0,diag,0,0,0,0,diag,0,0,0,0,diag);}
	
	public Matrix4d(Matrix4d m) {
	   this.m00 = m.m00;this.m01 = m.m01;this.m02 = m.m02;this.m03 = m.m03;
	   this.m10 = m.m10;this.m11 = m.m11;this.m12 = m.m12;this.m13 = m.m13;
	   this.m20 = m.m20;this.m21 = m.m21;this.m22 = m.m22;this.m23 = m.m23;
	   this.m30 = m.m30;this.m31 = m.m31;this.m32 = m.m32;this.m33 = m.m33;
	}
	
	public Matrix4d(double x0, double x1, double x2, double x3, double y0, double y1, double y2, double y3, double z0, double z1, double z2, double z3, double w0, double w1, double w2, double w3){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;this.m03 = x3;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;this.m13 = y3;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;this.m23 = z3;
		this.m30 = w0;this.m31 = w1;this.m32 = w2;this.m33 = w3;
	}
	
	public final void set(Matrix3d mat)
	{
		this.m00 = mat.m00;this.m01 = mat.m01;this.m02 = mat.m02;
		this.m10 = mat.m10;this.m11 = mat.m11;this.m12 = mat.m12;
		this.m20 = mat.m20;this.m21 = mat.m21;this.m22 = mat.m22;
	}
	
	public final void set(double x0, double x1, double x2, double x3, double y0, double y1, double y2, double y3, double z0, double z1, double z2, double z3, double w0, double w1, double w2, double w3){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;this.m03 = x3;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;this.m13 = y3;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;this.m23 = z3;
		this.m30 = w0;this.m31 = w1;this.m32 = w2;this.m33 = w3;
	}
	
	public final boolean invert(Matrix4d read)
	{
		double [] mat = new double[size() * 2];
		read.getColMajor(mat, 0, 8);
		mat[4] = mat[13] = mat[22] = mat[31] = 1;
		if (Calculate.toRREF(mat, 4) != 4) {return false;}
		setColMajor(mat, 4, 8);
		return true;
	}

	@Override
	public void set(Matrixd o) {
		if (o instanceof Matrix4d)
		{
			set((Matrix4d) o);
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

	public final void setRowMajor(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;
	}
	
	public final void getCol(int row, Vector3d vec)
	{
		switch (row)
		{
			case 0:vec.set(m00, m10, m20);return;
			case 1:vec.set(m01, m11, m21);return;
			case 2:vec.set(m02, m12, m22);return;
			case 3:vec.set(m03, m13, m23);return;
			default: throw new ArrayIndexOutOfBoundsException(row);
		}
	}
	
	public final void getRow(int col, Vector3d vec)
	{
		switch (col)
		{
			case 0: vec.set(m00, m01, m02);return;
			case 1: vec.set(m10, m11, m12);return;
			case 2: vec.set(m20, m21, m22);return;
			case 3: vec.set(m30, m31, m32);return;
			default: throw new ArrayIndexOutOfBoundsException(col);
		}
	}
	
	public final double getRowDot3(int row)
	{
		switch (row)
		{
			case 0: return m00 * m00 + m01 * m01 + m02 * m02;
			case 1: return m10 * m10 + m11 * m11 + m12 * m12;
			case 2: return m20 * m20 + m21 * m21 + m22 * m22;
			case 3: return m30 * m30 + m31 * m31 + m32 * m32;
			default: throw new ArrayIndexOutOfBoundsException(row);
		}
	}
	
	public final void getRowDot3(Vector3d vec)
	{
		vec.x = m00 * m00 + m01 * m01 + m02 * m02;
		vec.y = m10 * m10 + m11 * m11 + m12 * m12;
		vec.z = m20 * m20 + m21 * m21 + m22 * m22;
	}
	
	public final void getColDot3(Vector3d vec)
	{
		vec.x = m00 * m00 + m10 * m10 + m20 * m20;
		vec.y = m01 * m01 + m11 * m11 + m21 * m21;
		vec.z = m02 * m02 + m12 * m12 + m22 * m22;
	}
	
	public final double getColDot3(int col)
	{	
		switch (col)
		{
			case 0: return m00 * m00 + m10 * m10 + m20 * m20;
			case 1: return m01 * m01 + m11 * m11 + m21 * m21;
			case 2: return m02 * m02 + m12 * m12 + m22 * m22;
			case 3: return m03 * m03 + m13 * m13 + m23 * m23;
			default: throw new ArrayIndexOutOfBoundsException(col);
		}
	}
	
	public final void setCols(Vector3d c0, Vector3d c1, Vector3d c2, Vector3d c3)
	{
		m00 = c0.x; m10 = c0.y; m20 = c0.z;
		m01 = c1.x; m11 = c1.y; m21 = c1.z;
		m02 = c2.x; m12 = c2.y; m22 = c2.z;
		m03 = c3.x; m13 = c3.y; m23 = c3.z;
	}
	
	public final void setCol(int row, Vector3d vec)
	{
		switch (row)
		{
			case 0:m00 = vec.x; m10 = vec.y; m20 = vec.z;return;
			case 1:m01 = vec.x; m11 = vec.y; m21 = vec.z;return;
			case 2:m02 = vec.x; m12 = vec.y; m22 = vec.z;return;
			case 3:m03 = vec.x; m13 = vec.y; m23 = vec.z;return;
			default: throw new ArrayIndexOutOfBoundsException(row);
		}
	}
	
	public final void setRow(int col, Vector3d vec)
	{
		switch (col)
		{
			case 0: m00 = vec.x; m01 = vec.y; m02 = vec.z;return;
			case 1: m10 = vec.x; m11 = vec.y; m12 = vec.z;return;
			case 2: m20 = vec.x; m21 = vec.y; m22 = vec.z;return;
			case 3: m30 = vec.x; m31 = vec.y; m32 = vec.z;return;
			default: throw new ArrayIndexOutOfBoundsException(col);
		}
	}
	
	public final void setRow(int row, double x, double y, double z)
	{
		switch (row)
		{
			case 0: m00 = x; m01 = y; m02 = z;return;
			case 1: m10 = x; m11 = y; m12 = z;return;
			case 2: m20 = x; m21 = y; m22 = z;return;
			case 3: m30 = x; m31 = y; m32 = z;return;
			default: throw new ArrayIndexOutOfBoundsException(row);
		}
	}
	
	public final void setCol(int col, double x, double y, double z)
	{
		switch (col)
		{
			case 0: m00 = x; m10 = y; m20 = z;return;
			case 1: m01 = x; m11 = y; m21 = z;return;
			case 2: m02 = x; m12 = y; m22 = z;return;
			case 3: m03 = x; m13 = y; m23 = z;return;
			default: throw new ArrayIndexOutOfBoundsException(col);
		}
	}
	
	public final void setRows(Vector3d x, Vector3d y, Vector3d z, Vector3d w)
	{
			m00 = x.x; m01 = x.y; m02 = x.z;
			m10 = y.x; m11 = y.y; m12 = y.z;
			m20 = z.x; m21 = z.y; m22 = z.z;
			m30 = w.x; m31 = w.y; m32 = w.z;
	}
	
	public final void setColMajor(final double mat[][]){
		m00 = mat[0][0]; m01 = mat[0][1]; m02 = mat[0][2]; m03 = mat[0][3];
		m10 = mat[1][0]; m11 = mat[1][1]; m12 = mat[1][2]; m13 = mat[1][3];
		m20 = mat[2][0]; m21 = mat[2][1]; m22 = mat[2][2]; m23 = mat[2][3];
		m30 = mat[3][0]; m31 = mat[3][1]; m32 = mat[3][2]; m33 = mat[3][3];
	}
	
	public final void setColMajor(final double mat[]){
		m00 = mat[0];  m01 = mat[1];  m02 = mat[2];  m03 = mat[3];
		m10 = mat[4];  m11 = mat[5];  m12 = mat[6];  m13 = mat[7];
		m20 = mat[8];  m21 = mat[9];  m22 = mat[10]; m23 = mat[11];
		m30 = mat[12]; m31 = mat[13]; m32 = mat[14]; m33 = mat[15];
	}
	
	public final void setColMajor(final double mat[][], int row, int col){
		m00 = mat[0 + row][col]; m01 = mat[0 + row][1 + col]; m02 = mat[0 + row][2 + col]; m03 = mat[0 + row][3 + col];
		m10 = mat[1 + row][col]; m11 = mat[1 + row][1 + col]; m12 = mat[1 + row][2 + col]; m13 = mat[1 + row][3 + col];
		m20 = mat[2 + row][col]; m21 = mat[2 + row][1 + col]; m22 = mat[2 + row][2 + col]; m23 = mat[2 + row][3 + col];
		m30 = mat[3 + row][col]; m31 = mat[3 + row][1 + col]; m32 = mat[3 + row][2 + col]; m33 = mat[3 + row][3 + col];
	}
	
	public final void setColMajor(final double mat[], int pos, int stride){
		m00 = mat[pos]; m01 = mat[pos+1]; m02 = mat[pos+2]; m03 = mat[pos+3];pos += stride;
		m10 = mat[pos]; m11 = mat[pos+1]; m12 = mat[pos+2]; m13 = mat[pos+3];pos += stride;
		m20 = mat[pos]; m21 = mat[pos+1]; m22 = mat[pos+2]; m23 = mat[pos+3];pos += stride;
		m30 = mat[pos]; m31 = mat[pos+1]; m32 = mat[pos+2]; m33 = mat[pos+3];
	}
	
	public final void getColMajor(final double mat[][]){
		mat[0][0] = m00; mat[0][1] = m01; mat[0][2] = m02; mat[0][3] = m03;
		mat[1][0] = m10; mat[1][1] = m11; mat[1][2] = m12; mat[1][3] = m13;
		mat[2][0] = m20; mat[2][1] = m21; mat[2][2] = m22; mat[2][3] = m23;
		mat[3][0] = m30; mat[3][1] = m31; mat[3][2] = m32; mat[3][3] = m33;
	}
	
	public final void getColMajor(final double mat[]){
		mat[0]  = m00; mat[1] = m01;  mat[2]  = m02; mat[3] = m03;
		mat[4]  = m10; mat[5] = m11;  mat[6]  = m12; mat[7] = m13;
		mat[8]  = m20; mat[9] = m21;  mat[10] = m22; mat[11] = m23;
		mat[12] = m30; mat[13] = m31; mat[14] = m32; mat[15] = m33;
	}
	
	public final void getColMajor(final double mat[], int begin, int stride){
		mat[begin] = m00; mat[begin+1] = m01; mat[begin+2] = m02; mat[begin+3] = m03;begin += stride;
		mat[begin] = m10; mat[begin+1] = m11; mat[begin+2] = m12; mat[begin+3] = m13;begin += stride;
		mat[begin] = m20; mat[begin+1] = m21; mat[begin+2] = m22; mat[begin+3] = m23;begin += stride;
		mat[begin] = m30; mat[begin+1] = m31; mat[begin+2] = m32; mat[begin+3] = m33;
	}
	
	@Override
	public final void setRowMajor(final double mat[][]){
		m00 = mat[0][0]; m01 = mat[1][0]; m02 = mat[2][0]; m03 = mat[3][0];
		m10 = mat[0][1]; m11 = mat[1][1]; m12 = mat[2][1]; m13 = mat[3][1];
		m20 = mat[0][2]; m21 = mat[1][2]; m22 = mat[2][2]; m23 = mat[3][2];
		m30 = mat[0][3]; m31 = mat[1][3]; m32 = mat[2][3]; m33 = mat[3][3];
	}
	
	public final void setRowMajor(final double mat[]){
		m00 = mat[0]; m01 = mat[4]; m02 = mat[8]; m03 = mat[12];
		m10 = mat[1]; m11 = mat[5]; m12 = mat[9]; m13 = mat[13];
		m20 = mat[2]; m21 = mat[6]; m22 = mat[10]; m23 = mat[14];
		m30 = mat[3]; m31 = mat[7]; m32 = mat[11]; m33 = mat[15];
	}
	
	public final void setRowMajor(final double mat[][], int row, int col){
		m00 = mat[row][0 + col]; m01 = mat[1 + row][0 + col]; m02 = mat[2 + row][0 + col]; m03 = mat[3 + row][0 + col];
		m10 = mat[row][1 + col]; m11 = mat[1 + row][1 + col]; m12 = mat[2 + row][1 + col]; m13 = mat[3 + row][1 + col];
		m20 = mat[row][2 + col]; m21 = mat[1 + row][2 + col]; m22 = mat[2 + row][2 + col]; m23 = mat[3 + row][2 + col];
		m30 = mat[row][3 + col]; m31 = mat[1 + row][3 + col]; m32 = mat[2 + row][3 + col]; m33 = mat[3 + row][3 + col];
	}
	
	public final void getRowMajor(final double mat[][]){
		mat[0][0] = m00; mat[1][0] = m01; mat[2][0] = m02; mat[3][0] = m03;
		mat[0][1] = m10; mat[1][1] = m11; mat[2][1] = m12; mat[3][1] = m13;
		mat[0][2] = m20; mat[1][2] = m21; mat[2][2] = m22; mat[3][2] = m23;
		mat[0][3] = m30; mat[1][3] = m31; mat[2][3] = m32; mat[3][3] = m33;
	}
	
	public final void rdotAffine(Vector3f v)	{rdotAffine(v.x, v.y, v.z, v);}

	public final void rdotAffine(double x, double y, double z, Vector3f v){
		v.x = (float)(m00 * x + m10 * y + m20 * z + m30);
		v.y = (float)(m01 * x + m11 * y + m21 * z + m31);
		v.z = (float)(m02 * x + m12 * y + m22 * z + m32);
	}

	public final double ldotX(double x, double y, double z, double w){return m00 * x + m10 * y + m20 * z + m30 * w;}
	public final double ldotY(double x, double y, double z, double w){return m01 * x + m11 * y + m21 * z + m31 * w;}
	public final double ldotZ(double x, double y, double z, double w){return m02 * x + m12 * y + m22 * z + m32 * w;}
	public final double ldotW(double x, double y, double z, double w){return m03 * x + m13 * y + m23 * z + m33 * w;}
	public final double rdotX(double x, double y, double z, double w){return m00 * x + m01 * y + m02 * z + m03 * w;}
	public final double rdotY(double x, double y, double z, double w){return m10 * x + m11 * y + m12 * z + m13 * w;}
	public final double rdotZ(double x, double y, double z, double w){return m20 * x + m21 * y + m22 * z + m23 * w;}
	public final double rdotW(double x, double y, double z, double w){return m30 * x + m31 * y + m32 * z + m33 * w;}
	public final double ldotAffineX(double x, double y, double z){return m00 * x + m10 * y + m20 * z + m30;}
	public final double ldotAffineY(double x, double y, double z){return m01 * x + m11 * y + m21 * z + m31;}
	public final double ldotAffineZ(double x, double y, double z){return m02 * x + m12 * y + m22 * z + m32;}
	public final double ldotAffineW(double x, double y, double z){return m03 * x + m13 * y + m23 * z + m33;}
	public final double rdotAffineX(double x, double y, double z){return m00 * x + m01 * y + m02 * z + m03;}
	public final double rdotAffineY(double x, double y, double z){return m10 * x + m11 * y + m12 * z + m13;}
	public final double rdotAffineZ(double x, double y, double z){return m20 * x + m21 * y + m22 * z + m23;}
	public final double rdotAffineW(double x, double y, double z){return m30 * x + m31 * y + m32 * z + m33;}
	public final double ldotX(double x, double y, double z){return m00 * x + m10 * y + m20 * z;}
	public final double ldotY(double x, double y, double z){return m01 * x + m11 * y + m21 * z;}
	public final double ldotZ(double x, double y, double z){return m02 * x + m12 * y + m22 * z;}
	public final double ldotW(double x, double y, double z){return m03 * x + m13 * y + m23 * z;}
	public final double rdotX(double x, double y, double z){return m00 * x + m01 * y + m02 * z;}
	public final double rdotY(double x, double y, double z){return m10 * x + m11 * y + m12 * z;}
	public final double rdotZ(double x, double y, double z){return m20 * x + m21 * y + m22 * z;}
	public final double rdotW(double x, double y, double z){return m30 * x + m31 * y + m32 * z;}
	
	public final void preTranslate(double x, double y, double z)
	{
		m03 += x * m00 + y * m01 + z * m02;
		m13 += x * m10 + y * m11 + z * m12;
		m23 += x * m20 + y * m21 + z * m22;
		m33 += x * m30 + y * m31 + z * m32;
	}
	
	public final void postTranslate(double x, double y, double z){
        m00 += m30 * x; m10 += m30 * y; m20 += m30 * z;
        m01 += m31 * x; m11 += m31 * y; m21 += m31 * z;
        m02 += m32 * x; m12 += m32 * y; m22 += m32 * z;
	    m03 += m33 * x; m13 += m33 * y; m23 += m33 * z;
	}
	
	public final void affineScale(double s)
	{
		m00 *= s; m10 *= s; m20 *= s;
		m01 *= s; m11 *= s; m21 *= s;
		m02 *= s; m12 *= s; m22 *= s;
	}

	public final void preScale(double x, double y, double z) {
		m00 *= x; m01 *= y; m02 *= z;
		m10 *= x; m11 *= y; m12 *= z;
		m20 *= x; m21 *= y; m22 *= z;
		m30 *= x; m31 *= y; m32 *= z;
	}

	public final void postScale(double x, double y, double z)
	{
		m00 *= x; m01 *= x; m02 *= x; m03 *= x;
		m10 *= y; m11 *= y; m12 *= y; m13 *= y;
		m20 *= z; m21 *= z; m22 *= z; m23 *= z;
	}

	public final void rdot(Vector4d vector){
		final double x = vector.x, y = vector.y, z = vector.z, w = vector.w;
		vector.x = m00 * x + m10 * y + m20 * z + m30 * w;
		vector.y = m01 * x + m11 * y + m21 * z + m31 * w;
		vector.z = m02 * x + m12 * y + m22 * z + m32 * w;
		vector.w = m03 * x + m13 * y + m23 * z + m33 * w;
	}

	public final void ldot(Vector4d vector){
		final double x = vector.x, y = vector.y, z = vector.z, w = vector.w;
		vector.x = m00 * x + m01 * y + m02 * z + m03 * w;
		vector.y = m10 * x + m11 * y + m12 * z + m13 * w;
		vector.z = m20 * x + m21 * y + m22 * z + m23 * w;
		vector.w = m30 * x + m31 * y + m32 * z + m33 * w;
	}

	public final void ldotAffine(Vector3d vector)						{ldotAffine(vector.x, vector.y, vector.z, vector);}	
	public final void rdotAffine(Vector3d vector)						{rdotAffine(vector.x, vector.y, vector.z, vector);}
	public void rdotAffine(float[] position, int i, Vector3d result) 	{rdotAffine(position[i], position[i + 1], position[i + 2], result);}
	public void rdotAffine(double[] position, int i, Vector3d result) 	{rdotAffine(position[i], position[i + 1], position[i + 2], result);}
	public void rdotAffine(DoubleList position, int i, Vector3d result) {rdotAffine(position.getD(i), position.getD(i + 1), position.getD(i + 2), result);}
	public final void ldot(Vector3d vector)                             {ldot(vector.x, vector.y, vector.z, vector);}
	
	public final void rdotAffine(double x, double y, double z, Vector3d vector){
		vector.x = m00 * x + m01 * y + m02 * z + m03;
		vector.y = m10 * x + m11 * y + m12 * z + m13;
		vector.z = m20 * x + m21 * y + m22 * z + m23;
	}
	
	public final void ldot(double x, double y, double z, Vector3d vector)
	{
		vector.x = m00 * x + m10 * y + m20 * z;
		vector.y = m01 * x + m11 * y + m21 * z;
		vector.z = m02 * x + m12 * y + m22 * z;
	}
	
	public final void rdot(double x, double y, double z, Vector3d result)
	{		
		result.x = m00 * x + m01 * y + m02 * z;
		result.y = m10 * x + m11 * y + m12 * z;
		result.z = m20 * x + m21 * y + m22 * z;
	}
	
	public void rdot(double x, double y, double z, float[] out, int i) {
		out[i++] = (float)(m00 * x + m01 * y + m02 * z);
		out[i++] = (float)(m10 * x + m11 * y + m12 * z);
		out[i++] = (float)(m20 * x + m21 * y + m22 * z);
	}
	
	public void rdot(Vector3d in, float result[], int i)	{rdot(in.x, in.y, in.z, result, i);}
	public void rdot(Vector3d in, double result[], int i)	{rdot(in.x, in.y, in.z, result, i);}
	public void rdot(Vector3d in, DoubleList result, int i)	{rdot(in.x, in.y, in.z, result, i);}
	public void rdot(Vector3d in, Object result, int i)		{rdot(in.x, in.y, in.z, result, i);}
	
	public void rdot(double x, double y, double z, double[] out, int i) {
		out[i++] = m00 * x + m01 * y + m02 * z;
		out[i++] = m10 * x + m11 * y + m12 * z;
		out[i++] = m20 * x + m21 * y + m22 * z;
	}
	
	public void rdot(double x, double y, double z, DoubleList out, int i) {
		out.setElem(i++, m00 * x + m01 * y + m02 * z);
		out.setElem(i++, m10 * x + m11 * y + m12 * z);
		out.setElem(i++, m20 * x + m21 * y + m22 * z);
	}
	
	public final void rdot(Vector3d vector){rdot(vector.x, vector.y, vector.z, vector);}
	
	public final void rdot(double x, double y, double z, Object out, int pos)
	{
		if (out instanceof float[])        {rdot(x, y, z, (float[])out, pos);}
    	else if (out instanceof double[])  {rdot(x, y, z, (double[])out, pos);}
    	else if (out instanceof DoubleList){rdot(x, y, z, (DoubleList)out, pos);}
    	else                               {throw new IllegalArgumentException(out.getClass().toString());}
	}
	
	public final void rdot(Object in, int pos, Vector3d out)
	{
		if (in instanceof float[])         {rdot((float[])in, pos, out);}
    	else if (in instanceof double[])   {rdot((double[])in, pos, out);}
    	else if (in instanceof DoubleList) {rdot((DoubleList)in, pos, out);}
    	else                               {throw new IllegalArgumentException(in.getClass().toString());}
	}
	
	public final void rdot(float in[], int index, Vector3d vector)     {rdot(in[index], in[index + 1], in[index + 2], vector);}
	public final void rdot(double in[], int index, Vector3d vector)    {rdot(in[index], in[index + 1], in[index + 2], vector);}
	public final void rdot(DoubleList in, int index, Vector3d vector)  {rdot(in.getD(index), in.getD(index + 1), in.getD(index + 2), vector);}
	
	public final void rdotAffine(Object in, int pos, Vector3d out)
	{
		if 		(in instanceof float[])		{rdotAffine((float[])in, pos, out);}
    	else if (in instanceof double[])	{rdotAffine((double[])in, pos, out);}
    	else if (in instanceof DoubleList)	{rdotAffine((DoubleList)in, pos, out);}
    	else{throw new IllegalArgumentException(in.getClass().toString());}
	}
	
	public final void rdotAffine(Vector3d in, Object out, int pos)
	{
		if		(out instanceof float[])			{rdotAffine(in, (float[])out, pos);}
    	else if (out instanceof double[])   {rdotAffine(in, (double[])out, pos);}
    	else if (out instanceof DoubleList)	{rdotAffine(in, (DoubleList)out, pos);}
    	else	{throw new IllegalArgumentException(in.getClass().toString());}
	}
	

	
	public final void ldotAffine(Vector3d vector, Vector3d out)				{ldotAffine(vector.x, vector.y, vector.z, out);}
	public final void rdotAffine(Vector3d vector, Vector3d out)				{rdotAffine(vector.x, vector.y, vector.z, out);}
	public final void rdotAffine(Vector3d vector, float out[], int index)	{rdotAffine(vector.x, vector.y, vector.z, out, index);}
	public final void rdotAffine(Vector3d vector, double out[], int index)	{rdotAffine(vector.x, vector.y, vector.z, out, index);}
	public final void rdotAffine(Vector3d vector, DoubleList out, int index){rdotAffine(vector.x, vector.y, vector.z, out, index);}
	public final void ldotAffine(Vector3d vector, float out[], int index)	{ldotAffine(vector.x, vector.y, vector.z, out, index);}
	
	public final void ldotAffine(double x, double y, double z, Vector3d out){
		out.x = m00 * x + m10 * y + m20 * z + m30;
		out.y = m01 * x + m11 * y + m21 * z + m31;
		out.z = m02 * x + m12 * y + m22 * z + m32;
	}
		
	public final void ldotAffine(double x, double y, double z, float out[], int index){
		out[index]   = (float)(m00 * x + m10 * y + m20 * z + m30);
		out[++index] = (float)(m01 * x + m11 * y + m21 * z + m31);
		out[++index] = (float)(m02 * x + m12 * y + m22 * z + m32);
	}
	
	public final void rdotAffine(double x, double y, double z, float out[], int index){
		out[index]   = (float)(m00 * x + m01 * y + m02 * z + m03);
		out[++index] = (float)(m10 * x + m11 * y + m12 * z + m13);
		out[++index] = (float)(m20 * x + m21 * y + m22 * z + m23);
	}
	
	public final void rdotAffine(double x, double y, double z, double out[], int index){
		out[index]   = m00 * x + m01 * y + m02 * z + m03;
		out[++index] = m10 * x + m11 * y + m12 * z + m13;
		out[++index] = m20 * x + m21 * y + m22 * z + m23;
	}
	
	public final void rdotAffine(double x, double y, double z, DoubleList out, int index){
		out.setElem(index++,m00 * x + m01 * y + m02 * z + m03);
		out.setElem(index++,m10 * x + m11 * y + m12 * z + m13);
		out.setElem(index++,m20 * x + m21 * y + m22 * z + m23);
	}
	
	public final void ldotAffine(DoubleArrayList in, int inIndex, float[] out, int outIndex){ldotAffine(in.getD(inIndex), in.getD(++inIndex), in.getD(++inIndex), out, outIndex);}
	public final void rdotAffine(DoubleArrayList in, int inIndex, float[] out, int outIndex){rdotAffine(in.getD(inIndex), in.getD(++inIndex), in.getD(++inIndex), out, outIndex);}
	public final void rdotAffine(float in[], int inIndex, float[] out, int outIndex) 		{rdotAffine(in[inIndex], in[inIndex + 1], in[inIndex + 2], out, outIndex);}
	public final void rdotAffine(double in[], int inIndex, float[] out, int outIndex) 		{rdotAffine(in[inIndex], in[inIndex + 1], in[inIndex + 2], out, outIndex);}
	
	public final void ldot(double x, double y, double z, double w, float out[], int index){
		out[index]   = (float)(m00 * x + m10 * y + m20 * z + m30 * w);
		out[++index] = (float)(m01 * x + m11 * y + m21 * z + m31 * w);
		out[++index] = (float)(m02 * x + m12 * y + m22 * z + m32 * w);
	}
	
	public final void rdot(double x, double y, double z, double w, float out[], int index){
		out[index]   = (float)(m00 * x + m01 * y + m02 * z + m03 * w);
		out[++index] = (float)(m10 * x + m11 * y + m12 * z + m13 * w);
		out[++index] = (float)(m20 * x + m21 * y + m22 * z + m23 * w);
	}
	
	@Override
	public final String toString(){
		StringBuilder strB = new StringBuilder(24);
		strB.append(m00).append(' ').append(m01).append(' ').append(m02).append(' ').append(m03).append('\n');
		strB.append(m10).append(' ').append(m11).append(' ').append(m12).append(' ').append(m13).append('\n');
		strB.append(m20).append(' ').append(m21).append(' ').append(m22).append(' ').append(m23).append('\n');
		strB.append(m30).append(' ').append(m31).append(' ').append(m32).append(' ').append(m33).append('\n');
		return strB.toString();
	}
	
	@Override
	public final void setElem(int i, double value)
	{
		switch(i) {
		case 0: m00 = value;return;case 1: m01 = value;return;case 2: m02 = value;return;case 3: m03=value;return;
		case 4: m10 = value;return;case 5: m11 = value;return;case 6: m12 = value;return;case 7: m13=value;return;
		case 8: m20 = value;return;case 9: m21 = value;return;case 10:m22 = value;return;case 11:m23=value;return;
		case 12:m30 = value;return;case 13:m31 = value;return;case 14:m32 = value;return;case 15:m33=value;return;
		}
		throw new ArrayIndexOutOfBoundsException(i);
	}
	
	@Override
	public final void set(int x, int y, double value){
		switch(x){
			case 0:switch(y){case 0:m00 = value;return;case 1:m01 = value;return;case 2:m02 = value;return;case 3:m03 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:m10 = value;return;case 1:m11 = value;return;case 2:m12 = value;return;case 3:m13 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:m20 = value;return;case 1:m21 = value;return;case 2:m22 = value;return;case 3:m23 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 3:switch(y){case 0:m30 = value;return;case 1:m31 = value;return;case 2:m32 = value;return;case 3:m33 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}

	@Override
	public final double get(int x, int y) {
		switch(x){
			case 0:switch(y){case 0:return m00;case 1:return m01;case 2:return m02;case 3:return m03;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:return m10;case 1:return m11;case 2:return m12;case 3:return m13;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:return m20;case 1:return m21;case 2:return m22;case 3:return m23;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 3:switch(y){case 0:return m30;case 1:return m31;case 2:return m32;case 3:return m33;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}

	public final void set(Matrix4d o) {
		this.m00 = o.m00;this.m01 = o.m01;this.m02 = o.m02;this.m03 = o.m03;
		this.m10 = o.m10;this.m11 = o.m11;this.m12 = o.m12;this.m13 = o.m13;
		this.m20 = o.m20;this.m21 = o.m21;this.m22 = o.m22;this.m23 = o.m23;
		this.m30 = o.m30;this.m31 = o.m31;this.m32 = o.m32;this.m33 = o.m33;
	}

	@Override
	public final int size() {return 16;}
	
	@Override
	public final int rows(){return 4;}
	
	@Override
	public final int cols(){return 4;}

	@Override
	public final double getD(int index) {
		switch(index)
		{
		case 0: return m00; case 1: return m01; case 2: return m02; case 3: return m03;
		case 4: return m10; case 5: return m11; case 6: return m12; case 7: return m13;
		case 8: return m20; case 9: return m21; case 10:return m22; case 11:return m23;
		case 12:return m30; case 13:return m31; case 14:return m32; case 15:return m33;
		default:throw new ArrayIndexOutOfBoundsException(index);
		}
	}
	
	public final void dotl(Matrix4d lhs)
	{
		double x = lhs.m00 * m00 + lhs.m01 * m10 + lhs.m02 * m20 + lhs.m03 * m30;
		double y = lhs.m10 * m00 + lhs.m11 * m10 + lhs.m12 * m20 + lhs.m13 * m30;
		double z = lhs.m20 * m00 + lhs.m21 * m10 + lhs.m22 * m20 + lhs.m23 * m30;
		double w = lhs.m30 * m00 + lhs.m31 * m10 + lhs.m32 * m20 + lhs.m33 * m30;
                            m00 = x;       m10 = y;       m20 = z;       m30 = w;
	    	   x = lhs.m00 * m01 + lhs.m01 * m11 + lhs.m02 * m21 + lhs.m03 * m31;
		       y = lhs.m10 * m01 + lhs.m11 * m11 + lhs.m12 * m21 + lhs.m13 * m31;
		       z = lhs.m20 * m01 + lhs.m21 * m11 + lhs.m22 * m21 + lhs.m23 * m31;
		       w = lhs.m30 * m01 + lhs.m31 * m11 + lhs.m32 * m21 + lhs.m33 * m31;
                            m01 = x;       m11 = y;       m21 = z;       m31 = w;
		       x = lhs.m00 * m02 + lhs.m01 * m12 + lhs.m02 * m22 + lhs.m03 * m32;
		       y = lhs.m10 * m02 + lhs.m11 * m12 + lhs.m12 * m22 + lhs.m13 * m32;
		       z = lhs.m20 * m02 + lhs.m21 * m12 + lhs.m22 * m22 + lhs.m23 * m32;
		       w = lhs.m30 * m02 + lhs.m31 * m12 + lhs.m32 * m22 + lhs.m33 * m32;
                            m02 = x;       m12 = y;       m22 = z;       m32 = w;
		       x = lhs.m00 * m03 + lhs.m01 * m13 + lhs.m02 * m23 + lhs.m03 * m33;
		       y = lhs.m10 * m03 + lhs.m11 * m13 + lhs.m12 * m23 + lhs.m13 * m33;
		       z = lhs.m20 * m03 + lhs.m21 * m13 + lhs.m22 * m23 + lhs.m23 * m33;
		       w = lhs.m30 * m03 + lhs.m31 * m13 + lhs.m32 * m23 + lhs.m33 * m33;
                            m03 = x;       m13 = y;       m23 = z;       m33 = w;
	}
	
	public final void dotr(Matrix4d rhs)
	{
		double v0 = m00 * rhs.m00 + m01 * rhs.m10 + m02 * rhs.m20 + m03 * rhs.m30;
		double v1 = m00 * rhs.m01 + m01 * rhs.m11 + m02 * rhs.m21 + m03 * rhs.m31;
		double v2 = m00 * rhs.m02 + m01 * rhs.m12 + m02 * rhs.m22 + m03 * rhs.m32;
		double v3 = m00 * rhs.m03 + m01 * rhs.m13 + m02 * rhs.m23 + m03 * rhs.m33;
                    m00 = v0;      m01 = v1;      m02 = v2;      m03 = v3;
               v0 = m10 * rhs.m00 + m11 * rhs.m10 + m12 * rhs.m20 + m13 * rhs.m30;
		       v1 = m10 * rhs.m01 + m11 * rhs.m11 + m12 * rhs.m21 + m13 * rhs.m31;
		       v2 = m10 * rhs.m02 + m11 * rhs.m12 + m12 * rhs.m22 + m13 * rhs.m32;
		       v3 = m10 * rhs.m03 + m11 * rhs.m13 + m12 * rhs.m23 + m13 * rhs.m33;
                    m10 = v0;      m11 = v1;      m12 = v2;      m13 = v3;
               v0 = m20 * rhs.m00 + m21 * rhs.m10 + m22 * rhs.m20 + m23 * rhs.m30;
			   v1 = m20 * rhs.m01 + m21 * rhs.m11 + m22 * rhs.m21 + m23 * rhs.m31;
		       v2 = m20 * rhs.m02 + m21 * rhs.m12 + m22 * rhs.m22 + m23 * rhs.m32;
		       v3 = m20 * rhs.m03 + m21 * rhs.m13 + m22 * rhs.m23 + m23 * rhs.m33;
                    m20 = v0;      m21 = v1;      m22 = v2;      m23 = v3;
		       v0 = m30 * rhs.m00 + m31 * rhs.m10 + m32 * rhs.m20 + m33 * rhs.m30;
		       v1 = m30 * rhs.m01 + m31 * rhs.m11 + m32 * rhs.m21 + m33 * rhs.m31;
		       v2 = m30 * rhs.m02 + m31 * rhs.m12 + m32 * rhs.m22 + m33 * rhs.m32;
		       v3 = m30 * rhs.m03 + m31 * rhs.m13 + m32 * rhs.m23 + m33 * rhs.m33;
                    m30 = v0;      m31 = v1;      m32 = v2;      m33 = v3;
	}

	public final void dot(Matrix4d lhs, Matrix4d rhs) {
		if (lhs == this){dotl(rhs);return;}
		if (rhs == this){dotr(lhs);return;}
		m00 = lhs.m00 * rhs.m00 + lhs.m01 * rhs.m10 + lhs.m02 * rhs.m20 + lhs.m03 * rhs.m30;
		m01 = lhs.m00 * rhs.m01 + lhs.m01 * rhs.m11 + lhs.m02 * rhs.m21 + lhs.m03 * rhs.m31;
		m02 = lhs.m00 * rhs.m02 + lhs.m01 * rhs.m12 + lhs.m02 * rhs.m22 + lhs.m03 * rhs.m32;
		m03 = lhs.m00 * rhs.m03 + lhs.m01 * rhs.m13 + lhs.m02 * rhs.m23 + lhs.m03 * rhs.m33;
		m10 = lhs.m10 * rhs.m00 + lhs.m11 * rhs.m10 + lhs.m12 * rhs.m20 + lhs.m13 * rhs.m30;
		m11 = lhs.m10 * rhs.m01 + lhs.m11 * rhs.m11 + lhs.m12 * rhs.m21 + lhs.m13 * rhs.m31;
		m12 = lhs.m10 * rhs.m02 + lhs.m11 * rhs.m12 + lhs.m12 * rhs.m22 + lhs.m13 * rhs.m32;
		m13 = lhs.m10 * rhs.m03 + lhs.m11 * rhs.m13 + lhs.m12 * rhs.m23 + lhs.m13 * rhs.m33;
		m20 = lhs.m20 * rhs.m00 + lhs.m21 * rhs.m10 + lhs.m22 * rhs.m20 + lhs.m23 * rhs.m30;
		m21 = lhs.m20 * rhs.m01 + lhs.m21 * rhs.m11 + lhs.m22 * rhs.m21 + lhs.m23 * rhs.m31;
		m22 = lhs.m20 * rhs.m02 + lhs.m21 * rhs.m12 + lhs.m22 * rhs.m22 + lhs.m23 * rhs.m32;
		m23 = lhs.m20 * rhs.m03 + lhs.m21 * rhs.m13 + lhs.m22 * rhs.m23 + lhs.m23 * rhs.m33;
		m30 = lhs.m30 * rhs.m00 + lhs.m31 * rhs.m10 + lhs.m32 * rhs.m20 + lhs.m33 * rhs.m30;
		m31 = lhs.m30 * rhs.m01 + lhs.m31 * rhs.m11 + lhs.m32 * rhs.m21 + lhs.m33 * rhs.m31;
		m32 = lhs.m30 * rhs.m02 + lhs.m31 * rhs.m12 + lhs.m32 * rhs.m22 + lhs.m33 * rhs.m32;
		m33 = lhs.m30 * rhs.m03 + lhs.m31 * rhs.m13 + lhs.m32 * rhs.m23 + lhs.m33 * rhs.m33;
	}
}
