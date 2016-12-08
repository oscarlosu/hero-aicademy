package marttoslo.evolution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import game.GameState;
import pacman.game.util.IO;

public class Data {
	
	public static int sampleResolution = 100;
	public static String generationsFilename = "generations";
	public static String unsplitFitnessFilename = "championFitnessUnsplit";
	public static String splitFitnessFilename = "championFitnessSplit";
	public static String resultsFilename = "results";
	public static String championFindFilename = "championFindGen";
	
	public static void main(String[] args) {
		// IO already reads from myData/
//		MergeData("08-12-2016 - 100 games budget 1", "08-12-2016 - 100 games budget 1.json");
		
		ProcessData("/08-12-2016 - 100 games budget 1/08-12-2016 - 100 games budget 1.json", 
					"08-12-2016 - 100 games budget 1/Processed");
		
		System.out.println("Done!");
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
	
	public static void ProcessData(String inputFile, String outputFolder) {
		// Load raw data
		ExperimentResultsCollection data = ExperimentResultsCollection.LoadFromFile(inputFile);		
		// Create folder
		File folder = new File("myData/" + outputFolder);
		folder.mkdir();
		
		// Win rates (player win rates, crystal wins, unit wins)
		saveResultData(data, outputFolder);
		
		// Champion find generation avg (host and parasite)
		saveChampionFindData(data, outputFolder);
		
		// Generations over turns graph (RHCA and OE)	
		saveGenerationsData(data, outputFolder);
			
		// Fitness over turns (hosts and parasites and OE)
		// Unsplit
		saveUnsplitFitnessData(data, outputFolder);
		// Split by winner
		saveSplitFitnessData(data, outputFolder);

		
	}
	
	public static void saveResultData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhcaWin = new double[1], oeWin = new double[1], draw = new double[1], rhcaCrystalWin = new double[1], 
			     rhcaUnitWin = new double[1], oeCrystalWin = new double[1], oeUnitWin = new double[1];
		
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 1) {
				// OE win
				oeWin[0]++;
				if(matchData.crystalWin) {
					oeCrystalWin[0]++;
				} else {
					oeUnitWin[0]++;
				}
			} else if(matchData.winnerIndex == 2) {
				// RHCA win
				rhcaWin[0]++;
				if(matchData.crystalWin) {
					rhcaCrystalWin[0]++;
				} else {
					rhcaUnitWin[0]++;
				}
			} else {
				// Draw
				draw[0]++;
			}
		}
		
		String[] headers = new String[]{"rhcaWins", "oeWin", "draw", "rhcaCrystalWin", "rhcaUnitWin", "oeCrystalWin", "oeUnitWin"};
		double[][] results = new double[][]{rhcaWin, oeWin, draw, rhcaCrystalWin, rhcaUnitWin, oeCrystalWin, oeUnitWin};
		SaveToFile(headers, results, outputFolder + "/" + resultsFilename + ".csv");
	}
	
	public static void saveChampionFindData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostChampionFindGen = new double[sampleResolution];
		double[] rhca_parasiteChampionFindGen = new double[sampleResolution];
		double[] oe_championFindGen = new double[sampleResolution];
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA
			// Hosts
			double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_championHostFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_hostChampionFindGen[t] += rhca_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_hostChampionFindGen[t] /= data.collection.size();
				}
			}
			// Parasites
			double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_championParasiteFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_parasiteChampionFindGen[t] += rhca_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_parasiteChampionFindGen[t] /= data.collection.size();
				}
			}
			// OE
			double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_championHostFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				oe_championFindGen[t] += oe_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					oe_championFindGen[t] /= data.collection.size();
				}
			}
			
		}
		String[] headers = new String[]{"rhca_hostChampionFindGen", "rhca_parasiteChampionFindGen", "oe_championFindGen"};
		double[][] championFindGen = new double[][]{rhca_hostChampionFindGen, rhca_parasiteChampionFindGen, oe_championFindGen};
		SaveToFile(headers, championFindGen, outputFolder + "/" + championFindFilename + ".csv");
	}
	
	public static void saveSplitFitnessData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostFitnessWin = new double[sampleResolution];
		double[] rhca_parasiteFitnessWin = new double[sampleResolution];
		double[] oe_fitnessWin = new double[sampleResolution];
		
		double[] rhca_hostFitnessLose = new double[sampleResolution];
		double[] rhca_parasiteFitnessLose = new double[sampleResolution];
		double[] oe_fitnessLose = new double[sampleResolution];
		
		double[] rhca_hostFitnessTie = new double[sampleResolution];
		double[] rhca_parasiteFitnessTie = new double[sampleResolution];
		double[] oe_fitnessTie = new double[sampleResolution];
		
		int rhcaWins = 0;
		int oeWins = 0;
		int draws = 0;	
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 1) {
				oeWins++;
			} else if(matchData.winnerIndex == 2) {
				rhcaWins++;
			} else {
				draws++;
			}			
		}
		
		
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 1) {
				// RHCA loses
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessLose[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessLose[t] += rhca_parasite_normalizedMatchData[t];
				}
				// OE wins
				double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_championFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					oe_fitnessWin[t] += oe_normalizedMatchData[t];
				}
			} else if(matchData.winnerIndex == 2) {
				// RHCA wins
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessWin[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessWin[t] += rhca_parasite_normalizedMatchData[t];
				}
				// OE loses
				double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_championFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					oe_fitnessLose[t] += oe_normalizedMatchData[t];
				}
			} else {
				// RHCA ties
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessTie[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessTie[t] += rhca_parasite_normalizedMatchData[t];
				}
				// OE ties
				double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_championFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					oe_fitnessTie[t] += oe_normalizedMatchData[t];
				}
			}
			
			
			
			// Divide to get average
			if(i == data.collection.size() - 1) {
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessLose[t] /= oeWins;
					rhca_parasiteFitnessLose[t] /= oeWins;
					oe_fitnessWin[t] /= oeWins;
					rhca_hostFitnessWin[t] /= rhcaWins;
					rhca_parasiteFitnessWin[t] /= rhcaWins;
					oe_fitnessLose[t] /= rhcaWins;
					rhca_hostFitnessTie[t] /= draws;
					rhca_parasiteFitnessTie[t] /= draws;
					oe_fitnessTie[t] /= draws;
				}
			}
			
		}
		
		System.out.println("rhcaWins" + rhcaWins);
		System.out.println("oeWins" + oeWins);
		System.out.println("draws" + draws);
		String[] headers = new String[]{"rhca_hostChampionFitnessWin", "rhca_parasiteChampionFitnessWin", "oe_championFitnessWin", 
										"rhca_hostChampionFitnessLose", "rhca_parasiteChampionFitnessLose", "oe_championFitnessLose", 
										"rhca_hostChampionFitnessTie", "rhca_parasiteChampionFitnessTie", "oe_championFitnessTie"};
		double[][] championFitness = new double[][]{rhca_hostFitnessWin, rhca_parasiteFitnessWin, oe_fitnessWin,
													rhca_hostFitnessLose,rhca_parasiteFitnessLose,oe_fitnessLose,
													rhca_hostFitnessTie,rhca_parasiteFitnessTie,oe_fitnessTie};
		SaveToFile(headers, championFitness, outputFolder + "/" + splitFitnessFilename + ".csv");
	}
	
	public static void saveUnsplitFitnessData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostFitness = new double[sampleResolution];
		double[] rhca_parasiteFitness = new double[sampleResolution];
		double[] oe_fitness = new double[sampleResolution];
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA
			// Hosts
			double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_championHostFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_hostFitness[t] += rhca_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_hostFitness[t] /= data.collection.size();
				}
			}
			// Parasites
			double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_championParasiteFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_parasiteFitness[t] += rhca_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_parasiteFitness[t] /= data.collection.size();
				}
			}
			// OE
			double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_championFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				oe_fitness[t] += oe_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					oe_fitness[t] /= data.collection.size();
				}
			}
			
		}
		String[] headers = new String[]{"rhca_hostChampionFitness", "rhca_parasiteChampionFitness", "oe_championFitness"};
		double[][] championFitness = new double[][]{rhca_hostFitness, rhca_parasiteFitness, oe_fitness};
		SaveToFile(headers, championFitness, outputFolder + "/" + unsplitFitnessFilename + ".csv");
	}
	
	public static void saveGenerationsData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_generations = new double[sampleResolution];
		double[] oe_generations = new double[sampleResolution];
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA
			double[] rhca_normalizedMatchData = matchNormalizeData(matchData.co_generations, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_generations[t] += rhca_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_generations[t] /= data.collection.size();
				}
			}
			// OE
			double[] oe_normalizedMatchData = matchNormalizeData(matchData.oe_generations, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				oe_generations[t] += oe_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					oe_generations[t] /= data.collection.size();
				}
			}
		}
		String[] headers = new String[]{"rhca_generations", "oe_generations"};
		double[][] generations = new double[][]{rhca_generations, oe_generations};
		SaveToFile(headers, generations, outputFolder + "/" + generationsFilename + ".csv");
	}
	
	public static double[] matchNormalizeData(List<Double> inData, int sampleResolution) {
		double[] outData = new double[sampleResolution];
		for(int i = 0; i < sampleResolution; i++) {
			double samplePos = i / (double)sampleResolution;
			// Locate indexes in current match data
			double matchSamplePos = samplePos * inData.size();
			double interpolation = matchSamplePos - (int)matchSamplePos;
			int lowerIndex = (int)Math.floor(matchSamplePos);
			// Avoids index out of range in last sample
			int upperIndex = Math.min(inData.size() - 1, (int)Math.ceil(matchSamplePos));				
			// Sample data
			double sample = (1 - interpolation) * inData.get(lowerIndex) + 
							interpolation * inData.get(upperIndex);
			outData[i] = sample;
		}
		return outData;
	}
	
	public static void SaveToFile(String[] headers, double[][] valueGroups, String filename) {
		StringBuilder builder = new StringBuilder();
		if(headers.length != valueGroups.length) {
			System.out.println("Error in SaveToFile. Headers size = " + headers.length + ". Value Groups size = " + valueGroups.length);
		}
		// Write headers
		for(int h = 0, hl = headers.length; h < hl; ++h) {
			builder.append(headers[h]);
			// Separator or line break
			if(h < hl - 1) {
				builder.append(",");
			} else {
				builder.append("\n");
			}
			
		}
		// Write data
		for(int v = 0, vl = valueGroups[0].length; v < vl; ++v) {
			for(int g = 0, gl = valueGroups.length; g < gl; ++g) {
				builder.append(valueGroups[g][v]);				
				// Separator or line break
				if(g < gl - 1) {
					builder.append(",");
				} else {
					builder.append("\n");
				}
			}
		}
		// Save to file
		IO.saveFile(filename, builder.toString(), false);		
	}

}
