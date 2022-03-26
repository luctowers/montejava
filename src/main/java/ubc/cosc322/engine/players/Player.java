package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.util.IntList;

/** An interface that abstracts a Game of Amazons player. */
public abstract class Player {
	
	protected State state;

	public void useState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public void doTurn(Turn turn) {
		state.doTurn(turn);
	}

	public void doMove(int move) {
		state.doMove(move);
	}

	public abstract void suggestAndDoMoves(int maxMoves, IntList output);

	public Turn suggestAndDoTurn() {
		// DEBUG ASSERTION
		if (state.getNextMoveType() != MoveType.PICK_QUEEN) {
			throw new IllegalStateException("when suggesting a turn, it must be the start of a turn");
		}
		IntList moves = new IntList(3);
		int lastSize = 0;
		while (moves.size() != 3) {
			suggestAndDoMoves(3 - lastSize, moves);
			int received = moves.size() - lastSize;
			if (received == 0) {
				return null;
			}
			lastSize = moves.size();
		}
		return new Turn(
			moves.get(0),
			moves.get(1),
			moves.get(2)
		);
	}

}
