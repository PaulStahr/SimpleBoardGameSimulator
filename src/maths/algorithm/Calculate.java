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
package maths.algorithm;

import java.util.Arrays;
import java.util.Random;

import util.ArrayUtil;
import util.DoubleFunctionDouble;
import util.DoubleMatrixUtil;
import util.data.DoubleList;

/**
 * Write a description of class Math here.
 *
 * @author Paul Stahr, Yakup Ates
 * @version (a version number or a date)
 */
public abstract class Calculate
{
    public static final byte FAST = 0, CODE = 1, SYSTEM = 2;
    public static byte method = SYSTEM;
    private static final double fakCacheDouble[] = new double[171];
    private static final long fakCacheLong[] = new long[21];
    private static final int primeCache[] = new int[100];
    private static final char primeCacheBoolean[] = new char[0x10000];
    private static final double sinCache[] = new double[0x10000];
    private static final long fibunacciCache[] = new long[93];
    private static boolean inited = false;
    public static final double TWO_PI = 2 * Math.PI;
    private static final long divisorCacheForSin[] = new long[30];
    private static final double LONG_MAX_VALUE_INV=1d/Long.MAX_VALUE;
    //private static final double TWO_PI_INV=1/TWO_PI;
    //private static final double TWO_PI_TO_LONG_RANGE = TWO_PI_INV*Long.MAX_VALUE;
    //private static final double LONG_TO_TWO_PI_RANGE = 1/TWO_PI_TO_LONG_RANGE;

    public synchronized static void init(){
    	final double step = Math.PI/(sinCache.length*2);
        for (int i=0;i<sinCache.length;i++)
            sinCache[i] = Math.sin(i*step);
        final int length = primeCacheBoolean.length<<4;
        for (int i=2, count=0;i<length;i++){
        	if ((primeCacheBoolean[i>>4]&(0x8000>>(i&15)))==0){
            	if (count<primeCache.length)
            		primeCache[count++] = i;
            	for (int j=i+i;j<length;j+=i)
            		primeCacheBoolean[j>>4]|=(0x8000>>(j&15));
        	}
        }

        fibunacciCache[0] = 0;
        fibunacciCache[1] = 1;
        for (int i=2;i<fibunacciCache.length;++i){
        	fibunacciCache[i] = fibunacciCache[i - 1] + fibunacciCache[i - 2];
        }

        for (int i=3, j=0; j<divisorCacheForSin.length;i+=2, j++)
        	divisorCacheForSin[j] = Long.MAX_VALUE / (i * (i - 1));
        {
            long l=1;
	        int i;
	        fakCacheDouble[0] = fakCacheLong[0] = 0;
	        for (i=1;i<fakCacheLong.length;++i)
	            fakCacheDouble[i] = fakCacheLong[i] = (l*=i);
	        for (double d = l;i<fakCacheDouble.length;++i)
	            fakCacheDouble[i] = (d*=i);
        }
        inited = true;
    }

    private Calculate(){}

    public static final long[] getFacLongs(){
    	if (!inited)
    		init();
    	return fakCacheLong.clone();
    }

    public static final double[] getFacDoubles(){
    	if (!inited)
    		init();
    	return fakCacheDouble.clone();
    }

    public static final boolean isFibunacci(long number){
    	int index = Arrays.binarySearch(fibunacciCache, number);
    	if (index >= 0)
    		return true;
    	return false;
    }

    public static int getPoissonRandom(double mean) {
        Random r = new Random();
        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * r.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }

    public static final boolean isPrime(long number){
	    if (number < 2)
	        return false;
	    if ((number>>4) < primeCacheBoolean.length)
	    	return (primeCacheBoolean[(int)number>>4]&(0x8000>>(number&15)))==0;
	    long sqrt = 1+(long)Math.sqrt(number);
	    for (int i=0;i<primeCache.length && primeCache[i] < sqrt;i++)
	    	if (number % primeCache[i] == 0)
	    		return false;
	    for (long i = primeCache[primeCache.length - 1];i<sqrt;i++)
	        if (number%i == 0)
	            return false;
	    return true;
	}

