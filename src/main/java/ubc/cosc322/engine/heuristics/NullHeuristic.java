package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.State;

/** A test heuristic that thinks every position is a draw. */
public class NullHeuristic implements Heuristic {

	@Override
	/** Always returns zero. */
	public int evaluate(State state) {
		return 0;
	}

}
