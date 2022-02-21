package ubc.cosc322.engine.analysis;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.players.Player;

public class HeadToHeadAnalyzer {

	State state;
	Player whitePlayer, blackPlayer;
	private Map<Color,Integer> winCounts;
	private List<Consumer<State>> moveCallbacks;
	private List<Consumer<State>> endCallbacks;
	
	public HeadToHeadAnalyzer(State state, Player whitePlayer, Player blackPlayer) {
		if (whitePlayer == blackPlayer) {
			throw new IllegalArgumentException("seperate players required");
		}
		this.state = state;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.winCounts = new EnumMap<>(Color.class);
		winCounts.put(Color.WHITE, Integer.valueOf(0));
		winCounts.put(Color.BLACK, Integer.valueOf(0));
		this.moveCallbacks = new ArrayList<>();
		this.endCallbacks = new ArrayList<>();
	}

	public void play(int n) {
		for (int i = 0; i < n; i++) {
			State playState = state.clone();
			whitePlayer.useState(state.clone());
			blackPlayer.useState(state.clone());
			while (true) {
				for (Consumer<State> callback : moveCallbacks) {
					callback.accept(playState);
				}
				Player playerToMove, playerToWait;
				if (state.getColorToMove() == Color.WHITE) {
					playerToMove = whitePlayer;
					playerToWait = blackPlayer;
				} else {
					playerToMove = blackPlayer;
					playerToWait = whitePlayer;
				}
				Move move = playerToMove.suggestMove();
				if (move == null) {
					break;
				}
				playerToMove.doMove(move);
				playerToWait.doMove(move);
				playState.doMove(move);
			}
			for (Consumer<State> callback : endCallbacks) {
				callback.accept(playState);
			}
			Color winner = playState.getColorToMove().other();
			winCounts.put(
				winner,
				Integer.valueOf(
					winCounts.get(winner).intValue() + 1
				)
			);
		}
	}

	public float getWinRate(Color color) {
		return (float) getWinCount(color) / getGamesPlayed();
	}

	public int getWinCount(Color color) {
		return winCounts.get(color).intValue();
	}

	public int getGamesPlayed() {
		return winCounts.get(Color.WHITE).intValue() + winCounts.get(Color.BLACK).intValue();
	}

	public void onMove(Consumer<State> callback) {
		moveCallbacks.add(callback);
	}

	public void onEnd(Consumer<State> callback) {
		endCallbacks.add(callback);
	}

}
