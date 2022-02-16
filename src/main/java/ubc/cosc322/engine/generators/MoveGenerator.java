package ubc.cosc322.engine.generators;

import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;

public interface MoveGenerator {
	
	public List<Move> generateMoves(State state);

}
