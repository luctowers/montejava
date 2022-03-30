package ubc.cosc322.engine.core;

/** A single turn in the Game of Amazons. A turn has two moves, a queen move and an arrow move. */
public class Turn {
	
	public final int queenMove, arrowMove;

	public Turn(int queenMove, int arrowMove) {
		this.queenMove = queenMove;
		this.arrowMove = arrowMove;
	}

	public Turn(int queenSource, int queenDestination, int arrowMove) {
		this.queenMove = Move.encodeQueenMove(queenSource, queenDestination);
		this.arrowMove = arrowMove;
	}

	public int getQueenSource() {
		return Move.decodeQueenSource(queenMove);
	}

	public int getQueenDestination() {
		return Move.decodeQueenDestination(queenMove);
	}

	public int getArrowDestination() {
		return arrowMove;
	}

}
