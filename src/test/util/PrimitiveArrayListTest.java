package test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import util.data.ByteArrayList;
import util.data.DoubleArrayList;
import util.data.IntegerArrayList;

public class PrimitiveArrayListTest {
    private class IsEqualPredicate<T> implements Predicate<T>
    {
        private final T elem;
        
        public IsEqualPredicate(T elem){this.elem = elem;}

        @Override
        public boolean test(T t) {return elem.equals(t);}
        
    }
    
    public <E> void testList(List<E> l, final E e0, final E e1, final E e2) {
        l.add(e0);
        l.add(e1);
        l.add(e0);
        assert(l.size() == 3);
        assert(l.contains(e1));
        assert(l.removeIf(new IsEqualPredicate<E>(e1)));
        assert(l.size() == 2);
        assert(l.get(0).equals(e0));
        assert(!l.contains(e1));
        assert(!l.removeIf(new IsEqualPredicate<E>(e1)));
    }

    @Test
    public void testByteArrayList() {testList(new ByteArrayList(), (byte)7, (byte)12, (byte)64);}
    
    @Test
    public void testIntegerArrayList(){testList(new IntegerArrayList(), 7, 23, 64);}
    
    @Test
    public void testDoubleArrayList() {testList(new DoubleArrayList(), 7., 12., 64.);}
    
    @Test
    public void testGenericArrayList() {testList(new ArrayList<Integer>(), 7, 12, 64);}
}
