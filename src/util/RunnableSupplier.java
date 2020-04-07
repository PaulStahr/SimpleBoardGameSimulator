package util;

import java.util.function.Supplier;

public abstract class RunnableSupplier<T> implements Supplier<T>, Runnable {
	private T o = null;
	
	@Override
	public abstract void run();

	@Override
	public final T get() {
		return o;
	}
	
	public final void set(T o)
	{
		this.o = o;
	}

}
