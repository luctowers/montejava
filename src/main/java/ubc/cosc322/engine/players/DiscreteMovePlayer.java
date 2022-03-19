package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.Turn;

public abstract class DiscreteMovePlayer extends Player {
	
	@Override
	public Turn suggestTurn() {
		Move queenMove = suggestMove();
		if (queenMove == null) {
			return null;
		}
		state.doMove(queenMove);
		Move arrowMove = suggestMove();
		if (arrowMove == null) {
			return null;
		}
		state.undoMove();
		return new Turn(queenMove, arrowMove);
	}

}
