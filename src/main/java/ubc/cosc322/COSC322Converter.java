package ubc.cosc322;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/** provides utility functions to convert between the server/client formats and our engine's formats */
public class COSC322Converter {
	
	/** decodes the servers board state format and converts it to our format */
	@SuppressWarnings("unchecked")
	public static State decodeBoardState(Map<String, Object> msgDetails) {
		ArrayList<Integer> board = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
		// the game messages use 1-based indexing 11x11 board
		// instead of 0-based 10x10 board for reason
		if (board.size() != 121) {
			throw new IllegalArgumentException("board size is not 10x10");
		}
		ArrayList<Position> whiteQueens = new ArrayList<>();
		ArrayList<Position> blackQueens = new ArrayList<>();
		ArrayList<Position> arrows = new ArrayList<>();
		for (int x = 1; x <= 10; x++) {
			for (int y = 1; y <= 10; y++) {
				int piece = board.get(x+(11-y)*11);
				if (piece == 1) {
					blackQueens.add(new Position(x - 1, y - 1));
				} else if (piece == 2) {
					whiteQueens.add(new Position(x - 1, y - 1));
				} else if (piece == 3) {
					arrows.add(new Position(x - 1, y - 1));
				}
			}
		}
		return new State(10, 10, whiteQueens, blackQueens, arrows);
	}

	/** decodes the server's turn format and converts it to our format */
	@SuppressWarnings("unchecked")
	public static Turn decodeTurn(Map<String, Object> msgDetails) {
		ArrayList<Integer> queenSource = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		ArrayList<Integer> queenDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
		ArrayList<Integer> arrowDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
		return new Turn(
			new Position(queenSource.get(1) - 1, 10 - queenSource.get(0)),
			new Position(queenDestination.get(1) - 1, 10 - queenDestination.get(0)),
			new Position(arrowDestination.get(1) - 1, 10 - arrowDestination.get(0))
		);
	}

	/** encode's a turn from the engine into a format that can be sent to the server */
	public static Map<String, Object> encodeTurn(Turn turn) {
		HashMap<String,Object> result = new HashMap<>();
		ArrayList<Integer> queenSource = new ArrayList<>();
		queenSource.add(10 - turn.queenSource.y);
		queenSource.add(turn.queenSource.x + 1);
		result.put(AmazonsGameMessage.QUEEN_POS_CURR, queenSource);
		ArrayList<Integer> queenDestination = new ArrayList<>();
		queenDestination.add(10 - turn.queenDestination.y);
		queenDestination.add(turn.queenDestination.x + 1);
		result.put(AmazonsGameMessage.QUEEN_POS_NEXT, queenDestination);
		ArrayList<Integer> arrowDestination = new ArrayList<>();
		arrowDestination.add(10 - turn.arrowDestination.y);
		arrowDestination.add(turn.arrowDestination.x + 1);
		result.put(AmazonsGameMessage.ARROW_POS, arrowDestination);
		return result;
	}

	/** decodes which players are playing which colors */
	public static Map<Color,String> decodePlayerUsernames(Map<String, Object> msgDetails) {
		String whitePlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_WHITE);
		String blackPlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_BLACK);
		EnumMap<Color,String> result = new EnumMap<>(Color.class);
		result.put(Color.WHITE, whitePlayer);
		result.put(Color.BLACK, blackPlayer);
		return result;
	}

}
