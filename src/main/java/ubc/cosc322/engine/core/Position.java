package ubc.cosc322.engine.core;

public class Position {
	
	public final int x, y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Position offset(Direction direction) {
		return new Position(x + direction.x, y + direction.y);
	}

	@Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Position)) {
            return false;
        }
        Position otherPosition = (Position) other;
        return x == otherPosition.x && y == otherPosition.y;
    }
	
}
