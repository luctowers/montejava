package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;

public class MobilityHeuristic implements Heuristic {

	@Override
	public int evaluate(State state) {
		int heuristic = 0;
		// compute for both white and black
		for (Color color : Color.values()) {
			// count the positions that can be reached by queens of that color
			int mobility = 0;
			for (Position source : state.getQueens(color)) {
				mobility += state.traceAll(source).size();
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
