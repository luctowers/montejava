package ubc.cosc322.engine.core;

/** Distinguishes players in the Game of Amazons. */
public enum Color {

	WHITE, BLACK;

	/** Inverts the color. WHITE -> BLACK, BLACK -> WHITE
	 * @return The opposite color.
	 */
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
