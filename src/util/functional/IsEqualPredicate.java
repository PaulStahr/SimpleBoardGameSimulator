package util.functional;

import java.util.function.Predicate;

public class IsEqualPredicate<T> implements Predicate<T>
{
    private final T elem;
    
    public IsEqualPredicate(T elem){this.elem = elem;}

    @Override
    public boolean test(T t) {return elem.equals(t);}
}