package sg.edu.smu.droidmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileUtils {
	
	public static List<String> readFile(String filename) throws FileNotFoundException,IOException
	{
		//Get the text file
		File file = new File(filename);

		//Read text from file
		StringBuilder text = new StringBuilder();
		List<String> lines = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;

		while ((line = br.readLine()) != null) 
		{
			lines.add(line);
		}
		
		br.close();
		return lines;
	}
	

}