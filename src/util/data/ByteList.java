package util.data;

import java.util.List;

public interface ByteList extends List<Byte>{
	public byte getB(int index);
	
	public int size();
	
	public void setElem(int index, byte value);
}
