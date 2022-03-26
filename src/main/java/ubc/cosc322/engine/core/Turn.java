package ubc.cosc322.engine.core;

/** A single turn in the Game of Amazons. A turn has two moves, a queen move and an arrow move. */
public class Turn {
	
	/** The positions involved in a turn.
	 * Each positional must be orthogonal or diagonal to the preceding position.
	 */
	public final int queenPick, queenMove, arrowShot;

	public Turn(int queenPick, int queenMove, int arrowShot) {
		this.queenPick = queenPick;
		this.queenMove = queenMove;
		this.arrowShot = arrowShot;
	}

}
