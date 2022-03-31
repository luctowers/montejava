package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.util.IntList;

public class HybridRolloutHeuristic implements Heuristic {

	private Player player;
	IntList simulationBuffer;
	EndgameHeuristic endgame;

	public HybridRolloutHeuristic(Player player) {
		this.player = player;
		this.simulationBuffer = new IntList(2);
		this.endgame = new EndgameHeuristic();
	}

	@Override
	public int evaluate(Board board) {
		player.useBoard(board);
		do {
			simulationBuffer.clear();
			player.suggestAndDoMoves(simulationBuffer.capacity(), simulationBuffer);
			int endgameEvaluation = endgame.evaluate(board);
			if (endgameEvaluation != 0) {
				return endgameEvaluation;
			}
		} while (simulationBuffer.size() != 0);
		if (board.getColorToMove() == Color.WHITE) {
			return -1;
		} else {
			return 1;
		}
	}
	
}
