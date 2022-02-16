package ubc.cosc322;

import java.util.Arrays;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.players.RandomPlayer;

public class LocalTest {
	
	public static void main(String[] args) {

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

		Player p1 = new RandomPlayer(new LegalMoveGenerator());
		p1.setState(state.clone());
		Player p2 = new RandomPlayer(new LegalMoveGenerator());
		p2.setState(state.clone());

		long startTime = System.nanoTime();
		while (true) {
			System.out.println(state);
			Player playerToMove, playerToWait;
			if (state.getColorToMove() == Color.WHITE) {
				playerToMove = p1;
				playerToWait = p2;
			} else {
				playerToMove = p2;
				playerToWait = p1;
			}
			Move move = playerToMove.play();
			if (move == null) {
				break;
			}
			playerToWait.play(move);
			state.doMove(move);
		}
		long stopTime = System.nanoTime();
		System.out.println(stopTime - startTime);

	}

}
