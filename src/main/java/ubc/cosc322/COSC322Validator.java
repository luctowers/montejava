// package ubc.cosc322;

// import java.util.List;

// import ubc.cosc322.engine.core.Move;
// import ubc.cosc322.engine.core.State;
// import ubc.cosc322.engine.core.Turn;
// import ubc.cosc322.engine.generators.LegalMoveGenerator;

// public class COSC322Validator {

// 	private static LegalMoveGenerator generator = new LegalMoveGenerator();
	
// 	public static boolean validateAndLog(State state, Turn turn) {
// 		boolean result = validate(state, turn);
// 		if (result) {
// 			System.out.println("RECEIVED MOVE IS VALID");
// 		} else {
// 			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
// 			System.out.println("! RECEIVED MOVE IS NOT VALID !");
// 			System.out.println("! RECEIVED MOVE IS NOT VALID !");
// 			System.out.println("! RECEIVED MOVE IS NOT VALID !");
// 			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
// 		}
// 		return result;
// 	}

// 	public static boolean validate(State state, Turn turn) {
// 		state = state.clone();
// 		List<Move> moves = generator.generateMoves(state);
// 		if (!moves.contains(turn.queenMove())) {
// 			return false;
// 		}
// 		state.doMove(turn.queenMove());
// 		moves = generator.generateMoves(state);
// 		if (!moves.contains(turn.arrowMove())) {
// 			return false;
// 		}
// 		return true;
// 	}

// }
