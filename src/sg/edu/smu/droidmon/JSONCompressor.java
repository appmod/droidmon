package sg.edu.smu.droidmon;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.*;

import android.util.Base64;

public class JSONCompressor{
	
	//compress the input string, return the input string back if there is any exception
	public static String compress(String str){
	    if (str == null || str.length() == 0) {
	        return str;
	    }
	    String outStr = str;
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
		    gzip.write(str.getBytes());
		    gzip.close();
		    outStr = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return outStr;
	 }
	
	// decompress only if the input string is compressed. Otherwise, return the input string.
	public static String decompress(String str){
		String outStr = str;
		byte[] bytes = Base64.decode(str, Base64.DEFAULT);
		if (JSONCompressor.isCompressed(bytes)) {
			GZIPInputStream gis;
			try {
				gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis));
			    String line;
				while ((line = bufferedReader.readLine()) != null) {
					outStr+= line;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				outStr = e.toString();
				e.printStackTrace();
			}
		}
		return outStr;
	}
	
	public static boolean isCompressed(final byte[] compressed) {
		return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
	}
	
	

}
