package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;

public interface Player {
	
	public void useState(State state);

	public void doMove(Move move);

	public Move suggestMove();

}
