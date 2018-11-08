package com.cuckoodroid.droidmon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cuckoodroid.droidmon.utils.Files;
import com.cuckoodroid.droidmon.utils.MethodApiType;
import com.google.gson.Gson;
import android.content.pm.ApplicationInfo;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook;
import static de.robv.android.xposed.XposedHelpers.findClass;
//start added code
//to use our own Logger and MethodHookImpl
import sg.edu.smu.droidmon.Logger;
import sg.edu.smu.droidmon.MethodHookImpl;
import sg.edu.smu.droidmon.FileUtils;
//end added code

// This class will be called by Xposed
public class InstrumentationManager implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	public static String CONFIG_FILE = "/data/local/tmp/hooks.json";
	public static Boolean TRACE = false;
	//start added code
	public static String CUSTOM_CONFIG_FILE = "/data/local/tmp/monitoredApp.txt";
	public static Map<String, Integer> tracedMethodToIdMap = new HashMap<String, Integer>();
	private static List<String> MONITORED_APPS = null; 
	//end added code
	
	private static Set<String> OS_APPS = new HashSet<String>(Arrays.asList(
			"com.android.systemui"
			,"com.android.inputmethod.latin"
			,"com.android.phone"
			,"com.android.launcher"
			,"com.android.settings"
			,"com.android.contacts"
			,"com.android.providers.calendar"
			,"android.process.media"
			,"com.android.mms"
			,"com.android.deskclock"
			,"com.android.email"
			,"com.android.exchange"
			,"com.cuckoo.android.agent"
			,"com.noshufou.android.su"
			,"com.android.calendar"
			,"com.android.droidmon"
			,"de.robv.android.xposed.installer")) ;

	// Call when zygote initialize
	public void initZygote(StartupParam startupParam) throws Throwable {
		// start added code
		MONITORED_APPS = FileUtils.readFile(InstrumentationManager.CUSTOM_CONFIG_FILE);
		// end added code
		hookNonSystemServices();

	}

	// Hook methods not provided by system services
	private void hookNonSystemServices(){


	}

	// Call when package loaded
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable{
		if(lpparam.appInfo == null || 
				(lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) !=0){
			return;
		}//else if(lpparam.isFirstApplication && !OS_APPS.contains(lpparam.packageName)) // original if condition
		else if(lpparam.isFirstApplication && MONITORED_APPS.contains(lpparam.packageName))
		{
			Logger.PACKAGENAME = lpparam.packageName;
			try {
				instrumentApp();
			} catch (FileNotFoundException e) {
				Logger.logError(e.getMessage());
			} catch (IOException e) {
				Logger.logError(e.getMessage());
			}
		}
	}
	
	public void instrumentApp() throws FileNotFoundException, IOException
	{
		Gson gson = new Gson();
		String json = Files.readFile(CONFIG_FILE);
		InstrumentationConfiguration instrumentationConfiguration = gson.fromJson(json, InstrumentationConfiguration.class);
		TRACE=instrumentationConfiguration.trace;
		for (HookConfig hookConfig : instrumentationConfiguration.hookConfigs) {
			//start added code
			tracedMethodToIdMap.put(hookConfig.class_name+"."+hookConfig.method, tracedMethodToIdMap.size()+1);
			//end added code
			hook(new MethodHookImpl(hookConfig.class_name,hookConfig.method,hookConfig.thisObject,hookConfig.type));
		}
	}

 	public class InstrumentationConfiguration {
		public List<HookConfig> hookConfigs;
		public Boolean trace;
	}

	public class HookConfig {
		private String class_name;
		private String method;
		private Boolean thisObject;
		private MethodApiType type;
	}

	private static void hook(MethodHookImpl methodHook) {
		hook(methodHook, null);
	}

	private static void hook(final MethodHookImpl methodHook, ClassLoader classLoader) {
		try {

			// Create hook method
			XC_MethodHook xcMethodHook = methodHook;

			// Find hook class
			Class<?> hookClass = findClass(methodHook.getClassName(), classLoader);
			if (hookClass == null) {
				String message = String.format("Hook-Class not found: %s", methodHook.getClassName());
				Logger.logError(message);
				return;
			}

			// Add hook
			if (methodHook.getMethodName() == null) {
				for (Constructor<?> constructor : hookClass.getDeclaredConstructors()){
					XposedBridge.hookMethod(constructor, xcMethodHook);
				}
			} else{
				for (Method method : hookClass.getDeclaredMethods())
					if (method.getName().equals(methodHook.getMethodName()))
						XposedBridge.hookMethod(method, xcMethodHook);
			}

		} catch (Throwable ex) {

		}
	}
}
