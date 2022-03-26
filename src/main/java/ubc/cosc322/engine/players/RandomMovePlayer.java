package ubc.cosc322.engine.players;

import java.util.Random;

import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.MoveGenerator;
import ubc.cosc322.engine.util.IntList;

public class RandomMovePlayer extends Player {
	
	private Random randomGenerator;
	private MoveGenerator moveGenerator;
	private IntList moveBuffer;

	public RandomMovePlayer(MoveGenerator moveGenerator) {
		this.moveGenerator = moveGenerator;
		this.randomGenerator = new Random();
	}

	@Override
	public void useState(State state) {
		this.state = state;
		this.moveBuffer = new IntList(state.dimensions.maxTrace);
	}

	@Override
	public void suggestAndDoMoves(int maxMoves, IntList output) {
		moveGenerator.generateMoves(state, moveBuffer);
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
