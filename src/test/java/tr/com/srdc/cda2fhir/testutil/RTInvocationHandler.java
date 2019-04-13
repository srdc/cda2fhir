package tr.com.srdc.cda2fhir.testutil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;

public class RTInvocationHandler implements InvocationHandler {
	private IResourceTransformer rt;

	private Map<String, List<Object>> cdaObjects;

	public RTInvocationHandler(IResourceTransformer rt) {
		this.rt = rt;
		this.cdaObjects = new HashMap<>();
	}

	public void addMethod(String methodName) {
		cdaObjects.put(methodName, new ArrayList<Object>());
	}

	public void resetObjects() {
		cdaObjects.entrySet().forEach((entry) -> {
			entry.getValue().clear();
		});
	}

	public List<Object> getObjects(String methodName) {
		return Collections.unmodifiableList(cdaObjects.get(methodName));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		List<Object> objects = cdaObjects.get(method.getName());
		if (objects != null) {
			Object arg = args[0];
			objects.add(arg);
		}

		return method.invoke(rt, args);
	}
}
