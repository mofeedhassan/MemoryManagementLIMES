package de.uni_leipzig.simba.memorymanagement.parallel.tests;

import de.uni_leipzig.simba.memorymanagement.Index.planner.MPCNSolver;

public class MPCNSolverTest {
	static int matrixSize=0;
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
					System.out.println(i+"---"+j+"---"+matrix[i][j]);
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
		double[][] matrix=MatricesInitializer.getMatrix(1);
		matrixSize = matrix[0].length;
		printMatrix(matrix);
		drawMatrix(matrix);
		MPCNSolver solver = new MPCNSolver();
		solver.clustersSize = matrixSize;
		solver.setMaxICluster(getMaxNodeInMatrix(matrix));
		
		int[] path = solver.getMPRNPath(matrix);

		for (int i : path) {
			System.out.print(i+"\t");
		}

	}
}
