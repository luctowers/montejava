package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;

/** An interface that abstracts a Game of Amazons player. */
public abstract class Player {
	
	protected State state;

	public void useState(State state) {
		this.state = state;
	}

	public void doMove(Move move) {
		state.doMove(move);
	}

	public void doTurn(Turn turn) {
		state.doMove(turn.queenMove());
		state.doMove(turn.arrowMove());
	}

	public abstract Move suggestMove();

	public abstract Turn suggestTurn();

}