    public static final double sin(final double x){
        switch (method){
            case FAST  : return fastSin(x);
            case CODE  : return codeSin(x);
            case SYSTEM: return Math.sin(x);
        }
        return Double.NaN;
    }

    public static final double sintest(){
        long time =System.nanoTime();
        double value = 0;
        for (double i=-1000000;i<1000000;i+=0.01)
            value += codeSin(i);
        System.out.println(System.nanoTime()-time);
        return value;
    }

    public static final double fastSin(final double x){
    	if (!inited)
    		init();
        final int index = (int)(x*(0x1FFFE/Math.PI))&0x3FFFC;
        if (index<0x20000)
            return index < 0x10000 ? sinCache[index] : sinCache[0x1FFFF-index];
        else
            return -(index < 0x30000 ? sinCache[index-0x20000] : sinCache[0x3FFFF-index]);
    }

    public static final double codeSin(double x){
        boolean negative = false;
        x=x<0.0 ? x%TWO_PI+TWO_PI : x % TWO_PI;

        if (x>Math.PI){
            x-=Math.PI;
            negative = true;
        }
        final double xq = -x*x*LONG_MAX_VALUE_INV;
        double erg=x,exp=x;
        for (int i=0;i<10;i++)
        	erg += (exp *= xq * divisorCacheForSin[i]);
        return negative ? -erg : erg;
    }

    public static final double cos(final double x){
        switch (method){
            case FAST  : return fastCos(x);
            case CODE  : return codeCos(x);
            case SYSTEM: return Math.cos(x);
        }
        return Double.NaN;
    }

    public static final double fastCos(final double x){
    	if (!inited)
    		init();
        int index = (int)(x*(0x1FFFE/Math.PI))-0x10000&0x3FFFC;
        if (index<0x20000)
            return index < 0x10000 ? sinCache[index] : sinCache[0x1FFFF-index];
        else
            return index < 0x30000 ? -sinCache[index-0x20000] : -sinCache[0x3FFFF-index];
    }

    public static final double codeCos(final double x){
        return codeSin (x+Math.PI/2);
    }

    public static final double tan(final double x){
        switch (method){
            case FAST  : return fastTan(x);
            case CODE  : return codeTan(x);
            case SYSTEM: return Math.tan(x);
        }
        return Double.NaN;
    }

    public static final double fastTan(final double x){
        return fastSin(x)/fastCos(x);
    }

    public static final double codeTan(final double x){
        return codeSin(x)/codeCos(x);
    }

    public static final long phi(long n){
    	if (n<1)
    		return 0;
    	long erg =0;
    	for (long i=2;i<n;i++)
    		if (ggtUnchecked(n,i)==1)
    			erg++;
    	return erg;
    }

	public static final long ggt(long x, long y){
    	if (y < 0)
    		y = -y;
    	if (x==0)
    		return y;
    	if (x < 0)
    		x = -x;
    	if (y==0)
    		return x;
    	return ggtUnchecked(x,y);
	}

	/**
	 * Ggt ohne Sonderbehandlung
	 * Warning: Make sure that x>0 and y>0
	 * @param x
	 * @param y
	 * @return
	 */
	public static final long ggtUnchecked(long x, long y){
    	byte d=0;
    	while ((x & 1l) == 0 && (y & 1l)==0){
    		d++;
    		x>>=1;
    		y>>=1;
    	}
		while((x & 1l) == 0) x>>=1;
		while((y & 1l) == 0) y>>=1;
		while(true){
			if(x < y){
				if ((y-=x) == 0)
					return x<<d;
				while(((y>>=1) & 1l) == 0);
			}else{
				if ((x-=y) == 0)
					return y<<d;
				while(((x>>=1) & 1l) == 0);
			}
		}
	}

    public static final long ggt2 (long a, long b){
    	if (a < 0)
    		a = -a;
    	if (b < 0)
    		b = -b;
    	if (a==0)
    		return b;
        while ((b%=a)!=0)
            if ((a%=b)==0)
                return b;
        return a;
    }


    public static final long kgv (long x, long y){
    	if (x == 0 || y == 0)
    		return 0;
    	if (x < 0)
    		x = -x;
    	if (y < 0)
    		y = -y;
    	return kgvUnchecked(x, y);
    }

