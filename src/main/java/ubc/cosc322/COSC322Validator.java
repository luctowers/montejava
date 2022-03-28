package ubc.cosc322;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.util.IntList;

public class COSC322Validator {
	
	public static boolean validateAndLog(State state, Turn turn) {
		boolean result = validate(state, turn);
		if (result) {
			System.out.println("RECEIVED MOVE IS VALID");
		} else {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.out.println("! RECEIVED MOVE IS NOT VALID !");
			System.out.println("! RECEIVED MOVE IS NOT VALID !");
			System.out.println("! RECEIVED MOVE IS NOT VALID !");
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return result;
	}

	public static boolean validate(State state, Turn turn) {
		state = state.clone();
		IntList moves = new IntList(state.dimensions.maxTrace);
		state.generateMoves(moves);
		if (moves.search(turn.queenPick) == -1) {
			return false;
		}
		state.doMove(turn.queenPick);
		moves.clear();
		state.generateMoves(moves);
		if (moves.search(turn.queenMove) == -1) {
			return false;
		}
		state.doMove(turn.queenMove);
		moves.clear();
		state.generateMoves(moves);
		if (moves.search(turn.arrowShot) == -1) {
			return false;
		}
		return true;
	}

}
