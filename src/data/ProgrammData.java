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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ArrayTools;

public class ProgrammData {
    public static class Version{
        public final Date date;
        public final String code;
        public final String title;
        public final String compatibility;
        public final List<String> notices;

        public Version(Date date, String code, String title, String compatibility, List<String> notices) {
            this.date = date;
            this.code = code;
            this.title = title;
            this.compatibility = compatibility;
            this.notices = notices;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ProgrammData.class);
	public static final String name = new String("Simple Online Board-Game Simulator");
	private static final String version = new String("1.0.0");
	public static final List<String> authors = ArrayTools.unmodifiableList(new String[]{"Paul Stahr","Florian Seiffarth"});
	public static final String jarDirectory;
    private static WeakReference<Element> localVersion;
    private static WeakReference<List<Version> > localVersionList;
    public static enum Branch{
        DEV, MASTER;
        private WeakReference<Element> remoteVersion;
        private WeakReference<List<Version> > remoteVersionList;
    }

	public static String getVersion(){return version;}

	static{
		/*long authorHash = 0;
		for (int i=0;i<authors.size();i++)
			authorHash += authors.get(i).hashCode();
		authorHash %= name.hashCode();
		authorHash *= version.hashCode();*/
 		jarDirectory = ProgrammData.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	private static void parseVersionXml(Element elem, ArrayList<Version> v)
	{
        for (Element version : elem.getChildren("version"))
        {
            try {
                ArrayList<String> notices = new ArrayList<>();
                for (Element notice : version.getChildren("notice"))
                {
                    notices.add(notice.getValue());
                }
                v.add(new Version((new SimpleDateFormat("yyyy.MM.dd")).parse(version.getAttributeValue("date")), version.getAttributeValue("code"), version.getAttributeValue("title"), version.getAttributeValue("compatibility"), ArrayTools.unmodifiableList(notices.toArray(new String[notices.size()]))));
            } catch (ParseException e) {
                logger.error("Couldn't parse Version", e);
            }
        }
	}

	public static List<Version> getRemoteVersionList(Branch b){
	    Element elem = getRemoteVersion(b);
        List<Version> result = b.remoteVersionList == null ? null : b.remoteVersionList.get();
        if (result == null)
        {
            ArrayList<Version> v = new ArrayList<>();
            parseVersionXml(elem, v);
            result = ArrayTools.unmodifiableList(v.toArray(new Version[v.size()]));
            b.remoteVersionList = new WeakReference<List<Version>>(result);
        }
        return result;
	}

	public static List<Version> getLocalVersionList(){
	    Element elem = getLocalVersion();
	    List<Version> result = localVersionList == null ? null : localVersionList.get();
	    if (result == null)
	    {
	        ArrayList<Version> v = new ArrayList<>();
	        parseVersionXml(elem, v);
	        result = ArrayTools.unmodifiableList(v.toArray(new Version[v.size()]));
    	    localVersionList = new WeakReference<List<Version>>(result);
	    }
	    return result;
	}

	private static Element getRemoteVersion(Branch b) {
        Element root = b.remoteVersion == null ? null : b.remoteVersion.get();
        if (root == null) {
            try {
                String address;
                switch (b) {
                    case DEV:   address = "https://raw.githubusercontent.com/PaulStahr/SimpleBoardGameSimulator/dev/src/resources/version.xml";break;
                    case MASTER:address = "https://raw.githubusercontent.com/PaulStahr/SimpleBoardGameSimulator/master/src/resources/version.xml";break;
                    default:    throw new IllegalArgumentException();
                }
                InputStream input = new URL(address).openStream();
                Document doc = new SAXBuilder().build(input);
                input.close();
                root = doc.getRootElement();
                b.remoteVersion = new WeakReference<Element>(root);
            } catch (JDOMException e) {
                logger.error("Couldn't read version",e);
            } catch (IOException e) {
                logger.error("Couldn't read version",e);
            }
        }
        return root;
    }

	private static Element getLocalVersion() {
	    Element root = localVersion == null ? null : localVersion.get();
	    if (root == null) {
	        try {
                Document doc = new SAXBuilder().build(DataHandler.getResourceAsStream("version.xml"));
                root = doc.getRootElement();
                localVersion = new WeakReference<Element>(root);
            } catch (JDOMException e) {
                logger.error("Couldn't read version",e);
            } catch (IOException e) {
                logger.error("Couldn't read version",e);
            }
	    }
	    return root;
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

    public static Version getHighestVerion(List<Version> versionListMaster) {
        ProgrammData.Version result = null;
        long maxVersion = Long.MIN_VALUE;
        for (int i = 0; i < versionListMaster.size(); ++i)
        {
            ProgrammData.Version v = versionListMaster.get(i);
            long currentVersionNumber = ProgrammData.getLongOfVersion(v.code);
            if(currentVersionNumber > maxVersion)
            {
                result = v;
                maxVersion = currentVersionNumber;
            }
        }
        return result;
    }   
}