    /**
     * Warning: Make sure that x>0 and y>0
     * @param x
     * @param y
     * @return
     */
    public static final long kgvUnchecked(final long x, final long y){
    	final long c = x/ggtUnchecked(x, y);
    	final long kgv = c * y;
    	return kgv / y == c ? kgv: -1;
    }

    /**
     *
     * @param n
     * @param probability
     * @param k
     * @return
     */
    public static final double binomCdf(long n, double probability, long k){
    	double erg=0;
    	double leftP=1;
    	for (long i=0;i<=k;i++){
        	final long ncr = ncr(n,i);
        	erg+= (ncr == -1 ? ncrd(n,i) : ncr)*(leftP*=probability)*pow(1-probability, n-i);
    	}
    	return erg;
    }
    /**
     *
     * @param n
     * @param probability
     * @param k
     * @return
     */
    public static final double binomPdf(long n, double probability, long k){
    	final long ncr = ncr(n,k);
    	return (ncr == -1 ? ncrd(n,k) : ncr)*pow(probability, k)*pow(1-probability, n-k);
    }

    /**
     * Berechnet den Binomialkoeffizient von zwei Zahlen
     * Gibt bei \u00DCberlauf -1 und bei falscher Eingabe -2 zur\u00FCck
     * @param n
     * @param k0
     * @return Binomialkoeffizient
     */
    public static final long ncr (final long n, long k0){
    	if (k0 < 0 || n < 0 || n < k0)
    		return -2;
    	if (n < fakCacheLong.length)
    		return fakCacheLong[(int)n]/(fakCacheLong[(int)k0]*fakCacheLong[(int)(n-k0)]);
    	final long k1;
    	if ((n>>1)<k0)
    		k0 = n-(k1 = k0);
    	else
    		k1 = n-k0;
    	long erg = 1;
    	for (int i=1;i<=k0;i++)
    		if ((erg /= i) != (erg *= k1 + i) / (k1 + i))
    			return -1;
    	return erg;
    }

    /**
     * Berechnet den Binomialkoeffizient von zwei Zahlen
     * Gibt bei \u00DCberlauf -1 und bei falscher Eingabe -2 zur\u00FCck
     * @param n
     * @param k0
     * @return Binomialkoeffizient
     */
    public static final double ncrd (final long n, long k0){
    	if (k0 < 0 || n < 0 || n < k0)
    		return Double.NaN;
    	if (n < fakCacheLong.length)
    		return fakCacheLong[(int)n]/(fakCacheLong[(int)k0]*fakCacheLong[(int)(n-k0)]);
    	if (n < fakCacheDouble.length)
    		return fakCacheDouble[(int)n]/(fakCacheDouble[(int)k0]*fakCacheDouble[(int)(n-k0)]);
    	final long k1;
    	if ((n>>1)<k0)
    		k0 = n-(k1 = k0);
    	else
    		k1 = n-k0;
    	double erg = 1;
    	for (int i=1;i<=k0;i++)
    		erg*=(k1+i)/i;
    	return erg;
    }

    public static final long npr(long n, long k){
    	if (n < 0 || k < 0 || n < k)
    		return -2;
    	if (n < fakCacheLong.length)
    		return fakCacheLong[(int)n]/fakCacheLong[(int)(n-k)];
    	if (n<(k<<1))
    		k = n-k;
    	long erg = 1;
    	for (long i=k;i<n;i++)
    		if (erg != (erg *= i)/i)
    			return -1;
    	return erg;
    }

    public static final double nprd(long n, long k){
    	if (n < 0 || k < 0 || n < k)
    		return Double.NaN;
    	if (n < fakCacheLong.length)
    		return fakCacheLong[(int)n]/fakCacheLong[(int)(n-k)];
    	if (n < fakCacheDouble.length)
    		return fakCacheDouble[(int)n]/fakCacheDouble[(int)(n-k)];
    	if (n<(k<<1))
    		k = n-k;
    	double erg = 1;
    	for (long i=k;i<n;i++)
    		if (erg != (erg *= i)/i)
    			return -1;
    	return erg;
    }

