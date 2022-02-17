package ubc.cosc322;

import ubc.cosc322.engine.analysis.DebugOutput;
import ubc.cosc322.engine.analysis.HeadToHeadAnalyzer;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.players.FastRandomPlayer;
import ubc.cosc322.engine.players.Player;

public class LocalTest {
	
	public static void main(String[] args) {

		final int ITERATION_COUNT = 1000;
		final DebugOutput DEBUG_OUTPUT = DebugOutput.OUTCOME;

		State state = new State(); // standard 10x10 4 queen board

		Player white = new FastRandomPlayer(4);
		Player black = new FastRandomPlayer(4);

		HeadToHeadAnalyzer analyzer = new HeadToHeadAnalyzer(state, white, black);
		analyzer.setDebugOutput(DEBUG_OUTPUT);
		System.out.println("iteration count: " + ITERATION_COUNT);

		long startTime = System.currentTimeMillis();
		analyzer.play(ITERATION_COUNT);
		long stopTime = System.currentTimeMillis();
		long elapsed = stopTime - startTime;
		if (DEBUG_OUTPUT != DebugOutput.NONE) {
			System.out.println("execution time: " + elapsed + " ms (innacurate)");
		} else {
			System.out.println("execution time: " + elapsed + " ms");
		}

		System.out.println("white win-rate: " + analyzer.getWinRate(Color.WHITE));
		System.out.println("black win-rate: " + analyzer.getWinRate(Color.BLACK));
		System.out.println("white win-count: " + analyzer.getWinCount(Color.WHITE));
		System.out.println("black win-count: " + analyzer.getWinCount(Color.BLACK));

	}

}
