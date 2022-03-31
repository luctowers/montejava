package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.util.IntList;

public class EndgameHeuristic implements Heuristic {

	IntList whiteChambers;
	IntList blackChambers;

	public EndgameHeuristic() {
		this.whiteChambers = new IntList(Board.MAX_QUEENS_PER_COLOR);
		this.blackChambers = new IntList(Board.MAX_QUEENS_PER_COLOR);
	}

	@Override
	public int evaluate(Board board) {
		int territory = 0;
		whiteChambers.clear();
		blackChambers.clear();
		IntList whiteQueens = board.getUntrappedQueens(Color.WHITE);
		IntList blackQueens = board.getUntrappedQueens(Color.BLACK);
		territory -= whiteQueens.size();
		territory += blackQueens.size();
		int maxQueens = Math.max(whiteQueens.size(), blackQueens.size());
		for (int q = 0; q < maxQueens; q++) {
			if (q < whiteQueens.size()) {
				int queen = whiteQueens.get(q);
				int chamber = board.getPositionChamber(queen);
				if (blackChambers.contains(chamber)) {
					return 0;
				}
				if (!whiteChambers.contains(chamber)) {
					whiteChambers.push(chamber);
					territory += board.getChamberSize(chamber);
				}
			}
			if (q < blackQueens.size()) {
				int queen = whiteQueens.get(q);
				int chamber = board.getPositionChamber(queen);
				if (whiteChambers.contains(chamber)) {
					return 0;
				}
				if (!blackChambers.contains(chamber)) {
					blackChambers.push(chamber);
					territory -= board.getChamberSize(chamber);
				}
			}
		}
		if (territory != 0) {
			return territory;
		} else {
			switch (board.getColorToMove()) {
				case WHITE:
					return -1;
				case BLACK:
					return 1;
				default:
					throw new IllegalStateException("invalid color");
			}
		}
	}
	
}
