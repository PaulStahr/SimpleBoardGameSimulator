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

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ArrayTools;
import util.HeterogenousComparator;
import util.ListTools;
import util.StringUtils;
/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public abstract class Options
{
    private static volatile int modCount = 0;
    private static volatile int lastListener = -1;
    private static int lastFileSync = -1;
	private static final Logger logger = LoggerFactory.getLogger(Options.class);
	private static final ArrayList<Runnable> oml = new ArrayList<Runnable>();
	private static Thread thread;
	
	public static void triggerUpdates()
	{
		if (modCount != lastListener)
		{
			if (thread == null)
			{
				thread = new Thread("Option Update")
				{
					@Override
					public void run()
					{
						do{
							Options.lastListener = Options.modCount;
							synchronized(oml)
							{
								for (int i = 0; i < oml.size(); ++i)
								{
									oml.get(i).run();
								}
							}
						}while (Options.lastListener != Options.modCount);
						thread = null;
					}
				};
				thread.start();
			}
		}
	}
	
	private static final void optionsUpdated(){
		++modCount;
	}
	
	public static void set(String key, Object value)
	{
		set(root, key, value);
	}
	
	public static synchronized void set(OptionTreeNode root, String key, Object value)
	{
		OptionTreeNode otn = createNode(key, root, value);
		if (otn instanceof OptionTreeLeafBigInteger)			{((OptionTreeLeafBigInteger)otn).value 	= (BigInteger)value;}
		else if (otn instanceof OptionTreeLeafString)			{((OptionTreeLeafString)otn).value 		= (String)value;}
		else if (otn instanceof OptionTreeLeafColor)			{((OptionTreeLeafColor)otn).value 		= (Color)value;}
		else if (otn instanceof OptionTreeLeafDouble)			{((OptionTreeLeafDouble)otn).value 		= (double)value;}
		else if (otn instanceof OptionTreeLeafInteger)			{((OptionTreeLeafInteger)otn).value 	= value instanceof Integer ? (int)value : (byte)value;}
		else if (otn instanceof OptionTreeLeafBoolean)			{((OptionTreeLeafBoolean)otn).value 	= (boolean)value;}
		else if (otn instanceof OptionTreeLeafFloat)			{((OptionTreeLeafFloat)otn).value 		= (float)value;}
		else													{throw new IllegalArgumentException("missmatched type for key " + key + ':' + value.getClass().getName() + " should be " + otn.typeValue());}
		optionsUpdated();
		otn.lastModification = modCount;
	}
	
	private static void readStructure(InputStream in) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(in);
    	root = recursiveTreeCreator(doc.getRootElement());
	}
	
	private static void readTree(InputStream in) throws JDOMException, IOException
	{
		Document doc = new SAXBuilder().build(in);
    	recursiveTreeReader(doc.getRootElement(), root);
	}
	
	private static OptionTreeNode recursiveTreeCreator(Element elem)
	{
		String type = elem.getAttributeValue("type");
		String init = elem.getValue();
		OptionTreeNode toAdd = null;
		if (type == null)
		{
			toAdd = new OptionTreeInnerNode(elem.getName(), modCount);
		}
		else
		{
			switch (type)
			{
				case "void":toAdd 	= new OptionTreeInnerNode(		elem.getName(), modCount);break;
				case "string":toAdd = new OptionTreeLeafString(		elem.getName(), init, modCount);break;
				case "int":toAdd 	= new OptionTreeLeafInteger(	elem.getName(), Integer.parseInt(init), modCount);break;
				case "float":toAdd 	= new OptionTreeLeafFloat(		elem.getName(), Float.parseFloat(init), modCount);break;
				case "double":toAdd = new OptionTreeLeafDouble(		elem.getName(), Double.parseDouble(init), modCount);break;
				case "bigint":toAdd = new OptionTreeLeafBigInteger(	elem.getName(), new BigInteger(init), modCount);break;
				case "color":toAdd 	= new OptionTreeLeafColor(		elem.getName(), new Color(Integer.parseInt(init)), modCount);break;
				case "bool":toAdd 	= new OptionTreeLeafBoolean(	elem.getName(), Boolean.parseBoolean(init), modCount);break;
				default:logger.error("Type " + type + " not known");break;
			}
		}
		List<Element> children = elem.getChildren();
		if (children.size() != 0)
		{
			((OptionTreeInnerNode)toAdd).children = new OptionTreeNode[children.size()];
			for (Element child : children)
			{
				((OptionTreeInnerNode)toAdd).addChild(recursiveTreeCreator(child));
			}
		}
		return toAdd;
	}
	
	private static void recursiveTreeReader(Element elem, OptionTreeNode node)
	{
		if (node == null)
		{
			throw new NullPointerException();
		}
		String type = elem.getAttributeValue("type");
		if (!type.equals("void"))
		{
			String init = elem.getValue();
			try
			{
				switch (type)
				{
					case "string":	((OptionTreeLeafString)		node).value = init;break;
					case "int":		((OptionTreeLeafInteger)	node).value = Integer.parseInt(init);break;
					case "float":	((OptionTreeLeafFloat)		node).value = Float.parseFloat(init);break;
					case "double":	((OptionTreeLeafDouble)		node).value = Double.parseDouble(init);break;
					case "bigint":	((OptionTreeLeafBigInteger)	node).value = new BigInteger(init);break;
					case "color":	((OptionTreeLeafColor)		node).value = new Color(Integer.parseInt(init));break;
					case "bool":	((OptionTreeLeafBoolean)	node).value = Boolean.parseBoolean(init);break;
				}
			}catch (Exception e)
			{
				logger.error("Node " + elem.getName() + " has wrong type");
			}
		}
		
		for (Element child : elem.getChildren())
		{
			OptionTreeNode childNode = ((OptionTreeInnerNode)node).getChild(child.getName());
			if (childNode == null)
			{
				logger.error("Child Node " + child.getName() + " not found");
			}
			else
			{
				recursiveTreeReader(child, childNode);
			}
		}
	}
	
	private static Document getDocument()
	{
		Document doc = new Document();
		doc.addContent(recursiveXmlCreator(root));
		return doc;
	}
	
	private static Element recursiveXmlCreator(OptionTreeNode node)
	{
		Element toAdd = new Element(node.name);
		toAdd.setAttribute("type", node.typeValue());
		if (!(node instanceof OptionTreeInnerNode))
		{
			toAdd.setText(node.stringValue());
		}
		if (node instanceof OptionTreeInnerNode)
		{
			OptionTreeInnerNode inner = ((OptionTreeInnerNode)node);
			for (int i = 0; i < inner.size; ++i)
			{
				toAdd.addContent(recursiveXmlCreator(inner.children[i]));
			}
		}
		return toAdd;
	}
	
	private static final HeterogenousComparator<OptionTreeNode, String> stringTreeNodeComparator = new HeterogenousComparator<OptionTreeNode, String>()
    {
    	@Override
		public final int compare(OptionTreeNode o2, String o1) {
			return o1.compareTo(o2.name);
		}
    };
    
    private static OptionTreeNode root = createNodeInstance("options", null, modCount);
    
    static
    {
    	try
    	{
    		InputStream stream = DataHandler.getResourceAsStream("options" + '.' + "xml");
            readStructure(stream);
            stream.close();
        }
    	catch (Exception e)
    	{
        	logger.error("Problems with loading standard Options", e);
        }
    	try
    	{
	    	final File file = new File(SystemFileUtil.defaultProgramDirectory() + '/' + "options" + '.' + "xml");
		    if (!file.exists() || file.isDirectory())
		    {
		        new XMLOutputter(Format.getPrettyFormat()).output(getDocument(), new FileWriter(file));
		    }
		    else
		    {
		    	readTree(new FileInputStream(file));
		    }
    	}
    	catch (Exception e)
    	{
        	logger.error("Problems with loading Options", e);
        }    	
        Runtime.getRuntime().addShutdownHook(new Thread()
		{
        	@Override
			public void run() {
                save();
            }
		});
    }

    private Options(){}
    
    public static final int modCount()
    {
        return modCount;
    }
    
    public synchronized static void addModificationListener(Runnable runnable)
    {
    	oml.add(runnable);
    	runnable.run();
    }
    
    private static final boolean save(){
    	if (lastFileSync == modCount)
    		return true;
        try{
            final File dir = new File(SystemFileUtil.defaultProgramDirectory());
            if (!dir.exists())
                dir.mkdir();
            if (!dir.isDirectory()){
                dir.delete();
                dir.mkdir();
            }
            new XMLOutputter(Format.getPrettyFormat()).output(getDocument(), new FileWriter(dir.getPath() + '/' + "options" + '.' + "xml"));
        }catch (Exception e){
            logger.error("Can't save Options",e);
            return false;
        }
        lastFileSync = modCount;
        return true;
    }

    public static abstract class OptionTreeNode
    {
    	public final String name;
    	private int lastModification;
    	
    	private OptionTreeNode(String name, int lastModification)
    	{
    		this.name = name.intern();
    		this.lastModification = lastModification;
    	}
    	
    	public final int getLastMod()
    	{
    		return lastModification;
    	}
    	
    	public abstract String typeValue();

		public abstract String stringValue();
		
		@Override
		public String toString(){
			return name + ':' + '=' + stringValue();
		}
    }
    
    private static OptionTreeNode createNodeInstance(String name, Object value, int lastModification)
    {
    	if (value instanceof BigInteger){	return new OptionTreeLeafBigInteger(name, (BigInteger)value, lastModification);}
		if (value instanceof Boolean){		return new OptionTreeLeafBoolean(name, (Boolean)value, lastModification);}
		if (value instanceof Integer){		return new OptionTreeLeafInteger(name, (Integer)value, lastModification);}
		if (value instanceof Double){		return new OptionTreeLeafDouble(name, (Double)value, lastModification);}
		if (value instanceof Float){		return new OptionTreeLeafFloat(name, (Float)value, lastModification);}
		if (value instanceof Color){		return new OptionTreeLeafColor(name, (Color)value, lastModification);}
		if (value instanceof String){		return new OptionTreeLeafString(name, (String)value, lastModification);}
		return new OptionTreeInnerNode(name, lastModification);
    }
    
    public static class OptionTreeLeafBoolean extends OptionTreeNode
    {
    	private boolean value;
    	private OptionTreeLeafBoolean(String name, boolean init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {
			return "bool";
		}
		
		public boolean get()
		{
			return value;
		}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
    }
    
    public static class OptionTreeLeafInteger extends OptionTreeNode
    {
    	private int value;
    	private OptionTreeLeafInteger(String name, int init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {
			return "int";
		}
		
		public int get()
		{
			return value;
		}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
	}
    
    public static class OptionTreeLeafDouble extends OptionTreeNode
    {
    	private double value;
    	private OptionTreeLeafDouble(String name, double init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {
			return "double";
		}
		
		public double get()
		{
			return value;
		}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
	}
    
    public static class OptionTreeLeafFloat extends OptionTreeNode
    {
    	private float value;
    	private OptionTreeLeafFloat(String name, float init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {return "float";}
		
		public float get(){return value;}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
	}
    
    public static class OptionTreeLeafColor extends OptionTreeNode
    {
    	private Color value;
    	private OptionTreeLeafColor(String name, Color init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {return "color";}
		
		public Color get(){return value;}
		
		@Override
		public String stringValue() {
			return String.valueOf(value.getRGB());
		}
	}
    
    public static class OptionTreeLeafString extends OptionTreeNode
    {
    	private String value;
    	private OptionTreeLeafString(String name, String init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
		@Override
		public String typeValue() {
			return "string";
		}
		
		public String get()
		{
			return value;
		}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
	}
    
    public static class OptionTreeLeafBigInteger extends OptionTreeNode
    {
    	private BigInteger value;
    	private OptionTreeLeafBigInteger(String name, BigInteger init, int lastModification)
    	{
    		super(name, lastModification);
    		value = init;
    	}
    	
    	public BigInteger get()
    	{
    		return value;
    	}
    	
		@Override
		public String typeValue() {
			return "bigint";
		}
		
		@Override
		public String stringValue() {
			return String.valueOf(value);
		}
	}
    
    public static class OptionTreeInnerNode extends OptionTreeNode
    {
    	private OptionTreeNode children[] = new OptionTreeNode[0];
    	private int size = 0;
    	private OptionTreeInnerNode(String name, int lastModification)
    	{
    		super(name, lastModification);
    	}
    	
		@Override
    	public String typeValue() {
    		return "void";
    	}
    	
		@Override
    	public String stringValue() {
    		return "void";
    	}
		
		public final OptionTreeNode getChild(String name)
    	{//TODO no alloc
			int index = ListTools.binarySearch(children, 0, size, name, stringTreeNodeComparator);
			if (index < 0)
			{
				return null;
			}
    		return children[index];
    	}
		
		public final OptionTreeInnerNode getInnerChild(String name)
    	{
			OptionTreeNode node = getChild(name);
			if (node instanceof OptionTreeInnerNode)
			{
				return (OptionTreeInnerNode)node;
			}
    		return null;
    	}
		
		private OptionTreeNode createChild(String name, Object value)
		{
			int index = ListTools.binarySearch(children, 0, size, name, stringTreeNodeComparator);
			if (index < 0)
			{
				OptionTreeNode tmp = createNodeInstance(name, value, modCount);
				children = ArrayTools.add(children, size, -index - 1, tmp);
				++size;
				return tmp;
			}
			return children[index];
		}

		
		private void addChild(OptionTreeNode child)
		{
			int index = ListTools.binarySearch(children, 0, size, child.name, stringTreeNodeComparator);
			if (index < 0)
			{
				children = ArrayTools.add(children, size, -index - 1, child);
				++size;
			}
		}

    }
    
    private static final OptionTreeNode createNode(final CharSequence name, OptionTreeNode root, final Object value)
    {
    	return createNode(new StringUtils().split(name, 0, name.length(), '.'), root, value);
    }
    
    public static final OptionTreeNode getNode(final CharSequence name)
    {
   		return getNode(new StringUtils().split(name, 0, name.length(), '.'));
    }

    public static final OptionTreeNode getNode(OptionTreeNode root, final CharSequence name)
    {
   		return getNode(root,new StringUtils().split(name, 0, name.length(), '.'));
    }

    public static final OptionTreeInnerNode getInnerNode(final CharSequence name)
    {
   		OptionTreeNode node = getNode(new StringUtils().split(name, 0, name.length(), '.'));
   		if (node instanceof OptionTreeInnerNode)
   		{
   			return (OptionTreeInnerNode)node;
   		}
   		return null;
    }

    public static final OptionTreeInnerNode getInnerNode(OptionTreeNode root, final CharSequence name)
    {
   		OptionTreeNode node = getNode(root, new StringUtils().split(name, 0, name.length(), '.'));
   		if (node instanceof OptionTreeInnerNode)
   		{
   			return (OptionTreeInnerNode)node;
   		}
   		return null;
    }

    private static final OptionTreeNode createNode(String str[], OptionTreeNode root, Object value)
    {
    	OptionTreeNode otn = root;
    	for (int i = 0; i < str.length; ++i)
    	{
    		if (otn == null)
    		{
    			return null;
    		}
    		if (i == str.length - 1)
    		{
    			otn = ((OptionTreeInnerNode)otn).createChild(str[i], value);
    		}
    		else
    		{
    			otn = ((OptionTreeInnerNode)otn).createChild(str[i], null);
    		}
    	}
   		return otn;   
   	}
    
    private static final OptionTreeNode getNode(String str[])
    {
    	return getNode(root, str);
    }

    private static final OptionTreeNode getNode(OptionTreeNode otn, String str[])
    {
    	for (int i = 0; i < str.length; ++i)
    	{
    		if (otn == null || !(otn instanceof OptionTreeInnerNode))
    		{
    			return null;
    		}
    		otn = ((OptionTreeInnerNode)otn).getChild(str[i]);
    	}
   		return otn;
    }

    /*private static final OptionTreeNode getNode(OptionTreeNode otn, Iterator<String> str)
    {
    	while (str.hasNext())
    	{
    		if (otn == null || !(otn instanceof OptionTreeInnerNode))
    		{
    			return null;
    		}
    		otn = ((OptionTreeInnerNode)otn).getChild(str.next());
    	}
   		return otn;
    }*/

    public static final Boolean getBoolean (final CharSequence name){
    	return getBoolean(name, null);
    }
    
    public static final Boolean getBoolean (final CharSequence name, Boolean def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafBoolean)
    	{
    		return ((OptionTreeLeafBoolean)otn).value;
    	}
    	return def;
    }

    public static final Boolean getBoolean (OptionTreeNode root, final CharSequence name){
    	return getBoolean(root, name, null);
    }
    
    public static final Boolean getBoolean (OptionTreeNode root, final CharSequence name, Boolean def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafBoolean)
    	{
    		return ((OptionTreeLeafBoolean)otn).value;
    	}
    	return def;
    }

    public static final Integer getInteger (final CharSequence name)
    {
    	return getInteger(name, null);
    }
    
    public static final Integer getInteger (final String[] name, Integer def)
    {
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return ((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }
    
    public static final Byte getByte (final String[] name, Byte def)
    {
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return (byte)((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }
    
    public static final Integer getInteger (final CharSequence name, Integer def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return ((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }    

    public static final Byte getByte (OptionTreeNode root, final CharSequence name, Byte def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return (byte)((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }

    public static final Byte getByte (final CharSequence name, Byte def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return (byte)((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }
    
    public static final Integer getInteger (OptionTreeNode root, final CharSequence name, Integer def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return ((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }
    
    public static final Integer getInteger (OptionTreeNode root, final CharSequence name){
    	return getInteger(root, name, null);
    }
    
    public static final Float getFloat (final CharSequence name){
    	return getFloat(name, null);
    }
    
    public static final Float getFloat (final CharSequence name, Float def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafFloat)
    	{
    		return ((OptionTreeLeafFloat)otn).value;
    	}
    	return def;
    }

    public static final Float getFloat (OptionTreeInnerNode root, final CharSequence name){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafFloat)
    	{
    		return ((OptionTreeLeafFloat)otn).value;
    	}
    	return null;
    }

    public static final Double getDouble (final CharSequence name){
    	return getDouble(name, null);
    }
    
   public static final Double getDouble (final CharSequence name, Double def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafDouble)
    	{
    		return ((OptionTreeLeafDouble)otn).value;
    	}
    	return def;
    }

   	public static final Color getColor (final CharSequence name){
   		return getColor(name, null);
   	}
   
    public static final Color getColor (final CharSequence name, Color def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafColor)
    	{
    		return ((OptionTreeLeafColor)otn).value;
    	}
    	return def;
    }

    public static final Color getColor (OptionTreeNode root, final CharSequence name, Color def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafColor)
    	{
    		return ((OptionTreeLeafColor)otn).value;
    	}
    	return def;
    }

    public static final Float getFloat (OptionTreeNode root, final CharSequence name, Float def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafFloat)
    	{
    		return ((OptionTreeLeafFloat)otn).value;
    	}
    	return def;
    }

    public static final Long getLong (final CharSequence name){
    	return getLong(name, null);
    }

    public static final Long getLong(final CharSequence name, Long def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafInteger)
    	{
    		return (long)((OptionTreeLeafInteger)otn).value;
    	}
    	return def;
    }

    public static final String getString (final CharSequence name){
    	return getString(name, null);
    }

    public static final String getString(OptionTreeNode root, final CharSequence name, String def){
    	OptionTreeNode otn = getNode(root, name);
    	if (otn instanceof OptionTreeLeafString)
    	{
    		return ((OptionTreeLeafString)otn).value;
    	}
    	return def;
    }
 
    public static final String getString(final CharSequence name, String def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafString)
    	{
    		return ((OptionTreeLeafString)otn).value;
    	}
    	return def;
    }
 
    public static final BigInteger getBigInteger(final CharSequence name, BigInteger def){
    	OptionTreeNode otn = getNode(name);
    	if (otn instanceof OptionTreeLeafBigInteger)
    	{
    		return ((OptionTreeLeafBigInteger)otn).value;
    	}
    	return def;
    }
 
    public static final BigInteger getBigInteger (final CharSequence name){
    	return getBigInteger (name, null);
    }
}
