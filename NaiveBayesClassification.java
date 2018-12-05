import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class NaiveBayesClassification {
	// Course:			CS4242
	// Student name:	Jacob Martinez
	// Student ID:		000672813
	// Assignment #:	#6
	// Due Date:		11/26/2018
	// Signature:		______________
	// Score:			______________
	
	// Constants
	private static final int SEPAL_LENGTH = 0;
	private static final int SEPAL_WIDTH = 1;
	private static final int PETAL_LENGTH = 2;
	private static final int PETAL_WIDTH = 3;
	private static final int CLASSIFICATION = 4;
	
	// Program flow controls
	private static boolean trained;
	
	// List of unique classifications
	private static String[] classificationArr;
	
	// Arrays for storing trained data
	// Rows = classifications
	// Cols = constants
	private static double[][] meanArr;
	private static double[][] varianceArr;
	private static double[] classArr; // Stores class representation as decimal
	
	private static Object[][] rawData;
	
	// Adds classifier to classificationArr if one does not already exist
	private static int addClassifier(String classifier) {
		if (getClassifierIndex(classifier) < 0) {
			String[] tempArr = new String[classificationArr.length+1];
			tempArr[classificationArr.length] = classifier;
			for (int i=0; i<classificationArr.length; ++i)
				tempArr[i] = classificationArr[i];
			classificationArr = tempArr;
		}
		
		return getClassifierIndex(classifier);
	}
	
	// Returns index of classifier or -1 if it doesn't exist
	private static int getClassifierIndex(String classifier) {
		for (int i=0; i<classificationArr.length; ++i)
			if (classificationArr[i].equals(classifier))
				return i;
		
		return -1;
	}
	
	// Returns array of feature values for the given class
	private static double[] getFeatureValsByClass(int classIndex, int feature) {
		double[] values = new double[rawData.length];
		
		int index = 0;
		for (int i=0; i<rawData.length; ++i)
			if ((int)rawData[i][CLASSIFICATION] == classIndex) {
				values[index] = (double)rawData[i][feature];
				++index;
			}
		
		double[] valuesTrimmed = new double[index];
		for (int i=0; i<valuesTrimmed.length; ++i)
			valuesTrimmed[i] = values[i];
		
		return valuesTrimmed;
	}
	
	// Loads data from CSV into a 2 dimensional array of Objects
	private static Object[][] loadDataFromFile(String fileName) {
		Object[][] dataArr = null;

		try {
			RandomAccessFile raf = new RandomAccessFile(new File(fileName), "rw");
			String[] dataRow;
			int rows = 0;
			int cols = 0;
			
			for (String rawRow; (rawRow = raf.readLine()) !=null; ) {
				++rows;
				cols = rawRow.split(",").length;
			}
			
			raf.seek(0);
			dataArr = new Object[rows][cols];
			
			int row = 0;
			for (String rawRow; (rawRow = raf.readLine()) !=null; ) {
				dataRow = rawRow.split(",");
				for (int col = 0; col < dataRow.length; ++col) {
					if (col != CLASSIFICATION)
						dataArr[row][col] = Double.parseDouble(dataRow[col]);
					else
						dataArr[row][col] = addClassifier(dataRow[col]);
				}
				++row;
			}
			
			raf.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return dataArr;
	}
	
	// Shuffles list of data
	private static Object[][] shuffleData(Object[][] rawData) {
		Random rnd = ThreadLocalRandom.current();
		Object[][] shuffledData = rawData;
		Object[] tempArr = new Object[shuffledData[0].length];
		
		for (int i = 0; i<rawData.length; ++i) {
			int index = rnd.nextInt(i+1);
			// swap
			tempArr = shuffledData[index];
			shuffledData[index] = shuffledData[i];
			shuffledData[i] = tempArr;
		}
		
		return shuffledData;
	}
	
	// Trains data set by taking in a set of data and filling trained data arrays
	// for later use in probability calculation
	private static void trainNaiveBayes(Object[][] trainingData) {
		classArr = new double[classificationArr.length];
		meanArr = new double[classificationArr.length][4];
		varianceArr = new double[classificationArr.length][4];
		double sum = 0.0;
		double [] classFeatureArr;
		
		// Fill class Array
		for (int clsfctn = 0; clsfctn<classificationArr.length; ++clsfctn) {
			int count = 0;
			for (int i=0; i<trainingData.length; ++i)
				if (trainingData[i][CLASSIFICATION].equals(classificationArr[clsfctn]))
					++count;
			classArr[clsfctn] = (double)count/trainingData.length;
		}
				
		
		// Fill Average Array
		for (int clsfctn = 0; clsfctn<classificationArr.length; ++clsfctn)
			for (int feature = 0; feature<4; ++feature) {
				classFeatureArr = getFeatureValsByClass(clsfctn, feature);
				for (int i = 0; i < classFeatureArr.length; ++i)
					sum += classFeatureArr[i];
				meanArr[clsfctn][feature] = sum/((double)classFeatureArr.length);
			}
		
		sum = 0.0;
		
		// Fill Variance Array
		for (int clsfctn = 0; clsfctn<classificationArr.length; ++clsfctn)
			for (int feature = 0; feature<4; ++feature) {
				classFeatureArr = getFeatureValsByClass(clsfctn, feature);
				for (int i = 0; i < classFeatureArr.length; ++i)
					sum += (classFeatureArr[i] - meanArr[clsfctn][feature]) * (classFeatureArr[i] - meanArr[clsfctn][feature]);
				varianceArr[clsfctn][feature] = Math.sqrt(sum/((double)classFeatureArr.length));
			}
	}
	
	// Takes in a set of test values and returns the percentage of values guessed correctly
	private static double testNaiveBayes(Object[][] testData) {
		int countCorrect = 0;
		
		for (int i=0; i<testData.length; ++i) {
			double[] testFeature = new double[testData[i].length-1];
			for (int feature = 0; feature<testFeature.length; ++feature)
				testFeature[feature] = (double)testData[i][feature];
			if (predictClassifier(testFeature) == (int)testData[i][CLASSIFICATION])
				++countCorrect;
		}

		return (double)countCorrect/testData.length*100;
	}
	
	// Takes in feature array and returns best classification guess as an index value
	private static int predictClassifier(double[] featureArr) {
		double[] estimates = new double[classificationArr.length];
		
		// Test all possible classifications and store probabilities
		for (int i=0; i<classificationArr.length; ++i)
			estimates[i] = calculateProbability(i, featureArr);
		
		// Find and return best guess
		int bestGuess = 0;
		for (int i=1; i<estimates.length; ++i)
			if (estimates[i] > estimates[bestGuess])
				bestGuess = i;
		
		return bestGuess;
	}
	
	private static double calculateProbability(int classifier, double[] values) {
		double probability = classArr[classifier];
		for (int feature = 0; feature<4; ++feature)
			probability *= probabilityDensityFunction(feature, classifier, values[feature]);
		
		return probability;
	}
	
	private static double probabilityDensityFunction(int feature, int classifier, double x) {
		double stdDevSqrd = varianceArr[classifier][feature] * varianceArr[classifier][feature];
		double xMeanDiffSqrd = (x - meanArr[classifier][feature]) * (x - meanArr[classifier][feature]);
		return ( 1/Math.sqrt(2 * Math.PI * stdDevSqrd) ) * ( Math.exp(-xMeanDiffSqrd / (2 * stdDevSqrd) ) );
	}
	
	public static void main(String[] args) {
		Object[][] shuffledData, testSet, trainingSet;
		classificationArr = new String[0];
		
		rawData = loadDataFromFile("iris.data");
		shuffledData = shuffleData(rawData);
		testSet = Arrays.copyOfRange(shuffledData, 0, shuffledData.length/2);
		trainingSet = Arrays.copyOfRange(shuffledData, shuffledData.length/2, shuffledData.length); 
		
		trainNaiveBayes(trainingSet);

		System.out.println("Training success!\nClassifications loaded: ");
		for (int i=0; i<classificationArr.length; ++i)
			System.out.println("\t" + classificationArr[i]);
		
		double testResults = testNaiveBayes(testSet);
		System.out.println("Test complete. ");
		System.out.println("Percentage properly categorized: " + testResults);
	}

}