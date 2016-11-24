package marttoslo.evolution;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import pacman.game.util.IO;

public class ExperimentResultsCollection {
	@Expose
	public List<ExperimentResults> collection = new ArrayList<ExperimentResults>();	
	
	public static ExperimentResultsCollection LoadFromFile(String filename) {
		// Create json string
		GsonBuilder builder = new GsonBuilder();
	    builder.excludeFieldsWithoutExposeAnnotation();
	    builder.setPrettyPrinting();
	    Gson gson = builder.create();
		String json = IO.loadFile(filename);
		ExperimentResultsCollection resCol = gson.fromJson(json, ExperimentResultsCollection.class);
		return resCol;
	}
	
	public void SaveToFile(String filename) {
		// Create json string
		GsonBuilder builder = new GsonBuilder();
	    builder.excludeFieldsWithoutExposeAnnotation();
	    builder.setPrettyPrinting();
	    Gson gson = builder.create();
		String json = gson.toJson(this);
		// Save to file in myData/
		IO.saveFile(filename, json, false);
	}
}
