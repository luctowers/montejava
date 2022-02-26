package ubc.cosc322.engine.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The 8 directions pieces can move in the Game of Amazons. */
public enum Direction {

	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0),
	UP_LEFT(-1, -1), UP_RIGHT(1, -1), DOWN_RIGHT(1, 1), DOWN_LEFT(-1, 1);

	/** Coordinate offsets. One of (-1, 0, 1). */
	public final int x, y;

	/** A list of all directions, without generating a new array aeach time. */
	public static final List<Direction> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

	private Direction(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
