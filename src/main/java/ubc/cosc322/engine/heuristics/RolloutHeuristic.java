package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.util.IntList;

public class RolloutHeuristic implements Heuristic {

	private Player player;
	IntList simulationBuffer;

	public RolloutHeuristic(Player player) {
		this.player = player;
		this.simulationBuffer = new IntList(300);
	}

	@Override
	public int evaluate(Board board) {
		player.useState(board);
		do {
			simulationBuffer.clear();
			player.suggestAndDoMoves(simulationBuffer.capacity(), simulationBuffer);
		} while (simulationBuffer.size() != 0);
		if (board.getColorToMove() == Color.WHITE) {
			return -1;
		} else {
			return 1;
		}
	}
	
}