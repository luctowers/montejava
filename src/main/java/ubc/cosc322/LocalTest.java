package ubc.cosc322;

import java.util.Arrays;

import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.players.RandomPlayer;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1000;

		State state = new State(
			10, 10,
			Arrays.asList(
				new Position(0, 6),
				new Position(3, 9),
				new Position(6, 9),
				new Position(9, 6)
			),
			Arrays.asList(
				new Position(0, 3),
				new Position(3, 0),
				new Position(6, 0),
				new Position(9, 3)
			)
		);

		Player white = new RandomPlayer(new LegalMoveGenerator());
		Player black = new RandomPlayer(new LegalMoveGenerator());

		HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(state, white, black);
		System.out.println("iteration count: " + ITERATION_COUNT);

		long startTime = System.currentTimeMillis();
		analyzer.play(ITERATION_COUNT);
		long stopTime = System.currentTimeMillis();
		System.out.println("execution time: " + (stopTime - startTime) + " ms");

		System.out.println("white win-rate: " + analyzer.getWinRate(Color.WHITE));
		System.out.println("black win-rate: " + analyzer.getWinRate(Color.BLACK));
		System.out.println("white win-count: " + analyzer.getWinCount(Color.WHITE));
		System.out.println("black win-count: " + analyzer.getWinCount(Color.BLACK));


	}

}
