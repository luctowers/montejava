package ubc.cosc322.engine.players;

import java.util.List;
import java.util.Random;

import ubc.cosc322.engine.core.Direction;
import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;

public class FastRandomPlayer implements Player {

	private State state;
	private int maxFastAttempts;
	private Random randomGenerator;

	public FastRandomPlayer(int maxFastAttempts) {
		this.maxFastAttempts = maxFastAttempts;
		this.randomGenerator = new Random();
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	@Override
	public void play(Move move) {
		state.doMove(move);
	}

	@Override
	public Move play() {
		Move move = null;
		for (int i = 0; i < maxFastAttempts && move == null; i++) {
			move = randomMove();
		}
		if (move == null) {
			move = greedyMove();
		}
		if (move == null) {
			return null;
		}
		state.doMove(move);
		return move;
	}

	private Move randomMove() {
		switch (state.getNextMoveType()) {
			case QUEEN:
				return randomQueenMove();
			case ARROW:
				return randomArrowMove();
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
	}

	private Move randomQueenMove() {
		Position queen = randomQueen();
		if (queen == null) {
			return null;
		}
		Direction direction = randomDirection();
		Position destination = randomTrace(queen, direction);
		if (destination == null) {
			return null;
		}
		return new Move(MoveType.QUEEN, queen, destination);
	}

	private Move randomArrowMove() {
		Position queen = state.getLastMovedQueen();
		Direction direction = randomDirection();
		Position destination = randomTrace(queen, direction);
		if (destination == null) {
			return null;
		}
		return new Move(MoveType.ARROW, queen, destination);
	}

	private Direction randomDirection() {
		int directionIndex = randomGenerator.nextInt(Direction.VALUES.size());
    	return Direction.VALUES.get(directionIndex);
	}

	private Position randomQueen() {
		List<Position> queens = state.getQueens(state.getColorToMove());
		if (queens.size() == 0) {
			return null;
		}
		int queenIndex = randomGenerator.nextInt(queens.size());
    	return queens.get(queenIndex);
	}

	private Position randomTrace(Position source, Direction direction) {
		List<Position> positions = state.traceDirection(source, direction);
		if (positions.size() == 0) {
			return null;
		}
		int index = randomGenerator.nextInt(positions.size());
		return positions.get(index);
	}

	private Move greedyMove() {
		switch (state.getNextMoveType()) {
			case QUEEN:
				return greedyQueenMove();
			case ARROW:
				return greedyArrowMove();
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
	}

	private Move greedyQueenMove() {
		for (Position queen : state.getQueens(state.getColorToMove())) {
			for (Direction direction : Direction.VALUES) {
				Position destination = randomTrace(queen, direction);
				if (destination != null) {
					return new Move(MoveType.QUEEN, queen, destination);
				}
			}
		}
		return null;
	}

	private Move greedyArrowMove() {
		Position queen = state.getLastMovedQueen();
		for (Direction direction : Direction.VALUES) {
			Position destination = randomTrace(queen, direction);
			if (destination != null) {
				return new Move(MoveType.ARROW, queen, destination);
			}
		}
		return null;
	}
	
}
