package de.uni_leipzig.simba.memorymanagement.lazytsp.parallel.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni_leipzig.simba.memorymanagement.Index.planner.MPCNSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.MPVNSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.SortingSolver;

public class MPVNSolverTest {

	static int matrixSize=12;
	private static void printMatrix(double[][] matrix)
	{
		for(int i=0;i<matrixSize;i++)
		{
			for(int j=0;j<matrixSize;j++)
				System.out.print(matrix[i][j]+"\t");
			System.out.println();
		}
	}
	private static void drawMatrix(double[][] matrix)
	{
		for(int i=0;i<matrixSize;i++)
		{
			for(int j=i;j<matrixSize;j++)
				if(matrix[i][j]> 0)
					System.out.println(i+"---"+matrix[i][j]+"---"+j);
		}
	}
	private static int getMaxNodeInMatrix(double[][] matrix)
	{
		double maxEdgeWeight =-1;
		int maxNode=-1;
		for(int i=0; i< matrixSize;i++)
			for(int j=0;j<matrixSize;j++)
			{
				if(matrix[i][j]> maxEdgeWeight)
				{
					maxNode=i;
					maxEdgeWeight = matrix[i][j];
				}
			}
		return maxNode;
	}
	public static void main(String[] args) {
		double[][] matrix=MatricesInitializer.getMatrix(7);
		matrixSize = matrix[0].length;
		printMatrix(matrix);
		drawMatrix(matrix);
		MPVNSolver solver = new MPVNSolver();
		solver.clustersSize = matrixSize;
		solver.setMaxICluster(getMaxNodeInMatrix(matrix));

		int[] path = solver.getMPRNPath(matrix);

		for (int i : path) {
			System.out.print(i+"\t");
		}
		

	}

}
