package ubc.cosc322.engine.core;

public class Turn {
	
	/** The positions involved in a turn.
	 * Each positional must be orthogonal or diagonal to the preceding position.
	 */
	public final Position queenSource, queenDestination, arrowDestination;

	public Turn(Position queenSource, Position queenDestination, Position arrowDestination) {
		this.queenSource = queenSource;
		this.queenDestination = queenDestination;
		this.arrowDestination = arrowDestination;
	}

	public Turn(Move queenMove, Move arrowMove) {
		if (queenMove.type != MoveType.QUEEN) {
			throw new IllegalArgumentException("the first move in a turn must be a queen move");
		}
		if (arrowMove.type != MoveType.ARROW) {
			throw new IllegalArgumentException("the second move in a turn must be a arrow move");
		}
		if (!queenMove.destination.equals(arrowMove.source)) {
			throw new IllegalArgumentException("arrow move source must be equal to queen destination");
		}
		this.queenSource = queenMove.source;
		this.queenDestination = queenMove.destination;
		this.arrowDestination = arrowMove.destination;
	}

	public Move queenMove() {
		return new Move(MoveType.QUEEN, queenSource, queenDestination);
	}

	public Move arrowMove() {
		return new Move(MoveType.ARROW, queenDestination, arrowDestination);
	}

}
