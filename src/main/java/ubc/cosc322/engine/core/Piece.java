package ubc.cosc322.engine.core;

public class Piece {

	private Piece() {};

	public static final byte NONE = 0;
	public static final byte WHITE_QUEEN = 1;
	public static final byte BLACK_QUEEN = 2;
	public static final byte ARROW = 3;

	public static byte queenOfColor(Color color) {
		switch (color) {
			case WHITE:
				return WHITE_QUEEN;
			case BLACK:
				return BLACK_QUEEN;
			default:
				return NONE;
		}
	}

	public static Color colorOfQueen(byte queen) {
		switch (queen) {
			case WHITE_QUEEN:
				return Color.WHITE;
			case BLACK_QUEEN:
				return Color.BLACK;
			default:
				return null;
		}
	}

}
