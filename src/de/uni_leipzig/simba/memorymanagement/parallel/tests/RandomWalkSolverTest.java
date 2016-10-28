package de.uni_leipzig.simba.memorymanagement.parallel.tests;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uni_leipzig.simba.memorymanagement.Index.planner.MPVNSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.RandomWalkSolver;

public class RandomWalkSolverTest {

	static int matrixSize=12;

	private static Map<Integer,List<Integer>> convertMatrixToGraph(double[][] matrix)
	{
		Map<Integer,List<Integer>> graph = new LinkedHashMap<Integer, List<Integer>>();
		int size = matrix[0].length;
		for(int i =0; i<size; i++)
		{
			List<Integer> neighbor = new ArrayList<Integer>();
			for(int j=0; j< size; j++)
			{
				if(i != j && matrix[i][j] > 0 )
				{
					neighbor.add(j);
				}
			}
			graph.put(i, neighbor);
		}
		
		return graph;
	}
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
	public static void main(String[] args) {
		double[][] matrix=MatricesInitializer.getMatrix(9);
		matrixSize = matrix[0].length;
		printMatrix(matrix);
		drawMatrix(matrix);
		RandomWalkSolver solver = new RandomWalkSolver();
		solver.clustersSize = matrixSize;
		Map<Integer,List<Integer>>  graph = convertMatrixToGraph(matrix);
		int[] path = solver.getRandomWalkPath(graph);

		for (int i : path) {
			System.out.print(i+"\t");
		}

	}

}
