package util.jframe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.JFrameUtils;
import util.RunnableSupplier;
import util.data.UniqueObjects;

public class JComponentSingletonInstantiator<T> implements Supplier<T>, ActionListener{
	private static final Logger logger = LoggerFactory.getLogger(JComponentSingletonInstantiator.class);
	private WeakReference<T> ref;
	private final Class<?> cl;
	
	public JComponentSingletonInstantiator(Class<?> cl){this.cl = cl;}
	
	@Override
	@SuppressWarnings("unchecked")
	public final T get()
	{
		if (ref != null)
		{
			T o = ref.get();
			if (o != null)
			{
				return o;
			}
		}
		RunnableSupplier<T> sup = new RunnableSupplier<T>() {
			@Override
			public void run()
			{
				try {
					set((T)cl.getConstructor(UniqueObjects.EMPTY_CLASS_ARRAY).newInstance(UniqueObjects.EMPTY_OJECT_ARRAY));
				} catch (IllegalAccessException e1) {
					logger.error("Can't instantiate Window", e1);
				} catch (IllegalArgumentException e1) {
					logger.error("Can't instantiate Window", e1);
				} catch (InvocationTargetException e1) {
					logger.error("Can't instantiate Window", e1);
				} catch (NoSuchMethodException e1) {
					logger.error("Can't instantiate Window", e1);
				} catch (SecurityException e1) {
					logger.error("Can't instantiate Window", e1);
				} catch (InstantiationException e1) {
					logger.error("Can't instantiate Window", e1);
				}
			}
		};
		JFrameUtils.runByDispatcherAndWaitNoExcept(sup);
		T o = sup.get();
		ref = new WeakReference<T>(o);
    	return o;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			((JFrame)get()).setVisible(true);
			//((JFrame)(cl.getMethod("getInstance", DataHandler.EMPTY_CLASS_ARRAY).invoke(null, DataHandler.EMPTY_OJECT_ARRAY))).setVisible(true);
		} catch (IllegalArgumentException e1) {
			logger.error("Can't instantiate Window", e1);
		}
	}
}