    public static final double sqrt(final double n){
        if(n<0)
            return Double.NaN;
        if(n==0)
            return 0;
        double erg=n;
        for(int i=1;i!=11;i++)
            erg=(erg*erg+n)/(erg+erg);
        return erg;
    }

    public static final long sqrt(final long n){
    	if(n<0)
    		return -1;
    	if (n==0)
    		return 0;
    	long erg = n;
    	long oldErg=Long.MAX_VALUE;
        do {
        	oldErg = erg;
          	erg=((erg*erg+n)/erg)>>1;
        }while(erg < oldErg);
    	return erg*erg == n ? erg : -1;
    }

    public static double rt(double y, int n){
    	if(n<=0)
    		return Double.NaN;
    	if(y==0)
    		return 0;
    	double erg=y;
    	for(int i=1;i!=200;i++)
    	  	erg=((n-1)*pow(erg,n)+y)/(n*pow(erg,n-1));
    	return erg;
    }

    public static final long factLong (final long n){
    	if (n<0 || n > fakCacheLong.length)
    		return -1;
    	return fakCacheLong[(int)n];
    }

    public static final Number fak (final long n){
    	if (n<0)
    		return Double.NaN;
    	if (n > fakCacheDouble.length)
    		return Double.POSITIVE_INFINITY;
    	final int value = (int)n;
        if (value < fakCacheLong.length)
            return fakCacheLong[value];
        if (value < fakCacheDouble.length)
            return fakCacheDouble[value];
        return Double.POSITIVE_INFINITY;
    }

    public static final boolean additionOverflowTest(final long a, final long b, final long erg){
    	return (a<=0||b<=0||erg>=a)&&(a>=0||b>=0||erg<=a);
    }

    public static final boolean subtractionOverflowTest(final long a, final long b, final long erg){
    	return (a<=0||b>=0||erg>=a)&&(a>=0||b<=0||erg<=a);
    }

    public static final boolean multiplicationOverflowTest(final long a, final long b, final long erg){
    	return b==0 ? true : erg / b == a;
    }

    public static final Number pow2(long x, long exp){
    	if (exp < 3){
        	if (exp < 0)
        		return 1/pow(x,-exp).doubleValue();
        	if (exp == 0)
        		return 1l;
        	if (exp == 1)
        		return x;
        	if (exp == 2){
        		if (x < -Integer.MAX_VALUE || x > Integer.MAX_VALUE)
        			return (double)x * (double) x;
    			return x*x;
        	}
    	}
    	if (x == 0)
    		return 0l;
    	long res = 1;
    	if (x < 0){
			x =-x;
    		if ((exp & 1) == 1)
        		res = -1;
    	}

    	while (true){
    		long nextErg;
    		if ((exp & 1) == 1){
    			if(res != (nextErg = res * x)/x)
    				break;
    		}else{
    			nextErg = res;
    		}
    		if ((exp >>= 1) == 0)
    			return nextErg;
    		if (x > Integer.MAX_VALUE)
    			break;
    		x *= x;
    		res = nextErg;
    	}

    	double ergd = res;
    	double xd = x;
    	while (exp != 0){
    		if ((exp & 1) == 1)
    			ergd *= xd;
    		xd *= xd;
    		exp >>= 1;
    	}
    	return ergd;
    }

    public static final Number pow(final long x, final long exp){
    	if (exp < 3){
        	if (exp < 0)
        		return 1/pow(x,-exp).doubleValue();
        	if (exp == 0)
        		return 1l;
        	if (exp == 1)
        		return x;
        	if (exp == 2){
        		if (x < -Integer.MAX_VALUE || x > Integer.MAX_VALUE)
        			return (double)x * (double) x;
    			return x*x;
        	}
    	}
    	if (x == 0)
    		return 0l;
    	long stelle=-1;
    	while ((exp >> ++stelle)!=0);
    	stelle = 1 << (stelle-1);
    	long ergl = x ;
    	/*Laeuft manchmal endlos*/
    	while (true){
    		long nextNumber = ergl;
    		if ((stelle>>=1)==0)
    			return ergl;
    		if (nextNumber > Integer.MAX_VALUE || nextNumber < -Integer.MAX_VALUE)
    			break;
    		nextNumber *= nextNumber;
			if ((exp&stelle)!=0 && nextNumber != (nextNumber *= x)/x)
				break;
    		ergl=nextNumber;
    	}
    	double ergd = ergl;
    	do
    		ergd*=(exp&stelle)==0 ? ergd : ergd*x;
    	while ((stelle>>=1)!=0);
    	return ergd;
    }

