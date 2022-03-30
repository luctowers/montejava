package ubc.cosc322.engine.core;

public class Move {
	
	public static int encodeQueenMove(int queenSource, int queenDestination) {
		return (queenDestination << 16) | queenSource;
	}

	public static int decodeQueenSource(int queenMove) {
		return queenMove & 0xFFFF;
	}

	public static int decodeQueenDestination(int queenMove) {
		return queenMove >> 16;
	}

}
