package util.data;

import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;

public class UniqueObjects {
	public static final int EMPTY_INT_ARRAY[] = new int[0];
	public static final String EMPTY_STRING_ARRAY[] = new String[0];
	public static final UnmodifiableArrayList<String> EMPTY_STRING_LIST = ArrayTools.unmodifiableList(EMPTY_STRING_ARRAY);
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
}
