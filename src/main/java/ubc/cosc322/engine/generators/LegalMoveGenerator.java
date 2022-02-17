package ubc.cosc322.engine.generators;

import java.util.ArrayList;
import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;

public class LegalMoveGenerator implements MoveGenerator {

	@Override
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
