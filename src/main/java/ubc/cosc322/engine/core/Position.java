package ubc.cosc322.engine.core;

/** A position on the Game of Amazons board. */
public class Position {
	
    /** Zero-based coordinates of the position. */
	public final int x, y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

    /** Returns a new position offset by a direction.
     * @param direction The direction to offset in.
     * @return The new position.
     */
	public Position offset(Direction direction) {
		return new Position(x + direction.x, y + direction.y);
	}

    public boolean equals(Position other) {
        return x == other.x && y == other.y;
    }
	
}
