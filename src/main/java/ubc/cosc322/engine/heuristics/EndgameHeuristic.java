package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.util.IntList;

public class EndgameHeuristic implements Heuristic {

	IntList whiteChambers;
	IntList blackChambers;
	IntList allChambers;

	public EndgameHeuristic() {
		this.whiteChambers = new IntList(Board.MAX_QUEENS_PER_COLOR);
		this.blackChambers = new IntList(Board.MAX_QUEENS_PER_COLOR);
		this.allChambers = new IntList(2*Board.MAX_QUEENS_PER_COLOR);
	}

	@Override
	public int evaluate(Board board) {
		int territory = 0;
		whiteChambers.clear();
		blackChambers.clear();
		allChambers.clear();
		IntList whiteQueens = board.getUntrappedQueens(Color.WHITE);
		IntList blackQueens = board.getUntrappedQueens(Color.BLACK);
		territory -= whiteQueens.size();
		territory += blackQueens.size();
		int maxQueens = Math.max(whiteQueens.size(), blackQueens.size());
		for (int q = 0; q < maxQueens; q++) {
			if (q < whiteQueens.size()) {
				int queen = whiteQueens.get(q);
				int chamber = board.getPositionChamber(queen);
				if (blackChambers.contains(chamber) && board.getChamberSize(chamber) > 4) {
					return 0;
				}
				allChambers.push(chamber);
				if (!whiteChambers.contains(chamber)) {
					whiteChambers.push(chamber);
					territory += board.getChamberSize(chamber);
				}
			}
			if (q < blackQueens.size()) {
				int queen = blackQueens.get(q);
				int chamber = board.getPositionChamber(queen);
				if (whiteChambers.contains(chamber) && board.getChamberSize(chamber) > 4) {
					return 0;
				}
				allChambers.push(chamber);
				if (!blackChambers.contains(chamber)) {
					blackChambers.push(chamber);
					territory -= board.getChamberSize(chamber);
				}
			}
		}
		allChambers.sort();
		int count = 1;
		int currentChamber = allChambers.get(0);
		for (int i = 1; i < allChambers.size(); i++) {
			int nextChamber = allChambers.get(i);
			if (nextChamber == currentChamber) {
				count++;
			} else {
				if (count < board.getChamberSize(currentChamber)) {
					if (whiteChambers.contains(currentChamber) && blackChambers.contains(currentChamber)) {
						return 0;
					}
				}
				currentChamber = nextChamber;
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
