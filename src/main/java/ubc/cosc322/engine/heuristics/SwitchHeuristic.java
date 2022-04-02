package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;

public class SwitchHeuristic implements Heuristic {

	private Heuristic h1, h2;
	private int switchAferMove;

	public SwitchHeuristic(Heuristic h1, Heuristic h2, int switchAferMove) {
		this.h1 = h1;
		this.h2 = h2;
		this.switchAferMove = switchAferMove;
	}

	@Override
	public int evaluate(Board board) {
		if (board.getMoveCount() > switchAferMove) {
			return h2.evaluate(board);
		} else {
			return h1.evaluate(board);
		}
	}
	
}
