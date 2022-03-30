package ubc.cosc322.engine.core;

public enum MoveType {

	QUEEN, ARROW;

	public static final int COUNT = 2;

	public MoveType next() {
		switch (this) {
			case QUEEN:
				return ARROW;
			case ARROW:
				return QUEEN;
		}
		return null;
	}

	public MoveType previous() {
		switch (this) {
			case QUEEN:
				return ARROW;
			case ARROW:
				return QUEEN;
		}
		return null;
	}

}
