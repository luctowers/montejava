package ubc.cosc322;

import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.heuristics.RolloutHeuristic;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.RandomMovePlayer;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		State initialState = new State(); // standard 10x10 4 queen board

		MonteCarloPlayer white = new MonteCarloPlayer(() -> new RolloutHeuristic(new RandomMovePlayer()), 4, 1000, 0.3);
		MonteCarloPlayer black = new MonteCarloPlayer(() -> new RolloutHeuristic(new RandomMovePlayer()), 4, 1000, 0.5);
		// MonteCarloPlayer black = new MonteCarloPlayer(() -> new PartialRolloutHeuristic(new RandomMovePlayer(), new MobilityHeuristic(), 90), 4, 1000, 0.3);

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialState, white, black)) {

			analyzer.onTurn(state -> {
				System.out.println(state);
				System.out.println(white.getStats().whiteWinRatio);
				System.out.println(black.getStats().whiteWinRatio);
			});
			System.out.println("iteration count: " + ITERATION_COUNT);

			long startTime = System.currentTimeMillis();
			analyzer.play(ITERATION_COUNT);
			long stopTime = System.currentTimeMillis();
			long elapsed = stopTime - startTime;
			System.out.println("execution time: " + elapsed + " ms");

			System.out.println("white win-rate: " + analyzer.getWinRate(Color.WHITE));
			System.out.println("black win-rate: " + analyzer.getWinRate(Color.BLACK));
			System.out.println("white win-count: " + analyzer.getWinCount(Color.WHITE));
			System.out.println("black win-count: " + analyzer.getWinCount(Color.BLACK));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
