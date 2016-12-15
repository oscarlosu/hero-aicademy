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
	
	public static String mergedDataFilename = "15-12-2016 - Raw vs Portfolio - 100 games 4 budget";
	
	public static void main(String[] args) {
		String jsonFile = mergedDataFilename + ".json";
		// IO already reads from myData/
		//MergeData(mergedDataFilename, jsonFile);
		
		ProcessData("/" + mergedDataFilename + "/" + jsonFile, 
					mergedDataFilename + "/Processed");
		
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
		double[] rhcaWin = new double[1], rhcaPortfolioWin = new double[1], draw = new double[1], rhcaCrystalWin = new double[1], 
			     rhcaUnitWin = new double[1], rhcaPortfolioCrystalWin = new double[1], rhcaPortfolioUnitWin = new double[1];
		
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 2) {
				// rhcaPortfolio win
				rhcaPortfolioWin[0]++;
				if(matchData.crystalWin) {
					rhcaPortfolioCrystalWin[0]++;
				} else {
					rhcaPortfolioUnitWin[0]++;
				}
			} else if(matchData.winnerIndex == 1) {
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
		
		String[] headers = new String[]{"rhcaRawWins", "rhcaPortfolioWin", "draw", "rhcaCrystalWin", "rhcaUnitWin", "rhcaPortfolioCrystalWin", "rhcaPortfolioUnitWin"};
		double[][] results = new double[][]{rhcaWin, rhcaPortfolioWin, draw, rhcaCrystalWin, rhcaUnitWin, rhcaPortfolioCrystalWin, rhcaPortfolioUnitWin};
		SaveToFile(headers, results, outputFolder + "/" + resultsFilename + ".csv");
	}
	
	public static void saveChampionFindData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostChampionFindGen = new double[sampleResolution];
		double[] rhca_parasiteChampionFindGen = new double[sampleResolution];
		double[] rhcaPortfolio_hostChampionFindGen = new double[sampleResolution];
		double[] rhcaPortfolio_parasiteChampionFindGen = new double[sampleResolution];
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA raw
			// Hosts
			double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_raw_championHostFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_hostChampionFindGen[t] += rhca_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_hostChampionFindGen[t] /= data.collection.size();
				}
			}
			// Parasites
			double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_raw_championParasiteFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_parasiteChampionFindGen[t] += rhca_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_parasiteChampionFindGen[t] /= data.collection.size();
				}
			}
			// RHCA Portfolio
			// Hosts
			double[] rhcaPortfolio_host_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championHostFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhcaPortfolio_hostChampionFindGen[t] += rhcaPortfolio_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhcaPortfolio_hostChampionFindGen[t] /= data.collection.size();
				}
			}
			// Parasites
			double[] rhcaPortfolio_parasite_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championParasiteFindGen, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhcaPortfolio_parasiteChampionFindGen[t] += rhcaPortfolio_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhcaPortfolio_parasiteChampionFindGen[t] /= data.collection.size();
				}
			}
			
		}
		String[] headers = new String[]{"rhca_hostChampionFindGen", "rhca_parasiteChampionFindGen", "rhcaPortfolio_hostChampionFindGen", "rhcaPortfolio_parasiteChampionFindGen"};
		double[][] championFindGen = new double[][]{rhca_hostChampionFindGen, rhca_parasiteChampionFindGen, rhcaPortfolio_hostChampionFindGen, rhcaPortfolio_parasiteChampionFindGen};
		SaveToFile(headers, championFindGen, outputFolder + "/" + championFindFilename + ".csv");
	}
	
	public static void saveSplitFitnessData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostFitnessWin = new double[sampleResolution];
		double[] rhca_parasiteFitnessWin = new double[sampleResolution];
		double[] rhcaPortfolio_hostFitnessWin = new double[sampleResolution];
		double[] rhcaPortfolio_parasiteFitnessWin = new double[sampleResolution];
		
		double[] rhca_hostFitnessLose = new double[sampleResolution];
		double[] rhca_parasiteFitnessLose = new double[sampleResolution];
		double[] rhcaPortfolio_hostFitnessLose = new double[sampleResolution];
		double[] rhcaPortfolio_parasiteFitnessLose = new double[sampleResolution];
		
		double[] rhca_hostFitnessTie = new double[sampleResolution];
		double[] rhca_parasiteFitnessTie = new double[sampleResolution];
		double[] rhcaPortfolio_hostFitnessTie = new double[sampleResolution];
		double[] rhcaPortfolio_parasiteFitnessTie = new double[sampleResolution];
		
		int rhcaWins = 0;
		int rhcaPortfolioWins = 0;
		int draws = 0;	
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 2) {
				rhcaPortfolioWins++;
			} else if(matchData.winnerIndex == 1) {
				rhcaWins++;
			} else {
				draws++;
			}			
		}
		
		
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			if(matchData.winnerIndex == 1) {
				// RHCA RAW wins
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_raw_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessWin[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_raw_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessWin[t] += rhca_parasite_normalizedMatchData[t];
				}
				
				
				// RHCA Portfolio loses
				// Hosts
				double[] rhcaPortfolio_host_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_hostFitnessLose[t] += rhcaPortfolio_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhcaPortfolio_parasite_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_parasiteFitnessLose[t] += rhcaPortfolio_parasite_normalizedMatchData[t];
				}
			} else if(matchData.winnerIndex == 2) {
				// RHCA RAW loses
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_raw_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessLose[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_raw_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessLose[t] += rhca_parasite_normalizedMatchData[t];
				}
				
				
				// RHCA Portfolio wins
				double[] rhcaPortfolio_host_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_hostFitnessWin[t] += rhcaPortfolio_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhcaPortfolio_parasite_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_parasiteFitnessWin[t] += rhcaPortfolio_parasite_normalizedMatchData[t];
				}
			} else {
				// RHCA RAW ties
				// Hosts
				double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_raw_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessTie[t] += rhca_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_raw_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_parasiteFitnessTie[t] += rhca_parasite_normalizedMatchData[t];
				}
				// RHCA Portfolio ties
				double[] rhcaPortfolio_host_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championHostFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_hostFitnessTie[t] += rhcaPortfolio_host_normalizedMatchData[t];
				}
				// Parasites
				double[] rhcaPortfolio_parasite_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championParasiteFitnesses, sampleResolution);
				for(int t = 0; t < sampleResolution; ++t) {
					rhcaPortfolio_parasiteFitnessTie[t] += rhcaPortfolio_parasite_normalizedMatchData[t];
				}
			}
			
			
			
			// Divide to get average
			if(i == data.collection.size() - 1) {
				for(int t = 0; t < sampleResolution; ++t) {
					rhca_hostFitnessLose[t] /= rhcaPortfolioWins;
					rhca_parasiteFitnessLose[t] /= rhcaPortfolioWins;
					rhcaPortfolio_hostFitnessWin[t] /= rhcaPortfolioWins;
					rhcaPortfolio_parasiteFitnessWin[t] /= rhcaPortfolioWins;
					
					rhca_hostFitnessWin[t] /= rhcaWins;
					rhca_parasiteFitnessWin[t] /= rhcaWins;
					rhcaPortfolio_hostFitnessLose[t] /= rhcaWins;
					rhcaPortfolio_parasiteFitnessLose[t] /= rhcaWins;
					
					rhca_hostFitnessTie[t] /= draws;
					rhca_parasiteFitnessTie[t] /= draws;
					rhcaPortfolio_hostFitnessTie[t] /= draws;
					rhcaPortfolio_parasiteFitnessTie[t] /= draws;
				}
			}
			
		}
		
		System.out.println("rhcaWins" + rhcaWins);
		System.out.println("rhcaPortfolioWins" + rhcaPortfolioWins);
		System.out.println("draws" + draws);
		String[] headers = new String[]{"rhca_hostChampionFitnessWin", "rhca_parasiteChampionFitnessWin", "rhcaPortfolio_hostChampionFitnessWin", "rhcaPortfolio_parasiteChampionFitnessWin", 
										"rhca_hostChampionFitnessLose", "rhca_parasiteChampionFitnessLose", "rhcaPortfolio_hostChampionFitnessLose", "rhcaPortfolio_parasiteChampionFitnessLose", 
										"rhca_hostChampionFitnessTie", "rhca_parasiteChampionFitnessTie", "rhcaPortfolio_hostChampionFitnessTie", "rhcaPortfolio_parasiteChampionFitnessTie"};
		double[][] championFitness = new double[][]{rhca_hostFitnessWin, rhca_parasiteFitnessWin, rhcaPortfolio_hostFitnessWin, rhcaPortfolio_parasiteFitnessWin,
													rhca_hostFitnessLose,rhca_parasiteFitnessLose,rhcaPortfolio_hostFitnessLose, rhcaPortfolio_parasiteFitnessLose,
													rhca_hostFitnessTie,rhca_parasiteFitnessTie,rhcaPortfolio_hostFitnessTie, rhcaPortfolio_parasiteFitnessTie};
		SaveToFile(headers, championFitness, outputFolder + "/" + splitFitnessFilename + ".csv");
	}
	
	public static void saveUnsplitFitnessData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_hostFitness = new double[sampleResolution];
		double[] rhca_parasiteFitness = new double[sampleResolution];
		double[] rhcaPortfolio_hostFitness = new double[sampleResolution];
		double[] rhcaPortfolio_parasiteFitness = new double[sampleResolution];
		
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA raw
			// Hosts
			double[] rhca_host_normalizedMatchData = matchNormalizeData(matchData.co_raw_championHostFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_hostFitness[t] += rhca_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_hostFitness[t] /= data.collection.size();
				}
			}
			// Parasites
			double[] rhca_parasite_normalizedMatchData = matchNormalizeData(matchData.co_raw_championParasiteFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_parasiteFitness[t] += rhca_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_parasiteFitness[t] /= data.collection.size();
				}
			}
			// RHCA Portfolio
			// Hosts
			double[] rhcaPortfolio_host_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championHostFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhcaPortfolio_hostFitness[t] += rhcaPortfolio_host_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhcaPortfolio_hostFitness[t] /= data.collection.size();
				}
			}
			// Hosts
			double[] rhcaPortfolio_parasite_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_championParasiteFitnesses, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhcaPortfolio_parasiteFitness[t] += rhcaPortfolio_parasite_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhcaPortfolio_parasiteFitness[t] /= data.collection.size();
				}
			}
			
		}
		String[] headers = new String[]{"rhca_hostChampionFitness", "rhca_parasiteChampionFitness", "rhcaPortfolio_hostChampionFitness", "rhcaPortfolio_parasiteChampionFitness"};
		double[][] championFitness = new double[][]{rhca_hostFitness, rhca_parasiteFitness, rhcaPortfolio_hostFitness, rhcaPortfolio_parasiteFitness};
		SaveToFile(headers, championFitness, outputFolder + "/" + unsplitFitnessFilename + ".csv");
	}
	
	public static void saveGenerationsData(ExperimentResultsCollection data, String outputFolder) {
		double[] rhca_generations = new double[sampleResolution];
		double[] rhcaPortfolio_generations = new double[sampleResolution];
		for(int i = 0; i < data.collection.size(); ++i) {
			ExperimentResults matchData = data.collection.get(i);
			// RHCA
			double[] rhca_normalizedMatchData = matchNormalizeData(matchData.co_raw_generations, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhca_generations[t] += rhca_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhca_generations[t] /= data.collection.size();
				}
			}
			// RHCA Portfolio
			double[] rhcaPortfolio_normalizedMatchData = matchNormalizeData(matchData.co_portfolio_generations, sampleResolution);
			for(int t = 0; t < sampleResolution; ++t) {
				rhcaPortfolio_generations[t] += rhcaPortfolio_normalizedMatchData[t];
				// On last match, divide to get average
				if(i == data.collection.size() - 1) {
					rhcaPortfolio_generations[t] /= data.collection.size();
				}
			}
		}
		String[] headers = new String[]{"rhca_generations", "rhcaPortfolio_generations"};
		double[][] generations = new double[][]{rhca_generations, rhcaPortfolio_generations};
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
