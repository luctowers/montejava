package ubc.cosc322.engine.analysis;

import java.util.EnumMap;
import java.util.Map;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.players.Player;

public class HeadToHeadAnalyzer {

	State state;
	Player whitePlayer, blackPlayer;
	private Map<Color,Integer> winCounts;
	
	public HeadToHeadAnalyzer(State state, Player whitePlayer, Player blackPlayer) {
		this.state = state;
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
		this.winCounts = new EnumMap<>(Color.class);
		winCounts.put(Color.WHITE, Integer.valueOf(0));
		winCounts.put(Color.BLACK, Integer.valueOf(0));
	}

	public void play(int n) {
		for (int i = 0; i < n; i++) {
			State playState = state.clone();
			whitePlayer.setState(state.clone());
			blackPlayer.setState(state.clone());
			for (Color c = Color.WHITE;; c = c.other()) {
				Player playerToMove, playerToWait;
				if (state.getColorToMove() == Color.WHITE) {
					playerToMove = whitePlayer;
					playerToWait = blackPlayer;
				} else {
					playerToMove = blackPlayer;
					playerToWait = whitePlayer;
				}
				Move move = playerToMove.play();
				if (move == null) {
					break;
				}
				playerToWait.play(move);
				playState.doMove(move);
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

}
