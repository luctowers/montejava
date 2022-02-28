package ubc.cosc322.engine.core;

/** The three pieces in the Game of Amazons. */
public enum Piece {

	WHITE_QUEEN, BLACK_QUEEN, ARROW;

	/** Returns the queen of the corresponding color.
	 * @param color One of WHITE or BLACK
	 * @return One of WHITE_QUEEN or BLACK_QUEEN
	 */
	public static Piece queenOfColor(Color color) {
		if (color == Color.WHITE) {
			return WHITE_QUEEN;
		} else if (color == Color.BLACK) {
			return BLACK_QUEEN;
		} else {
			return null;
		}
	}

}
