package ubc.cosc322.engine.core;

public enum Color {

	WHITE, BLACK;

	public Color other() {
		if (this == WHITE) {
			return BLACK;
		} else if (this == BLACK) {
			return WHITE;
		} else {
			return null;
		}
	}

}
