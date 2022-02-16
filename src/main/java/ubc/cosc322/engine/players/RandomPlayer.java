package ubc.cosc322.engine.players;

import java.util.List;
import java.util.Random;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.MoveGenerator;

public class RandomPlayer implements Player {
	
	private State state;
	private Random randomGenerator;
	private MoveGenerator moveGenerator;

	public RandomPlayer(MoveGenerator moveGenerator) {
		this.moveGenerator = moveGenerator;
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
		List<Move> moves = moveGenerator.generateMoves(state);
		if (moves.size() == 0) {
			return null;
		}
		int index = randomGenerator.nextInt(moves.size());
		Move move = moves.get(index);
		state.doMove(move);
		return move;
	}

}
