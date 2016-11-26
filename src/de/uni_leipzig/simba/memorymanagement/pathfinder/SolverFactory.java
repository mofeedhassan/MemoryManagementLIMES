package de.uni_leipzig.simba.memorymanagement.pathfinder;


import de.uni_leipzig.simba.memorymanagement.Index.planner.SortingSolver;
import de.uni_leipzig.simba.memorymanagement.Index.planner.TSPSolver;
import de.uni_leipzig.simba.memorymanagement.structure.SolverType;

public class SolverFactory {
	public static PathFinder createSolver(SolverType solverType/*, double solverParameter*/)
	{
		if(solverType.equals(SolverType.TSPSOLVER))
			return new TSPSolver();
		else if (solverType.equals(SolverType.GREEDYSOLVER))
			return new GreedySolver();
		else if(solverType.equals(SolverType.SORTINGSOLVER))
			return new SortingSolver();
		else
		{
			//SimpleSolver.optimizationTime = solverParameter;
			return new SimpleSolver();
		}

	}
}
