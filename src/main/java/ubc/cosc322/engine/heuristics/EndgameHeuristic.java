package ubc.cosc322.engine.heuristics;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.util.IntList;

public class EndgameHeuristic implements Heuristic {

	@Override
	public int evaluate(Board board) {

		board.computeChambers();
		IntList allChambers = board.getQueenChambers();
		IntList whiteChambers = board.getWhiteChambers();
		IntList blackChambers = board.getBlackChambers();

		int territory = 0;
		territory -= whiteChambers.size();
		territory += blackChambers.size();

		// TODO: is get(0) safe ?
		int count = 1;
		int currentChamber = allChambers.get(0);
		for (int i = 1; i <= allChambers.size(); i++) {
			if (i < allChambers.size() && allChambers.get(i) == currentChamber) {
				count++;
			} else {
				int chamberSize = board.getChamberSize(currentChamber);
				if (whiteChambers.contains(currentChamber)) {
					territory += chamberSize;
				}
				if (blackChambers.contains(currentChamber)) {
					territory -= chamberSize;
				}
				if (count <= chamberSize) {
					if (whiteChambers.contains(currentChamber) && blackChambers.contains(currentChamber)) {
						return 0;
					}
				}
				count = 0;
				if (i < allChambers.size()) {
					currentChamber = allChambers.get(i);
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
