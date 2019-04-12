package tr.com.srdc.cda2fhir.testutil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;

public class RTInvocationHandler<T> implements InvocationHandler {
	private IResourceTransformer rt;
	private List<T> cdaObjects = new ArrayList<>();

	public RTInvocationHandler(IResourceTransformer rt) {
		this.rt = rt;
	}

	public void resetObjects() {
		cdaObjects.clear();
	}

	public List<T> getObjects() {
		return Collections.unmodifiableList(cdaObjects);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object arg = args[0];
		try {
			cdaObjects.add((T) arg);
		} catch (ClassCastException e) {
		}

		return method.invoke(rt, args);
	}
}
