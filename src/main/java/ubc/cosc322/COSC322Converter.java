// package ubc.cosc322;

// import java.util.ArrayList;
// import java.util.EnumMap;
// import java.util.HashMap;
// import java.util.Map;

// import ubc.cosc322.engine.core.Color;
// import ubc.cosc322.engine.core.Dimensions;
// import ubc.cosc322.engine.core.State;
// import ubc.cosc322.engine.core.Turn;
// import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

// /** provides utility functions to convert between the server/client formats and our engine's formats */
// public class COSC322Converter {
	
// 	/** decodes the servers board state format and converts it to our format */
// 	@SuppressWarnings("unchecked")
// 	public static State decodeBoardState(Map<String, Object> msgDetails) {
// 		ArrayList<Integer> board = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
// 		// the game messages use 1-based indexing 11x11 board
// 		// instead of 0-based 10x10 board for reason
// 		if (board.size() != 121) {
// 			throw new IllegalArgumentException("board size is not 10x10");
// 		}
// 		State state = new State(new Dimensions(10, 10));
// 		for (int x = 1; x <= 10; x++) {
// 			for (int y = 1; y <= 10; y++) {
// 				int piece = board.get(x+y*11);
// 				int position = state.dimensions.position(x-1, y-1);
// 				if (piece == 1) {
// 					state.placeQueen(Color.BLACK, position);
// 				} else if (piece == 2) {
// 					state.placeQueen(Color.WHITE, position);
// 				} else if (piece == 3) {
// 					state.placeArrow(position);
// 				}
// 			}
// 		}
// 		return state;
// 	}

// 	/** decodes the server's turn format and converts it to our format */
// 	@SuppressWarnings("unchecked")
// 	public static Turn decodeTurn(Map<String, Object> msgDetails, Dimensions dimensions) {
// 		ArrayList<Integer> queenSource = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
// 		ArrayList<Integer> queenDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
// 		ArrayList<Integer> arrowDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
// 		return new Turn(
// 			dimensions.position(queenSource.get(1)-1, queenSource.get(0)-1),
// 			dimensions.position(queenDestination.get(1)-1, queenDestination.get(0)-1),
// 			dimensions.position(arrowDestination.get(1)-1, arrowDestination.get(0)-1)
// 		);
// 	}

// 	/** encode's a turn from the engine into a format that can be sent to the server */
// 	public static Map<String, Object> encodeTurn(Turn turn, Dimensions dimensions) {
// 		HashMap<String,Object> result = new HashMap<>();
// 		ArrayList<Integer> queenSource = encodePosition(turn.queenSource, dimensions);
// 		ArrayList<Integer> queenDestination = encodePosition(turn.queenDestination, dimensions);
// 		ArrayList<Integer> arrowDestination = encodePosition(turn.arrowDestination, dimensions);
// 		result.put(AmazonsGameMessage.QUEEN_POS_CURR, queenSource);
// 		result.put(AmazonsGameMessage.ARROW_POS, arrowDestination);
// 		result.put(AmazonsGameMessage.QUEEN_POS_NEXT, queenDestination);
// 		return result;
// 	}

// 	public static ArrayList<Integer> encodePosition(int position, Dimensions dimensions) {
// 		ArrayList<Integer> result = new ArrayList<>(2);
// 		result.add(dimensions.y(position)+1);
// 		result.add(dimensions.x(position)+1);
// 		return result;
// 	}

// 	/** decodes which players are playing which colors */
// 	public static Map<Color,String> decodePlayerUsernames(Map<String, Object> msgDetails) {
// 		String whitePlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_WHITE);
// 		String blackPlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_BLACK);
// 		EnumMap<Color,String> result = new EnumMap<>(Color.class);
// 		result.put(Color.WHITE, whitePlayer);
// 		result.put(Color.BLACK, blackPlayer);
// 		return result;
// 	}

// }
