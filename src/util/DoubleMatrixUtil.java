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

public class DoubleMatrixUtil {

    public static final void vectorMatrixMultiplication(double matrix[][], double vector[], double erg[]){
    	for (int i=0;i<matrix.length;++i){
    		double sum = 0;
    		for (int j=0;j<matrix[i].length;++j){
    			sum += matrix[i][j] * erg[j];
    		}
    		erg[i] = sum;
    	}
    }

    public static final double norm(double vector[]){
    	double sum = 0;
    	for (int i=0;i<vector.length;++i)
    		sum += vector[i] * vector[i];
    	return sum;
    }

    public static final double scalarProd(double v1[], double v2[]){
    	double sum = 0;
    	for (int i=0;i<v1.length;++i)
    		sum += v1[i] * v2[i];
    	return sum;
    }

    public static final void vectorIteration(double matrix[][], double start[], double E){
    	double v1[] = new double[start.length];
    	do{
    		vectorMatrixMultiplication(matrix, start, v1);
    	}while(false);
    }

	public static void multiply(float[] vertices, float d, int from, int to) {
		for (int i = from; i < to; ++i)
		{
			vertices[i] *= d;
		}
	}


	public static final void multiply(double[] data, int from, int to, double d) {
		for (int i = from; i < to; ++i)
		{
			data[i] *= d;
		}
	}

	public static final void partialSum(double[] data, int from, int to) {
		for (int i = from + 1; i < to; ++i)
		{
			data[i] += data[i - 1];
		}
	}

	public static final void addTo(double[] input0, double[] toAdd, double scalar, double[] out) {
		for (int i = 0; i < input0.length; ++i)
		{
			out[i] = input0[i] + toAdd[i] * scalar;
		}
	}
}
