package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;

public interface Player {
	
	public void setState(State state);

	public void play(Move move);

	public Move play();

}
