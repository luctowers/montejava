package ubc.cosc322;

import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.players.RandomMovePlayer;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1;

		State initialState = new State(); // standard 10x10 4 queen board

		Player white = new MonteCarloPlayer(new LegalMoveGenerator(), () -> new RandomMovePlayer(new LegalMoveGenerator()), 8, 1000, 0.3);;
		Player black = new RandomMovePlayer(new LegalMoveGenerator());

		try (HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(initialState, white, black)) {

			analyzer.onTurn(state -> System.out.println(state));
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
