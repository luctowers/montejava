package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.util.IntList;

public class PartialRolloutHeuristic implements Heuristic {
	
	private Player player;
	private Heuristic heuristic;
	private IntList simulationBuffer;

	public PartialRolloutHeuristic(Player player, Heuristic heuristic, int maxMoves) {
		this.player = player;
		this.heuristic = heuristic;
		this.simulationBuffer = new IntList(maxMoves);
	}

	@Override
	public int evaluate(State state) {
		player.useState(state);
		int totalMoves = 0;
		do {
			simulationBuffer.clear();
			player.suggestAndDoMoves(simulationBuffer.capacity() - simulationBuffer.size(), simulationBuffer);
			totalMoves += simulationBuffer.size();
		} while (totalMoves < simulationBuffer.capacity() && simulationBuffer.size() != 0);
		simulationBuffer.clear();
		state.generateMoves(simulationBuffer);
		if (simulationBuffer.size() == 0) {
			if (state.getColorToMove() == Color.WHITE) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return heuristic.evaluate(state);
		}
	}

}
