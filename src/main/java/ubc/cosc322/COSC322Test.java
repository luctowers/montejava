
package ubc.cosc322;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.generators.UnchamberedMoveGenerator;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.RandomMovePlayer;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

public class COSC322Test extends GamePlayer {

	COSC322Timer timer;

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
		this.timer = new COSC322Timer();
		this.userName = userName;
		this.passwd = passwd;
		this.room = room;
		// create the monte carlo ai
		// uniform random rollout player
		// availableProcessors returns the number of cores in the system
		// 28sec < 30sec deadline
		// 0.3 exploration factor 
		this.ai = new MonteCarloPlayer(
			new LegalMoveGenerator(),
			() -> new RandomMovePlayer(new LegalMoveGenerator()),
			Runtime.getRuntime().availableProcessors(), 1000, 0.5
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
	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
		logGameMessage(messageType, msgDetails);
		switch(messageType) {
		case GameMessage.GAME_STATE_BOARD: handleBoardMessage(msgDetails); break;
		case GameMessage.GAME_ACTION_START: handleGameStart(msgDetails); break;
		case GameMessage.GAME_ACTION_MOVE: handleGameMove(msgDetails); break;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void handleBoardMessage(Map<String, Object> msgDetails) {
		if (gamegui != null) {
			gamegui.setGameState((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
		}
		State state = COSC322Converter.decodeBoardState(msgDetails);
		System.out.println(state);
		ai.useState(state);
		// reset aiColor in-case it was set in a previous game
		aiColor = null;
		timer.stop();
	}

	private void handleGameStart(Map<String, Object> msgDetails) {
		Map<Color,String> players = COSC322Converter.decodePlayerUsernames(msgDetails);
		String whiteUsername = players.get(Color.WHITE);
		String blackUsername = players.get(Color.BLACK);
		if (whiteUsername.equals(blackUsername)) {
			throw new IllegalArgumentException("players can't have the same usernames");
		}
		if (userName.equals(whiteUsername)) {
			aiColor = Color.WHITE;
		}
		if (userName.equals(blackUsername)) {
			aiColor = Color.BLACK;
		}
		timer.start(ai.getState().getColorToMove());
		makeMove();
	}

	private void handleGameMove(Map<String, Object> msgDetails) {
		if (gamegui != null) {
			gamegui.updateGameState(msgDetails);
		}
		Turn turn = COSC322Converter.decodeTurn(msgDetails, ai.getState().dimensions);
		timer.stop();
		COSC322Validator.validateAndLog(ai.getState(), turn);
		ai.doTurn(turn);
		timer.start(ai.getState().getColorToMove());
		logState();
		logStats(ai.getStats());
		makeMove();
	}

	/** makes a move and sends it to the server, if it is our turn */
	private void makeMove() {
		if (ai.getState().getColorToMove() != aiColor) {
			System.out.println("NOT PLAYING MOVE, NOT OUR TURN");
		} else {
			System.out.println("OUR ai is currently thinking...");
			Turn turn = ai.suggestAndDoTurn();
			if (turn == null) {
				System.out.println("OUR ai thinks it has LOST");
				return;
			}
			logState();
			logStats(ai.getStats());
			Map<String,Object> msgDetails = COSC322Converter.encodeTurn(turn, ai.getState().dimensions);
			logGameMessage("meta.sent-action.move", msgDetails);
			gameClient.sendMoveMessage(msgDetails);
			timer.stop();
			if (gamegui != null) {
				gamegui.updateGameState(msgDetails);
			}
			timer.start(ai.getState().getColorToMove());
		}
	}

	private void logGameMessage(String messageType, Map<String, Object> msgDetails) {
		StringBuilder builder = new StringBuilder();
		builder.append("messageType=");
		builder.append(messageType);
		for (String key: msgDetails.keySet()) {
			builder.append(" ");
			builder.append(key);
			builder.append("=");
			builder.append(msgDetails.get(key).toString());
		}
		System.out.println(builder);
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

	/** logging who the ai thinks is winning */
	private void logStats(MonteCarloPlayer.Stats stats) {
		double winPercentage = Math.round(1000.0 * stats.whiteWinRatio) / 10.0;
		if (aiColor == Color.WHITE) {
			System.out.println("OUR ai thinks WE have a " + winPercentage + "% chance of WINNING");
		} else if (aiColor == Color.BLACK) {
			System.out.println("OUR ai thinks WE have a " + (100.0-winPercentage) + "% chance of WINNING");
		} else {
			System.out.println("OUR ai thinks WHITE has a " + winPercentage + "% chance of WINNING");
		}
		System.out.println(NumberFormat.getNumberInstance(Locale.CANADA).format(stats.evaluations) + " SIMULATIONS performed with MAX DEPTH of " + stats.maxDepth);
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

}
