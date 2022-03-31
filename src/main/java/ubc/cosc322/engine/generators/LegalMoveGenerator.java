package ubc.cosc322.engine.generators;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.util.IntList;

/** A MoveGenerator implementation that returns all legal moves. */
public class LegalMoveGenerator implements MoveGenerator {

	@Override
	/** Return a list of a legal moves in a position. */
	public void generateMoves(Board board, IntList output) {
		board.generateMoves(output);
	}
	
}
