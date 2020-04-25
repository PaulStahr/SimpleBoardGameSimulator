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
package data;

import java.util.List;

import util.ArrayTools;

public class ProgrammData {
	//private static final Logger logger = LoggerFactory.getLogger(ProgrammData.class);
	public static final String name = new String("Simple Online Board-Game Simulator");
	private static final String version = new String("0.0.1 beta");
	public static final List<String> authors = ArrayTools.unmodifiableList(new String[]{"Paul Stahr, Florian Seiffarth"});
	public static final String jarDirectory;
	
	public static String getVersion(){
		return version;
	}
	
	static{
		long authorHash = 0;
		for (int i=0;i<authors.size();i++)
			authorHash += authors.get(i).hashCode();
		authorHash %= name.hashCode();
		authorHash *= version.hashCode();
		/*System.out.println(authorHash);
		if (authorHash != -401399625664877541l){
			//logger.error("1234, please update or contact paul.stahr@gmx.de");
			System.exit(-1);
		}*/
		jarDirectory = ProgrammData.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
	
	public static boolean isNewer(String version){
		return getLongOfVersion(version) > getLongOfVersion(ProgrammData.version);
	}
	
    public static long getLongOfVersion(String version){
    	boolean beta = version.endsWith("beta");
    	if (beta)
    		version = version.substring(0, version.length()-5);
    	int shift=48, tmp=0;
    	long erg=0;
    	for (int i=0;i<version.length();i++){
    		final char c = version.charAt(i);
    		if (c == '_' || c == ' ' || c == '.'){
    			if (shift == -16 || tmp > Short.MAX_VALUE)
    				return -1;
    			erg |= (long)tmp << shift;
    			tmp = 0;
    			shift -=16;
    		}else if (c <= '9' && c>='0'){
    			tmp = tmp*10 + c - '0';
    		}
    	}
		if (shift == -16 || tmp > Short.MAX_VALUE)
			return -1;
		erg |= (long)tmp << shift;
    	return beta ? erg * 2 + 1 : erg * 2;
    }   
}
