package ubc.cosc322;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.generators.ContestedMoveGenerator;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.heuristics.EndgameHeuristic;
import ubc.cosc322.engine.heuristics.HybridRolloutHeuristic;
import ubc.cosc322.engine.heuristics.RolloutHeuristic;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.RandomMovePlayer;
import ubc.cosc322.engine.util.HeadToHeadAnalyzer;
import ubc.cosc322.engine.util.IntList;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		Board initialState = new Board(); // standard 10x10 4 queen board

		// Player white = new RandomMovePlayer(new ContestedMoveGenerator());
		// Player black = new RandomMovePlayer(new LegalMoveGenerator());
		MonteCarloPlayer white = new MonteCarloPlayer(() -> new RolloutHeuristic(new RandomMovePlayer(new LegalMoveGenerator())), () -> new LegalMoveGenerator(), 4, 10000, 0.3);
		MonteCarloPlayer black = new MonteCarloPlayer(() -> new HybridRolloutHeuristic(new RandomMovePlayer(new ContestedMoveGenerator())), () -> new ContestedMoveGenerator(), 4, 10000, 0.3);

		EndgameHeuristic endgame = new EndgameHeuristic();

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialState, white, black)) {

			analyzer.onTurn(board -> {
				System.out.println(board);
				System.out.println(white.getStats().whiteWinRatio);
				System.out.println(white.getStats().evaluations);
				System.out.println(black.getStats().whiteWinRatio);
				System.out.println(black.getStats().evaluations);
				board.computeChambers();
				System.out.print("WHITE:");
				IntList whiteChambers = board.getWhiteChambers();
				for (int i = 0; i < whiteChambers.size(); i++) {
					int chamber = whiteChambers.get(i);
					int chamberSize = board.getChamberSize(chamber);
					System.out.print(" " + chamber + "@" + chamberSize);
				}
				System.out.println();
				System.out.print("BLACK:");
				IntList blackChambers = board.getBlackChambers();
				for (int i = 0; i < blackChambers.size(); i++) {
					int chamber = blackChambers.get(i);
					int chamberSize = board.getChamberSize(chamber);
					System.out.print(" " + chamber + "@" + chamberSize);
				}
				System.out.println();
				System.out.println("territory: " + endgame.evaluate(board));
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
