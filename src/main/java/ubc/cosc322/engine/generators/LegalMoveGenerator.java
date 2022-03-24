package ubc.cosc322.engine.generators;

import java.util.ArrayList;
import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.State;

/** A MoveGenerator implementation that returns all legal moves. */
public class LegalMoveGenerator implements MoveGenerator {

	@Override
	/** Return a list of a legal moves in a position. */
	public ArrayList<Move> generateMoves(State state) {
		return state.generateMoves();
	}
	
}
