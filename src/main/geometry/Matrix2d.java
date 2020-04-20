package main.geometry;

public class Matrix2d implements Matrixd{
	double x0, x1, y0, y1;
	
	public Matrix2d(){this(1,0,0,1);}
	
	public Matrix2d(double x0, double x1, double y0, double y1){
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}
	
	public void set(double x0, double x1, double y0, double y1)
	{
		this.x0 = x0;
		this.x1 = x1;
		this.y0 = y0;
		this.y1 = y1;
	}
	
	public void setIdentity() {
		this.x0 = this.y1 = 1;
		this.x1 = this.y0 = 0;
	}
	
	@Override
	public final double get(int x, int y) {
		switch(x){
			case 0:switch(y){case 0:return x0;case 1:return x1;default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:return y0;case 1:return y1;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}

	@Override
	public final int cols() {return 2;}
	
	@Override
	public final int rows() {return 2;}

	
	@Override
	public final void setRowMajor(final double mat[][]){
		x0 = mat[0][0]; x1 = mat[1][0];
		y0 = mat[0][1]; y1 = mat[1][1];
	}

	
	@Override
	public final void set(int x, int y, double value){
		switch(x){
			case 0:switch(y){case 0:x0 = value;return;case 1:x1 = value;return;case 2:default: throw new ArrayIndexOutOfBoundsException(y);}
			case 1:switch(y){case 0:y0 = value;return;case 1:y1 = value;return;case 2:return;default: throw new ArrayIndexOutOfBoundsException(y);}
		}
		throw new ArrayIndexOutOfBoundsException(x);
	}


	public final void set(Matrix2d o) {
		this.x0 = o.x0;this.x1 = o.x1;
		this.y0 = o.y0;this.y1 = o.y1;
	}

	
	@Override
	public void set(Matrixd o) {
		if (o instanceof Matrix2d)
		{
			set((Matrix2d) o);
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
}
