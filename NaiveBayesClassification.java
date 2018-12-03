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
	
	private static Object[][] rawData;
	
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
	
	private static int getClassificationCount(int classIndex) {
		int count = 0;
		
		for (int i=0; i<rawData.length; ++i)
			if ((int)rawData[i][CLASSIFICATION] == classIndex)
				++count;
		
		return count;
	}
	
	private static double[] getFeatureValsByClass(int classIndex, int feature) {
		double[] values = new double[getClassificationCount(classIndex)];
		
		int index = 0;
		for (int i=0; i<rawData.length; ++i)
			if ((int)rawData[i][CLASSIFICATION] == classIndex) {
				values[index] = (double)rawData[i][feature];
				++index;
			}
		
		return values;
	}
	
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
	
	private static void trainNaiveBayes(Object[][] trainingData) {
		meanArr = new double[classificationArr.length][4];
		varianceArr = new double[classificationArr.length][4];
		
		// Fill Average Array
		for (int clsfctn = 0; clsfctn<classificationArr.length; ++clsfctn)
			for (int feature = 0; feature<4; ++feature)
				meanArr[clsfctn][feature] = Arrays.stream(getFeatureValsByClass(clsfctn, feature)).average().getAsDouble();
		
		// Fill Variance Array
	}
	
	private static void testNaiveBayes(Object[][] testData) {
		if (!trained)
			return;
	}
	
	private static int prediction() {
		if (!trained)
			return -1;
	}
	
	public static void main(String[] args) {
		Object[][] shuffledData, testSet, trainingSet;
		classificationArr = new String[0];
		boolean trained = false;
		
		rawData = loadDataFromFile("iris.data");
		shuffledData = shuffleData(rawData);
		testSet = Arrays.copyOfRange(shuffledData, 0, shuffledData.length/2);
		trainingSet = Arrays.copyOfRange(shuffledData, shuffledData.length/2, shuffledData.length); 
		
		trainNaiveBayes(trainingSet);
		testNaiveBayes(testSet);
	
		System.out.println("Training success!\nClassifications loaded: ");
		for (int i=0; i<classificationArr.length; ++i)
			System.out.println("\t" + classificationArr[i]);
		
		}
	}

}