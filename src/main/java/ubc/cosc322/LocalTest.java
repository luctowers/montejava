package ubc.cosc322;

import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.heuristics.EndgameHeuristic;
import ubc.cosc322.engine.heuristics.HybridRolloutHeuristic;
import ubc.cosc322.engine.heuristics.RolloutHeuristic;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.players.RandomMovePlayer;
import ubc.cosc322.engine.util.IntList;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		Board initialState = new Board(); // standard 10x10 4 queen board

		// Player white = new RandomMovePlayer();
		// Player black = new RandomMovePlayer();
		MonteCarloPlayer white = new MonteCarloPlayer(() -> new RolloutHeuristic(new RandomMovePlayer()), 4, 1000, 0.3);
		MonteCarloPlayer black = new MonteCarloPlayer(() -> new HybridRolloutHeuristic(new RandomMovePlayer()), 4, 1000, 0.3);
		// MonteCarloPlayer black = new MonteCarloPlayer(() -> new PartialRolloutHeuristic(new RandomMovePlayer(), new MobilityHeuristic(), 90), 4, 1000, 0.3);

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialState, white, black)) {

			analyzer.onTurn(board -> {
				Integer.numberOfTrailingZeros(0);
				System.out.println(board);
				System.out.println(white.getStats().whiteWinRatio);
				System.out.println(white.getStats().evaluations);
				System.out.println(black.getStats().whiteWinRatio);
				System.out.println(black.getStats().evaluations);
				for (Color color : Color.values()) {
					System.out.print(color + ":");
					IntList queens = board.getUntrappedQueens(color);
					for (int i = 0; i < queens.size(); i++) {
						int queen = queens.get(i);
						int chamber = board.getPositionChamber(queen);
						int chamberSize = board.getChamberSize(chamber);
						System.out.print(" (" + board.dimensions.x(queen) + "," +  board.dimensions.y(queen) + ")=" + board.getPositionChamber(queen) + "@" + chamberSize);
					}
				}
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
