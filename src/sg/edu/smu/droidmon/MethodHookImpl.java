package sg.edu.smu.droidmon;

import com.cuckoodroid.droidmon.utils.MethodApiType;


public class MethodHookImpl extends com.cuckoodroid.droidmon.MethodHookImpl{
	
	public MethodHookImpl(String className, String methodName, boolean thisObject, MethodApiType type) {
		super(className, methodName, thisObject, type);
	}
	
	public void monitorMethod(MethodHookParam param)
	{
		try {
			String methodName = param.method.getName();
//			if (methodName.contains("read")) {
//				if (param.thisObject!=null) {
//					if (param.thisObject.getClass().toString().contains("ProcessInputStream")) {
//						//Logger.logHook(hookData);
//					}
//				}
//			}else if (methodName.contains("write")) {
//				if (param.thisObject!=null) {
//					if (param.thisObject.getClass().toString().contains("ProcessOutputStream")) {
//						//Logger.logHook(hookData);
//					}
//				}
			if (methodName.contains("invoke")){
				Logger.logReflectionMethod(param);
			}
			else if (!methodName.contains("read") && !methodName.contains("write")){
				Logger.logGenericMethod(param); // other than method that is "doing" read and write
			}
		} catch (Exception e) {
			Logger.logError(param.method.getDeclaringClass().getName()+"->"+param.method.getName());
		}
	}
	
}
