package ubc.cosc322.engine.core;

public class Move {
	
	public final MoveType type;
	public final Position source, destination;

	public Move(MoveType type, Position source, Position destination) {
		this.type = type;
		this.source = source;
		this.destination = destination;
	}

}
