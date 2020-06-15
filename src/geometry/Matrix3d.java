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

public class Matrix3d implements Matrixd{
	public double m00, m01, m02, m10, m11, m12, m20, m21, m22;

	
	public Matrix3d(){this(1,0,0,0,1,0,0,0,1);}
	
	public Matrix3d(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;
	}
	
	//public final void transform(Rotation3 rot){
		//double radX = rot.getXRadians();
		//double radY = rot.getXRadians();
		//double radZ = rot.getXRadians();
		
	//}
	
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
	
	public final double getNewX(double x, double y, double z){
		return m00 * x + m10 * y + m20 * z;
	}

	public final double getNewY(double x, double y, double z){
		return m01 * x + m11 * y + m21 * z;
	}

	public final double getNewZ(double x, double y, double z){
		return m02 * x + m12 * y + m22 * z;
	}
	
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

	public final double transformAffineX(double x, double y) {
		return m00 * x + m10 * y + m20;
	}
	
	public final double transformAffineY(double x, double y) {
		return m01 * x + m11 * y + m21;
	}

	public void set(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.m00 = x0;this.m01 = x1;this.m02 = x2;
		this.m10 = y0;this.m11 = y1;this.m12 = y2;
		this.m20 = z0;this.m21 = z1;this.m22 = z2;
	}

	public final void affineTranslate(double translateX, double translateY) {
		m20 += translateX;
		m21 += translateY;
	}

	public final void scale(double d) {
		this.m00 *= d;this.m01 *= d; this.m02 *= d;
		this.m10 *= d;this.m11 *= d; this.m12 *= d;
		this.m20 *= d;this.m21 *= d; this.m22 *= d;
	}

	public void transformAffine(double x, double y, Vector2d out) {
		out.x = transformAffineX(x, y);
		out.y = transformAffineY(x, y);
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
}
