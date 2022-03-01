package ubc.cosc322;

import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.heuristics.MobilityHeuristic;
import ubc.cosc322.engine.players.MiniMaxPlayer;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.Player;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		State initialState = new State(); // standard 10x10 4 queen board

		Player white = new MonteCarloPlayer(new LegalMoveGenerator(), 7, 250, 0.3);
		Player black = new MiniMaxPlayer(new LegalMoveGenerator(), new MobilityHeuristic(), 4);

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialState, white, black)) {

			analyzer.onMove(state -> System.out.println(state));
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
