package ubc.cosc322.engine.players;

import java.util.Random;

import ubc.cosc322.engine.util.IntList;

public class RandomMovePlayer extends Player {
	
	private Random randomGenerator;
	private IntList moveBuffer;

	public RandomMovePlayer() {
		this.randomGenerator = new Random();
		this.moveBuffer = new IntList(4*35);
	}

	@Override
	public void suggestAndDoMoves(int maxMoves, IntList output) {
		for (int i = 0; i < maxMoves; i++) {
			board.generateMoves(moveBuffer);
			if (moveBuffer.size() == 0) {
				return;
			}
			int randomIndex = randomGenerator.nextInt(moveBuffer.size());
			int randomMove = moveBuffer.get(randomIndex);
			doMove(randomMove);
			output.push(randomMove);
			moveBuffer.clear();
		}
	}

}
