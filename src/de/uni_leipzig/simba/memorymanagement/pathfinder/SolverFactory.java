package de.uni_leipzig.simba.memorymanagement.pathfinder;


import de.uni_leipzig.simba.memorymanagement.Index.planner.SortingSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;

public class SolverFactory {
	private static int iterations=0;
	private static int optimizationTime=250;


	public static PathFinder createSolver(SolverType solverType, double solverParameter)
	{
			
		if(solverType.equals(SolverType.TSPSOLVER))
		{
			TSPSolver.iterations = (int) solverParameter;
			return new TSPSolver();
		}
		else if (solverType.equals(SolverType.GREEDYSOLVER))
			return new GreedySolver();
		else if(solverType.equals(SolverType.SORTINGSOLVER))
			return new SortingSolver();
		else
		{
			SimpleSolver.optimizationTime = solverParameter;
			return new SimpleSolver();
		}

	}
	// used for default solver and its values
	public static PathFinder createSolver(SolverType solverType)
	{
			
		if(solverType.equals(SolverType.TSPSOLVER))
		{
			TSPSolver.iterations = iterations;
			return new TSPSolver();
		}
		else if (solverType.equals(SolverType.GREEDYSOLVER))
			return new GreedySolver();
		else if(solverType.equals(SolverType.SORTINGSOLVER))
			return new SortingSolver();
		else
		{
			SimpleSolver.optimizationTime = optimizationTime;
			return new SimpleSolver();
		}

	}
}
