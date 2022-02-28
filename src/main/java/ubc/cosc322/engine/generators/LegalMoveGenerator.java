package ubc.cosc322.engine.generators;

import java.util.ArrayList;
import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;

/** A MoveGenerator implementation that returns all legal moves. */
public class LegalMoveGenerator implements MoveGenerator {

	@Override
	/** Return a list of a legal moves in a position. */
	public List<Move> generateMoves(State state) {
		switch (state.getNextMoveType()) {
			case QUEEN:
				return generateQueenMoves(state);
			case ARROW:
				return generateArrowMoves(state);
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
	}

	/** Return a list of a legal queen moves in a position. */
	private List<Move> generateQueenMoves(State state) {
		ArrayList<Move> moves = new ArrayList<>();
		for (Position source : state.getQueens(state.getColorToMove())) {
			for (Position destination : state.traceAll(source)) {
				moves.add(new Move(
					MoveType.QUEEN,
					source,
					destination
				));
			}
		}
		return moves;
	}

	/** Return a list of a arrow queen moves in a position. */
	private List<Move> generateArrowMoves(State state) {
		ArrayList<Move> moves = new ArrayList<>();
		Position source = state.getLastMovedQueen();
		for (Position destination : state.traceAll(source)) {
			moves.add(new Move(
				MoveType.ARROW,
				source,
				destination
			));
		}
		return moves;
	}
	
}
