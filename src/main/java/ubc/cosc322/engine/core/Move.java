package ubc.cosc322.engine.core;

/** A single move in the Game of Amazons. */
public class Move {
	
	/** The move type. One of QUEEN or ARROW */
	public final MoveType type;

	/** The positions involved in a move.
	 * For QUEEN moves, source is the orignal queen position and destination is 
	 * the new queen position.
	 * For ARROW moves, source is the queen shooting the arrow and destination
	 * is the arrow's location.
	 */
	public final Position source, destination;

	public Move(MoveType type, Position source, Position destination) {
		this.type = type;
		this.source = source;
		this.destination = destination;
	}

	public boolean equals(Move other) {
        return source == other.source && destination == other.destination && type == other.type;
    }

}
