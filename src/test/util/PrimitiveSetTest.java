package test.util;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import util.data.SortedIntegerArrayList;
import util.functional.IsEqualPredicate;

public class PrimitiveSetTest {
    public <E> void testSet(Set<E> s, final E e0, final E e1, final E e2) {
        s.add(e0);
        s.add(e1);
        s.add(e0);
        assertEquals(s.size(), 2);
        assert(s.contains(e1));
        assert(s.removeIf(new IsEqualPredicate<E>(e1)));
        assertEquals(s.size(), 1);
        assert(!s.contains(e1));
        assert(!s.removeIf(new IsEqualPredicate<E>(e1)));
    }

    @Test
    public void testSortedIntegerList() {testSet(new SortedIntegerArrayList(), 7, 12, 64);}
    
    @Test
    public void testHashSet(){testSet(new HashSet<Integer>(), 7, 12, 64);}
}
