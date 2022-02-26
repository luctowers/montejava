package ubc.cosc322.engine.generators;

import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;

/** An interface to abstract move generation for positions in the Game of Amazons. */
public interface MoveGenerator {
	
	/** Returns an arbitrary subset of the legal moves in a given position. */
	public List<Move> generateMoves(State state);

}
