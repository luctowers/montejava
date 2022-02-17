package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.State;

public class NullHeuristic implements Heuristic {

	@Override
	public int evaluate(State state) {
		return 0;
	}

}
