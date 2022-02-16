package ubc.cosc322.engine.core;

public enum Piece {

	WHITE_QUEEN, BLACK_QUEEN, ARROW;

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
