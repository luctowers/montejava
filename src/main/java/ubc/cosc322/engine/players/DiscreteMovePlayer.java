// package ubc.cosc322.engine.players;

// import ubc.cosc322.engine.core.Move;
// import ubc.cosc322.engine.core.Turn;

// /** an abstract player who's suggestion of moves are entirely based on the internal
//  * state at the calling of suggest move.
//  */
// public abstract class DiscreteMovePlayer extends Player {
	
// 	@Override
// 	public Turn suggestTurn() {
// 		Move queenMove = suggestMove();
// 		if (queenMove == null) {
// 			return null;
// 		}
// 		state.doMove(queenMove);
// 		Move arrowMove = suggestMove();
// 		state.undoMove();
// 		if (arrowMove == null) {
// 			return null;
// 		}
// 		return new Turn(queenMove, arrowMove);
// 	}

// }
