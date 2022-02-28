package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;

/** An interface that abstracts a Game of Amazons player. */
public abstract class Player {
	
	protected State state;

	public void useState(State state) {
		this.state = state;
	}

	public void doMove(Move move) {
		state.doMove(move);
	}

	public abstract Move suggestMove();

}
