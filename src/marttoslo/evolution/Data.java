package marttoslo.evolution;

import java.io.File;

import game.GameState;

public class Data {
	public static void main(String[] args) {
		// IO already reads from myData/
		MergeData("/24-11-2016 - 400 games budget 1", "24-11-2016 - 400 games budget 1.json");
	}
	
	public static void MergeData(String folderName, String outputFile) {
		File folder = new File("myData/" + folderName);		
		File files[] = folder.listFiles();
		
		// Create one collection per file
		ExperimentResultsCollection partials[] = new ExperimentResultsCollection[files.length];
		for(int i = 0; i < files.length; ++i) {
			partials[i] = ExperimentResultsCollection.LoadFromFile(folderName + "/" + files[i].getName());			
			System.out.println(folderName + "/" + files[i].getName());
		}
		// Combine collections
		ExperimentResultsCollection combined = new ExperimentResultsCollection();
		for(int i = 0; i < partials.length; ++i) {
			combined.collection.addAll(partials[i].collection);
		}
		// Save to single file
		combined.SaveToFile(folderName + "/" + outputFile);		
	}
}