    public static final double pow (double x, long exp){
    	if (exp < 0){
    		exp = -exp;
    		x = 1/x;
    	}
    	if (exp < 3){
    		if (exp == Long.MIN_VALUE)
    			return Math.pow(x, exp);
        	if (exp == 0)
        		return Double.isNaN(x) ? Double.NaN : 1;
        	if (exp == 1)
        		return x;
        	if (exp == 2)
        		return x*x;
    	}
    	long stelle=-1;
    	while ((exp >> ++stelle)!=0);
    	stelle = 1 << stelle-1;
    	double erg = x;
    	while ((stelle>>=1)!=0)
    		erg*=(exp&stelle)==0 ? erg : erg*x;
    	return erg;
    }

    public static final float pow (float x, long exp){
    	if (exp < 0){
    		exp = -exp;
    		x = 1/x;
    	}
    	if (exp < 3){
    		if (exp == Long.MIN_VALUE)
    			return (float)Math.pow(x, exp);
        	if (exp == 0)
        		return Float.isNaN(x) ? Float.NaN : 1;
        	if (exp == 1)
        		return x;
        	if (exp == 2)
        		return x*x;
    	}
    	long stelle=-1;
    	while ((exp >> ++stelle)!=0);
    	stelle = 1 << stelle-1;
    	float res = x;
    	while ((stelle>>=1)!=0)
    		res*=(exp&stelle)==0 ? res : res*x;
    	return res;
    }

    public static int toRREF(double[][] m) {
        if (m.length == 0)
        	return 0;
        final int rowCount = m.length, columnCount = m[0].length;

        for (int r = 0, lead = 0, i=0; r < rowCount && lead < columnCount; i=++r, lead++) {
            while (m[i][lead] == 0) {
                if (++i == rowCount) {
                    if (++lead == columnCount)
                        return m.length;
                    i = r;
                }
            }
            final double rowr[] = m[i];
            m[i] = m[r];
            m[r] = rowr;

            if (rowr[lead] != 0){ArrayUtil.mult(rowr, 0, columnCount, 1/rowr[lead]);}
            for (int k = 0; k < rowCount; k++) {
                if (k != r){ArrayUtil.multAdd(rowr, 0, columnCount, m[k], 0, -m[k][lead]);}
            }
        }
        return m.length;
    }

    public static int toRREF(float[][] m) {
        if (m.length == 0)
        	return 0;
        final int rowCount = m.length, columnCount = m[0].length;

        for (int r = 0, lead = 0, i=0; r < rowCount && lead < columnCount; i=++r, lead++) {
            while (m[i][lead] == 0) {
                if (++i == rowCount) {
                    if (++lead == columnCount)
                        return m.length;
                    i = r;
                }
            }
            final float rowr[] = m[i];
            m[i] = m[r];
            m[r] = rowr;

            if (rowr[lead] != 0){ArrayUtil.mult(rowr, 0, columnCount, 1/rowr[lead]);}
            for (int k = 0; k < rowCount; k++) {
                if (k != r){ArrayUtil.multAdd(rowr, 0, columnCount, m[k], 0, -m[k][lead]);}
            }
        }
        return m.length;
    }

    public static int toRREF(double[] m, int numRows) {
        if (m.length == 0) {return 0;}
        final int columnCount = m.length / numRows;

        for (int r = 0, lead = 0, i=0; r < m.length && lead < columnCount; i=(r += columnCount), lead++) {
            while (m[i + lead] == 0) {
                if ((i += columnCount) == m.length) {
                    if (++lead == columnCount) {return numRows;}
                    i = r;
                }
            }
            ArrayUtil.swap(m, i, r, columnCount);
            if (m[r + lead] != 0){ArrayUtil.mult(m, r, r + columnCount, 1./m[r + lead]);}
            for (int k = 0; k < m.length; k+=columnCount) {
                if (k != r){ArrayUtil.multAdd(m, r, r + columnCount, m, k, -m[k + lead]);}
            }
        }
        return numRows;
    }

