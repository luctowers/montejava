package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;

/** A fast heuristic based of the mobility of a color's queens. */
public class MobilityHeuristic implements Heuristic {

	@Override
	/** Returns the difference of the number new queen postions that a immediately reacheable by each team. */
	public int evaluate(State state) {
		int heuristic = 0;
		// compute for both white and black
		for (Color color : Color.values()) {
			// count the positions that can be reached by queens of that color
			int mobility = 0;
			for (int source : state.getQueens(color)) {
				// TODO: FIX THIS
				// mobility += state.traceAll(source).size();
			}
			// white mobility makes heuristic positive, black makes it negative
			if (color == Color.WHITE) {
				heuristic += mobility;
			} else if (color == Color.BLACK) {
				heuristic -= mobility;
			}
		}
		return heuristic;
	}
	
}
