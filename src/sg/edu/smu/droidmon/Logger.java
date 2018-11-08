package sg.edu.smu.droidmon;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cuckoodroid.droidmon.InstrumentationManager;
import com.cuckoodroid.droidmon.utils.MethodApiType;

import android.text.TextUtils;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;

public class Logger extends com.cuckoodroid.droidmon.utils.Logger{
	
	public static void logHook(final JSONObject hookData){
		XposedBridge.log(LOGTAG_WORKFLOW+PACKAGENAME+":"+hookData.toString());
	}
	
	public static void logHook(String hookData){
		XposedBridge.log(LOGTAG_WORKFLOW+PACKAGENAME+":"+hookData+":"+InstrumentationManager.tracedMethodToIdMap.get(hookData));

	}
	
	public static void logShell(String message){
		XposedBridge.log(LOGTAG_SHELL+PACKAGENAME+":"+message);
	}

	public static void logError(String message){
		XposedBridge.log(LOGTAG_ERROR+":"+message);
	}
	
	public static String convertToCSV(JSONObject hookJson) throws JSONException {
		List<String> jsonValArr = new ArrayList<String>();
		String className = hookJson.getString("class");
		String methodName = hookJson.getString("class");
		String fullMethodName = className + "." + methodName;
		if (hookJson.has("args")) {
			
		}
		return fullMethodName;
	}
	
	public static void logGenericMethod(MethodHookParam param) throws JSONException {
		JSONObject hookJson = ParseGenerator.generateHookDataJson(param);
		String className = hookJson.getString("class");
		String methodName = hookJson.getString("method");
		hookJson.remove("class");
		hookJson.put("method", InstrumentationManager.tracedMethodToIdMap.get(className+"."+methodName));
		
		// We follow Lingfeng's log parsing conditions 
		// We keep only information that Lingfeng used
		if (className.contentEquals("android.app.ContextImpl") && methodName.contentEquals("registerReceiver")) {
			if (param.args!=null) {
				JSONArray args = ParseGenerator.parseArgs(param, hookJson);
				JSONArray filteredArgs = extractRelevantArgs(args, "mActions");
				if(filteredArgs.length()>0) {
					hookJson.put("args", filteredArgs);
					Logger.logHook(hookJson);
				}
			}
		} else if (className.contentEquals("android.app.SharedPreferencesImpl$EditorImpl")) {
			if (param.args!=null) {
				JSONArray args = ParseGenerator.parseArgs(param, hookJson);
				if (args.length() == 2) {
					hookJson.put("args", args);
					Logger.logHook(hookJson);
				}
			}
		} else if (className.contentEquals("android.content.ContentResolver")) {
			if (param.args!=null) {
				JSONArray args = ParseGenerator.parseArgs(param, hookJson);
				JSONArray filteredArgs = new JSONArray();
				if(methodName.contentEquals("query")) {
					filteredArgs = extractRelevantArgs(args, "uriString");
				}else if (methodName.contentEquals("registerContentObserver")) {
					filteredArgs = new JSONArray();
					JSONObject filteredArg = new JSONObject();
					JSONObject arg = (JSONObject) args.get(0);
					
					String uriString = arg.getString("uriString");
					if (!uriString.contentEquals("NOT CACHED")) {
						filteredArg.put("uriString",uriString);
					}
					
					String host =  arg.getString("host");
					if (!host.contentEquals("NOT CACHED")) {
						filteredArg.put("host",host);
					}
					
					JSONObject path =  (JSONObject) arg.get("path");
					String path_encoded = path.getString("encoded");
					if (!path_encoded.contentEquals("NOT CACHED")) {
						JSONObject new_path = new JSONObject();
						new_path.put("encoded", path_encoded);
						filteredArg.put("path",new_path);
					}
					filteredArgs.put(filteredArg);
				}
				
				if(filteredArgs.length()>0) {
					hookJson.put("args", filteredArgs);
					Logger.logHook(hookJson);
				}
			}
		} else if (methodName.contentEquals("loadUrl") || methodName.contentEquals("loadDataWithBaseURL")) {
			if (param.args!=null) {
				JSONArray args = ParseGenerator.parseArgs(param, hookJson);
				JSONArray filteredArgs = new JSONArray();
				Object arg = (Object) args.get(0);
				if (arg instanceof String) {
					String url = (String) arg;
					if(url.startsWith("http") || (!url.startsWith("file") && url.length()>50)) {
						filteredArgs.put(url);
					}
					
					if(filteredArgs.length()>0) {
						hookJson.put("args", filteredArgs);
						Logger.logHook(hookJson);
					}
				}
			}
		} else if (methodName.contentEquals("openConnection")) {
			if(param.thisObject!=null) {
				Object thisObj = ParseGenerator.parseThis(param,hookJson);
				if(thisObj instanceof String) {
					String url = (String) thisObj;
					if (url.startsWith("http")) {
						hookJson.put("this", url);
						Logger.logHook(hookJson);
					}
				}
			}
		} else {
			if (param.args!=null) {
				JSONArray args = ParseGenerator.parseArgs(param, hookJson);
				JSONArray filteredArgs = new JSONArray();
				for(int i=0;i<args.length(); i++) {
					Object arg = args.get(i);
					if (arg instanceof String) {
						String argStr = (String) arg;
						if (argStr.startsWith("http") || argStr.contains("@")) {
							filteredArgs.put(argStr);
						}
						
					}
				}
				hookJson.put("args", filteredArgs);
				Logger.logHook(hookJson);
			}
		}
		
	}

	// extract only args elements that is of type JSONObject, containsKey objectKey, and keep only value referred by objectKey
	private static JSONArray extractRelevantArgs(JSONArray args, String objectKey) throws JSONException {
		JSONArray filteredArgs = new JSONArray();
		for(int i=0;i<args.length(); i++) {
			Object arg = args.get(i);
			if (arg instanceof JSONObject) {
				JSONObject argJson = (JSONObject) arg;
				if (argJson.has(objectKey)) {
					JSONObject mActions = new JSONObject();
					mActions.put(objectKey, argJson.get(objectKey));
					filteredArgs.put(mActions);
					break;
				}
			}
		}
		return filteredArgs;
	}
	
	public static void logReflectionMethod(MethodHookParam param) throws JSONException {
		JSONObject hookJson = ParseGenerator.generateHookDataJson(param);
		String className = hookJson.getString("class");
		String methodName = hookJson.getString("method");
		hookJson.remove("class");
		hookJson.put("method", InstrumentationManager.tracedMethodToIdMap.get(className+"."+methodName));
		
		hookJson.put("hooked_method", ParseGenerator.parseReflectionClassName(param));
		hookJson.put("hooked_class", ParseGenerator.parseReflectionClassName(param));
		
		Logger.logHook(hookJson);
		
	}
	
}