    public static final long powMod (long x, long exp, long mod){
        if(exp<=0)
            return 1;
    	if (exp == 1)
    		return x;
    	int stelle=-1;
    	for (int i=1;i<=exp;i+=i)
    		stelle++;
    	long erg = x;
    	while (stelle>0)
    		erg=erg*(((exp>>--stelle)&1)==0 ? erg : erg*x)%mod;
    	return erg;
    }

    public static final double abs(double x){return x > 0 ? x : -x;}

    public static final long abs(long x){return x > 0 ? x : -x;}

    public static final long[] primeFactors(long n){
    	long erg[] = new long[1];
    	int count = 0;
    	for (long i=2;n>1;i++){
    		long testNumber = i <= primeCache.length ? primeCache[(int)i] : primeCache[primeCache.length-1]+i-primeCache.length;
    		long result = n/testNumber;
    		while (result*testNumber==n){
    			if (erg.length == count)
    				erg = Arrays.copyOf(erg, erg.length*2);
    			erg[count++]=i;
    			n = result;
    		}
    	}
    	return Arrays.copyOf(erg, count);
    }

    public static final double ln(final double x){
        if(x<=0)
        	return Double.NaN;
        double add = (x-1)/(x+1), erg = add;
        final double xx = add*add;
        final int maxi = (int)x+10;
        for(int i=3;i<maxi;i+=2)
            erg += (add *= xx) / i;
        return erg * 2;
    }

    public static long getFibunacciNumber(int index){
    	return fibunacciCache[index];
    }

	public static long getPrime(int index) {
		return primeCache[index];
	}

	public static int max(float[] positions) {
		int max = 0;
		for (int i = 1; i < positions.length; ++i)
		{
			if (positions[i] > positions[max])
			{
				max = i;
			}
		}
		return max;
	}
	public static int argMin(float[] positions) {
		int min = 0;
		for (int i = 1; i < positions.length; ++i)
		{
			if (positions[i] < positions[min])
			{
				min = i;
			}
		}
		return min;
	}

	public static abstract class Optimizer implements Runnable, DoubleList
	{
		private double lowerBound[];
		private double upperBound[];
		private double data[];
		private double tmp[];
		private double diff[];
		private final int length;
		private int maxIter = 20;
		double eps = 1e-10;
		boolean multithreaded = true;
		int randomEvaluations = 1000;

		public void setRandomEvaluations(int randomEvaluations)
		{
			this.randomEvaluations = randomEvaluations;
		}

		public void setMultithreaded(boolean multithreaded)
		{
			this.multithreaded = multithreaded;
		}

		public Optimizer(int length)
		{
			lowerBound = new double[length];
			upperBound = new double[length];
			data = new double[length];
			tmp = data.clone();
			diff = new double[tmp.length];
			this.length = tmp.length;
		}

		public Optimizer(double init[])
		{
			data = init.clone();
			tmp = data.clone();
			diff = new double[tmp.length];
			lowerBound = new double[tmp.length];
			upperBound = new double[tmp.length];
			Arrays.fill(lowerBound, Double.NEGATIVE_INFINITY);
			Arrays.fill(upperBound, Double.POSITIVE_INFINITY);
			this.length = tmp.length;
		}

		public void setBound(int index, double low, double up)
		{
			lowerBound[index] = low;
			upperBound[index] = up;
		}

		@Override
		public int size()
		{
			return length;
		}

		@Override
		public double getD(int index)
		{
			return data[index];
		}

		@Override
		public void setElem(int index, double value)
		{
			data[index] = value;
		}

		public abstract double func(double data[]);

