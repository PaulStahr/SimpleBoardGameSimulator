package util.data;

import java.util.Set;

public interface SortedIntegerList extends IntegerList, Set<Integer> {

	boolean hasMatch(int[] data, int begin, int end);

}
