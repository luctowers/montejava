
package ubc.cosc322;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Position;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.Player;
import ubc.cosc322.engine.players.UniformRandomPlayer;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

public class COSC322Test extends GamePlayer {

	private MonteCarloPlayer ai;
	private Color aiColor;

	private GameClient gameClient = null; 
	private BaseGameGUI gamegui = null;
	
	private String userName = null;
	private String passwd = null;
	private String room = null;
 
	/** entry-point */
	public static void main(String[] args) {
		// parse arguments
		String userName;
		String passwd;
		String room = null;
		if (args.length == 0) {
			Random random = new Random();
			userName = "MonteJava#" + random.nextInt(1000);
			passwd = "password";
		} else if (args.length < 2) {
			throw new IllegalArgumentException("not enough arguments");
		} else {
			userName = args[0];
			passwd = args[1];
			if (args.length > 2) {
				room = args[2];
			}
		}

		COSC322Test player = new COSC322Test(userName, passwd, room);
		// if a room is specified assume no gui is needed, terminal only
		if (room == null) {
			player.enableGUI();
		}

		// bootstrap
		if(player.getGameGUI() == null) {
			player.Go();
		}
		else {
			BaseGameGUI.sys_setup();
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					player.Go();
				}
			});
		}
	}

	/** create player that intereacts with the cosc322 game server */
	public COSC322Test(String userName, String passwd, String room) {
		this.userName = userName;
		this.passwd = passwd;
		this.room = room;
		// create the monte carlo ai
		// uniform random rollout player
		// availableProcessors returns the number of cores in the system
		// 14000ms = 14sec per half-turn 28sec < 30sec deadlin
		// 0.3 exploration factor 
		this.ai = new MonteCarloPlayer(
			new LegalMoveGenerator(),
			() -> new UniformRandomPlayer(new LegalMoveGenerator()),
			Runtime.getRuntime().availableProcessors(), 14000, 0.3
		);
	}

	private void enableGUI() {
		this.gamegui = new BaseGameGUI(this);
	}
 
	/** called when connected to the server */
	@Override
	public void onLogin() {
		System.out.println("Congratualations!!! "
				+ "I am called because the server indicated that the login is successfully");
		System.out.println("The next step is to find a room and join it: "
				+ "the gameClient instance created in my constructor knows how!");
		userName = gameClient.getUserName();
		if (gamegui != null) {
			gamegui.setRoomInformation(gameClient.getRoomList());
		}
		if (room != null) {
			gameClient.joinRoom(room);
		}
	}

	/** handle all messages received from the game server */
	@Override
	@SuppressWarnings("unchecked")
	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
		ArrayList<Integer> board;
		ArrayList<Integer> queenSource;
		ArrayList<Integer> queenDestination;
		ArrayList<Integer> arrowDestination;
		String whitePlayer;
		String blackPlayer;
		switch (messageType) {
			case GameMessage.GAME_STATE_BOARD:
				// received board state, feed it the gui and ai
				board = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
				if (gamegui != null) {
					gamegui.setGameState(board);
				}
				State state = decodeBoardState(board);
				System.out.println(state);
				ai.useState(state);
				break;
			case GameMessage.GAME_ACTION_START:
				// game has started, check who's turn it is and make a move if it our turn
				board = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);
				whitePlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_WHITE);
				blackPlayer = (String) msgDetails.get(AmazonsGameMessage.PLAYER_BLACK);
				if (whitePlayer.equals(blackPlayer)) {
					throw new IllegalArgumentException("players can't have the same usernames");
				}
				if (userName.equals(whitePlayer)) {
					aiColor = Color.WHITE;
				}
				if (userName.equals(blackPlayer)) {
					aiColor = Color.BLACK;
				}
				System.out.println("GAME START whitePlayer=" + whitePlayer + " blackPlayer=" + blackPlayer +  " aiColor=" + aiColor);
				makeMove();
				break;
			case GameMessage.GAME_ACTION_MOVE:
				// received move, feed it the gui and ai, and make a move ourselves
				if (gamegui != null) {
					gamegui.updateGameState(msgDetails);
				}
				queenSource = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
				queenDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
				arrowDestination = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
				Turn turn = decodeTurn(queenSource, queenDestination, arrowDestination);
				ai.doTurn(turn);
				logState();
				makeMove();
				break;
		}
		return true;
	}

	/** makes a move and sends it to the server, if it is our turn */
	private void makeMove() {
		if (ai.getState().getColorToMove() != aiColor) {
			System.out.println("NOT PLAYING MOVE, NOT OUR TURN");
			return;
		}
		Turn turn = ai.suggestTurn();
		ai.doTurn(turn);
		logState();
		ArrayList<Integer> queenSource = new ArrayList<>();
		queenSource.add(10 - turn.queenSource.y);
		queenSource.add(turn.queenSource.x + 1);
		ArrayList<Integer> queenDestination = new ArrayList<>();
		queenDestination.add(10 - turn.queenDestination.y);
		queenDestination.add(turn.queenDestination.x + 1);
		ArrayList<Integer> arrowDestination = new ArrayList<>();
		arrowDestination.add(10 - turn.arrowDestination.y);
		arrowDestination.add(turn.arrowDestination.x + 1);
		gameClient.sendMoveMessage(queenSource, queenDestination, arrowDestination);
		if (gamegui != null) {
			gamegui.updateGameState(queenSource, queenDestination, arrowDestination);
		}
	}

	/** logging to provide game history to the terminal */
	private void logState() {
		State state = ai.getState();
		System.out.println(state);
		if (aiColor == null) {
			System.out.println("WE ARE SPECTATING");
		} else if (state.getColorToMove() == aiColor) {
			System.out.println("OUR turn, we are " + aiColor);
		} else {
			System.out.println("OPPONENT'S turn, we are " + aiColor);
		}
	}

	/** decodes the servers board state format and converts it to our format */
	private State decodeBoardState(ArrayList<Integer> board) {
		// the game messages use 1-based indexing 11x11 board
		// instead of 0-based 10x10 board for reason
		if (board.size() != 121) {
			throw new IllegalArgumentException("board size is not 10x10");
		}
		ArrayList<Position> whiteQueens = new ArrayList<>();
		ArrayList<Position> blackQueens = new ArrayList<>();
		for (int x = 1; x <= 10; x++) {
			for (int y = 1; y <= 10; y++) {
				int piece = board.get(x+y*11);
				if (piece == 1) {
					whiteQueens.add(new Position(x - 1, y - 1));
				} else if (piece == 2) {
					blackQueens.add(new Position(x - 1, y - 1));
				}
			}
		}
		return new State(10, 10, whiteQueens, blackQueens);
	}

	/** decodes the server's turn format and converts it to our format */
	private Turn decodeTurn(ArrayList<Integer> queenSource, ArrayList<Integer> queenDestination, ArrayList<Integer> arrowDestination) {
		return new Turn(
			new Position(queenSource.get(1) - 1, 10 - queenSource.get(0)),
			new Position(queenDestination.get(1) - 1, 10 - queenDestination.get(0)),
			new Position(arrowDestination.get(1) - 1, 10 - arrowDestination.get(0))
		);
	}
	
	@Override
	public String userName() {
		return userName;
	}

	@Override
	public GameClient getGameClient() {
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		return  this.gamegui;
	}

	@Override
	public void connect() {
		gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class