		volatile double min;
		@Override
		public void run()
		{
			min = func(data);
			if (Double.isNaN(min))
			{
				throw new RuntimeException("NaN occured");
			}
			/*if (multithreaded)
			{
				final ThreadPool.ThreadLocal<double[]> tmpdat = DataHandler.runnableRunner.new ThreadLocal<>();
				DataHandler.runnableRunner.runParallel(new ThreadPool.ParallelRangeRunnable(){

					@Override
					public void run(int from, int to) {
						double tmp[] = tmpdat.get();
						if (tmp == null)
						{
							tmpdat.set(tmp = new double[length]);
						}
						for (int attempt= from; attempt < to; ++attempt)
						{
							for (int i = 0; i < length; ++i)
							{
								tmp[i] = Math.random() * (upperBound[i] - lowerBound[i]) + lowerBound[i];
							}
							double current = func(tmp);
							synchronized(Optimizer.this)
							{
								if (current < min)
								{
									min = current;
									System.arraycopy(tmp, 0, data, 0, length);
								}
							}
						}
					}

					@Override
					public void finished() {}
				}, "Optimize", null, 0, randomEvaluations, 10, true);
			}
			else
			{*/
				for (int attempt= 0; attempt < randomEvaluations; ++attempt)
				{
					for (int i = 0; i < length; ++i)
					{
						tmp[i] = Math.random() * (upperBound[i] - lowerBound[i]) + lowerBound[i];
					}
					double current = func(tmp);
					synchronized(Optimizer.this)
					{
						if (current < min)
						{
							min = current;
							System.arraycopy(tmp, 0, data, 0, length);
						}
					}
				}
			//}
			System.out.println("Premin :" + min);
			min = func(data);
			if (Double.isNaN(min))
			{
				throw new RuntimeException("NaN occured");
			}
			System.out.println("Premin2:" + min);
			for(int iteration = 0; iteration < maxIter; ++iteration)
			{
				double scalar = 0.001;
				System.arraycopy(data, 0, tmp, 0, length);
				double diffSum = 0;
				for (int i = 0; i < data.length; ++i)
				{
					tmp[i] = data[i] - scalar;
					diff[i] = func(tmp) - min;
					diffSum += diff[i] * diff[i];
					tmp[i] = data[i];
				}
				scalar /= Math.sqrt(diffSum);
				if (diffSum < eps)
				{
					break;
				}
				while(true)
				{
					DoubleMatrixUtil.addTo(data, diff, scalar, tmp);
					double current = func(tmp);
					if (Double.isNaN(current))
					{
						throw new RuntimeException("NaN occured");
					}
					if(current <= min)
					{
						min = current;
						break;
					}
					if (scalar == 0)
					{
						throw new RuntimeException("Wrong minimum value : " + current + '<' + '=' + min);
					}
					scalar /= 2;
				}
				while (true)
				{
					DoubleMatrixUtil.addTo(data, diff, scalar * 2, tmp);
					double current = func(tmp);
					if (Double.isNaN(current))
					{
						throw new RuntimeException("NaN occured");
					}
					if (current > min)
					{
						DoubleMatrixUtil.addTo(data, diff, scalar, data);
						break;
					}
					scalar *= 2;
					min = current;
				}
			}
			System.out.println(min);
		}
	}

	/**
	 * Searches a value in a monotone increasing function
	 * @param min
	 * @param max
	 * @param value
	 * @param eps
	 * @param df
	 * @return
	 */
	public static final double binarySearch(double min, double max, double value, double eps, DoubleFunctionDouble df)
	{
		while (Math.abs(max - min) > eps)
		{
			double arg = (min + max) * 0.5;
			double res = df.apply(arg);
			if (res < value)
			{
				min = arg;
			}
			else if (res > value)
			{
				max = arg;
			}
		}
		return (min + max) * 0.5;
	}

	public static final int propabilityRound(double density)
	{
		int result = (int)density;
		if (density >= 0)
		{
			if (Math.random() < density - result)
			{
				++result;
			}
		}
		else
		{
			if (Math.random() < result - density)
			{
				--result;
			}
		}
		return result;
	}

	public static double modToZeroOne(double d) {
		d = Math.IEEEremainder(d, 1);
		return d < 0 ? d + 1 : d;
	}

	public static final double clamp(double val, double low, double high)
	{
		return val < low ? low : val > high ? high : val;
	}

	public static final int signum(double d) {return d > 0 ? 1 : d < 0 ? -1 : 0;}

}
