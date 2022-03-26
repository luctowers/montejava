package ubc.cosc322.engine.core;

public enum MoveType {

	PICK_QUEEN, MOVE_QUEEN, SHOOT_ARROW;

	public MoveType next() {
		switch (this) {
			case PICK_QUEEN:
				return MOVE_QUEEN;
			case MOVE_QUEEN:
				return SHOOT_ARROW;
			case SHOOT_ARROW:
				return PICK_QUEEN;
		}
		return null;
	}

	public MoveType previous() {
		switch (this) {
			case PICK_QUEEN:
				return SHOOT_ARROW;
			case MOVE_QUEEN:
				return PICK_QUEEN;
			case SHOOT_ARROW:
				return MOVE_QUEEN;
		}
		return null;
	}

}
