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
	public double x0, x1, x2, y0, y1, y2, z0, z1, z2;

	
	public Matrix3d(){this(1,0,0,0,1,0,0,0,1);}
	
	public Matrix3d(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.x0 = x0;this.x1 = x1;this.x2 = x2;
		this.y0 = y0;this.y1 = y1;this.y2 = y2;
		this.z0 = z0;this.z1 = z1;this.z2 = z2;
	}
	
	//public final void transform(Rotation3 rot){
		//double radX = rot.getXRadians();
		//double radY = rot.getXRadians();
		//double radZ = rot.getXRadians();
		
	//}
	
	public void setIdentity()
	{
		this.x0 = this.y1 = this.z2 = 1;
		this.x1 = this.x2 = this.y0 = this.y2 = this.z0 = this.z1 = 0;
	}
	
	public final void transform(Vector3f v){
		final float x = v.x, y = v.y, z = v.z;
		v.x = (float)(x0 * x + y0 * y + z0 * z);
		v.y = (float)(x1 * x + y1 * y + z1 * z);
		v.z = (float)(x2 * x + y2 * y + z2 * z);
	}
	
	public final double getNewX(double x, double y, double z){
		return x0 * x + y0 * y + z0 * z;
	}

	public final double getNewY(double x, double y, double z){
		return x1 * x + y1 * y + z1 * z;
	}

	public final double getNewZ(double x, double y, double z){
		return x2 * x + y2 * y + z2 * z;
	}
	
	@Override
	public final double get(int x, int y) {
		switch(x){
			case 0:switch(y){case 0:return x0;case 1:return x1;case 2:return x2;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:return y0;case 1:return y1;case 2:return y2;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:return z0;case 1:return z1;case 2:return z2;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}

	@Override
	public final int cols() {return 3;}
	
	@Override
	public final int rows() {return 3;}

	
	@Override
	public final void setRowMajor(final double mat[][]){
		x0 = mat[0][0]; x1 = mat[1][0]; x2 = mat[2][0];
		y0 = mat[0][1]; y1 = mat[1][1]; y2 = mat[2][1];
		z0 = mat[0][2]; z1 = mat[1][2]; z2 = mat[2][2];
	}

	
	@Override
	public final void set(int x, int y, double value){
		switch(x){
			case 0:switch(y){case 0:x0 = value;return;case 1:x1 = value;return;case 2:x2 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:y0 = value;return;case 1:y1 = value;return;case 2:y2 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 2:switch(y){case 0:z0 = value;return;case 1:z1 = value;return;case 2:z2 = value;return;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}


	public final void set(Matrix3d o) {
		this.x0 = o.x0;this.x1 = o.x1;this.x2 = o.x2;
		this.y0 = o.y0;this.y1 = o.y1;this.y2 = o.y2;
		this.z0 = o.z0;this.z1 = o.z1;this.z2 = o.z2;
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
		v.x = x0 * x + y0 * y + z0;
		v.y = x1 * x + y1 * y + z1;
	}

	public final double transformAffineX(double x, double y) {
		return x0 * x + y0 * y + z0;
	}
	
	public final double transformAffineY(double x, double y) {
		return x1 * x + y1 * y + z1;
	}

	public void set(double x0, double x1, double x2, double y0, double y1, double y2, double z0, double z1, double z2){
		this.x0 = x0;this.x1 = x1;this.x2 = x2;
		this.y0 = y0;this.y1 = y1;this.y2 = y2;
		this.z0 = z0;this.z1 = z1;this.z2 = z2;
	}

	public final void affineTranslate(double translateX, double translateY) {
		z0 += translateX;
		z1 += translateY;
	}

	public final void scale(double d) {
		this.x0 *= d;this.x1 *= d; this.x2 *= d;
		this.y0 *= d;this.y1 *= d; this.y2 *= d;
		this.z0 *= d;this.z1 *= d; this.z2 *= d;
	}

	public void transformAffine(double x, double y, Vector2d out) {
		out.x = transformAffineX(x, y);
		out.y = transformAffineY(x, y);
	}
	
	public final void rotateZ(double value)
	{
		double sin = Math.sin(value), cos = Math.cos(value);
		{double tmp0 = cos * this.x0 + sin * this.x1;
		double tmp1 = -sin * this.x0 + cos * this.x1;
		this.x0 = tmp0; this.x1 = tmp1;}
		{double tmp0 = cos * this.y0 + sin * this.y1;
		double tmp1 = -sin * this.y0 + cos * this.y1;
		this.y0 = tmp0; this.y1 = tmp1;}
		{double tmp0 = cos * this.z0 + sin * this.z1;
		double tmp1 = -sin * this.z0 + cos * this.z1;
		this.z0 = tmp0; this.z1 = tmp1;}
	}
}
