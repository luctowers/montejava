package ubc.cosc322.engine.players;

import java.util.Random;

import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.data.IntList;
import ubc.cosc322.engine.generators.MoveGenerator;

/** A player that uses random generator to pick randomly between moves. */
public class RandomMovePlayer extends Player {
	
	private MoveGenerator moveGenerator;
	private Random randomGenerator;
	private IntList moveBuffer;

	public RandomMovePlayer(MoveGenerator moveGenerator) {
		this.moveGenerator = moveGenerator;
		this.randomGenerator = new Random();
	}

	@Override
	public void useBoard(Board board) {
		super.useBoard(board);
		if (moveBuffer == null || board.getMaxMovesAbsolute() > moveBuffer.capacity()) {
			this.moveBuffer = new IntList(board.getMaxMovesAbsolute());
		}
	}

	@Override
	public void suggestAndDoMoves(int maxMoves, IntList output) {
		for (int i = 0; i < maxMoves; i++) {
			moveBuffer.clear();
			moveGenerator.generateMoves(board, moveBuffer);
			if (moveBuffer.size() == 0) {
				return;
			}
			int randomIndex = randomGenerator.nextInt(moveBuffer.size());
			int randomMove = moveBuffer.get(randomIndex);
			doMove(randomMove);
			output.push(randomMove);
		}
	}

}
