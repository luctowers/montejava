package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.util.IntList;

/** An interface that abstracts a Game of Amazons player. */
public abstract class Player {
	
	protected Board board;

	public void useState(Board board) {
		this.board = board;
	}

	public Board getState() {
		return board;
	}

	public void doTurn(Turn turn) {
		board.doTurn(turn);
	}

	public void doMove(int move) {
		board.doMove(move);
	}

	public abstract void suggestAndDoMoves(int maxMoves, IntList output);

	public Turn suggestAndDoTurn() {
		// DEBUG ASSERTION
		// if (board.getNextMoveType() != MoveType.PICK_QUEEN) {
		// 	throw new IllegalStateException("when suggesting a turn, it must be the start of a turn");
		// }
		IntList moves = new IntList(MoveType.COUNT);
		int lastSize = 0;
		while (moves.size() != MoveType.COUNT) {
			suggestAndDoMoves(MoveType.COUNT - lastSize, moves);
			int received = moves.size() - lastSize;
			if (received == 0) {
				return null;
			}
			lastSize = moves.size();
		}
		return new Turn(
			moves.get(0),
			moves.get(1)
		);
	}

}
