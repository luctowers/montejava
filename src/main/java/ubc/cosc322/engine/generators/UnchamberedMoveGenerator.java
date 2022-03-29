package ubc.cosc322.engine.generators;

import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.util.IntList;

public class UnchamberedMoveGenerator implements MoveGenerator {

	@Override
	public void generateMoves(State state, IntList output) {
		int outputBase = output.size();
		state.generateMoves(output);
		if (state.getNextMoveType() == MoveType.PICK_QUEEN) {
			IntList unchamberedQueens = state.getUnchamberedQueens(state.getColorToMove());
			if (unchamberedQueens.size() > 1) {
				for (int i = output.size() - 1; i >= outputBase; i--) {
					if (unchamberedQueens.search(output.get(i)) == -1) {
						output.removeIndex(i);
					}
				}
			}
			// TODO: make this not needed
			if (output.size() == 0) {
				state.generateMoves(output);
			}
		}
	}
	
}
