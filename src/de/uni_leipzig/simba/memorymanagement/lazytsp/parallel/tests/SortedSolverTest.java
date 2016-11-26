package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_leipzig.simba.memorymanagement.Index.planner.SortingSolver;
import de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.SortedSolvertmp;

public class SortedSolverTest {
	final int matrixSize=12;
	@Test
	public void test() {
		double[][] matrix= createClustersMatrix();
		printMatrix(matrix);
		SortingSolver solver = new SortingSolver();
		Object[] sortedClusters = solver.sortingDistances(matrix);
		int[] path = new int[sortedClusters.length];
    	for (int i = 0; i < sortedClusters.length; i++) {
    		path[i] = (int)sortedClusters[i];
    	}
		for (int i : path) {
			System.out.print(i+"\t");
		}
		assertTrue(true);
	}
	private double[][] createClustersMatrix()
	{

		double[][] matrix = new double[matrixSize][matrixSize];
		matrix[0][1]=17;
		matrix[0][3]=11;

		matrix[1][0]=17;
		matrix[1][6]=8;
		matrix[1][2]=20;

		matrix[2][7]=15;
		matrix[2][1]=20;
		matrix[2][3]=7;
		matrix[2][10]=6;
		
		matrix[3][2]=7;
		matrix[3][4]=5;
		matrix[3][0]=11;
		
		matrix[4][3]=5;
		
		matrix[5][9]=10;
		
		matrix[6][7]=12;
		matrix[6][1]=8;
		matrix[6][10]=5;
		
		matrix[7][2]=15;
		matrix[7][6]=12;
		matrix[7][8]=13;
		
		matrix[8][7]=13;
		matrix[8][9]=13;
		
		matrix[9][5]=10;
		matrix[9][8]=13;
		
		matrix[10][6]=15;
		matrix[10][2]=6;
		matrix[10][11]=8;
		
		matrix[11][10]=8;
		return matrix;
	}
	private void printMatrix(double[][] matrix)
	{
		for(int i=0;i<matrixSize;i++)
		{
			for(int j=0;j<matrixSize;j++)
				System.out.print(matrix[i][j]+"\t");
			System.out.println();
		}
	}
}
