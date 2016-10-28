package de.uni_leipzig.simba.memorymanagement.pathfinder;


import de.uni_leipzig.simba.memorymanagement.Index.planner.SortingSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;

public class SolverFactory {
	public static PathFinder createSolver(SolverType solverType)
	{
		if(solverType.equals(SolverType.TSPSolver))
			return new TSPSolver();
		else if (solverType.equals(SolverType.GreedySolver))
			return new GreedySolver();
		else if(solverType.equals(SolverType.SortingSolver))
			return new SortingSolver();
		else
			return new SimpleSolver();

	}
}
