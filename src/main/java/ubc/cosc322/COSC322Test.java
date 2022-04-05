
package ubc.cosc322;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import sfs2x.client.entities.Room;
import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Board;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.ContestedMoveGenerator;
import ubc.cosc322.engine.generators.LegalMoveGenerator;
import ubc.cosc322.engine.heuristics.HybridRolloutHeuristic;
import ubc.cosc322.engine.heuristics.RolloutHeuristic;
import ubc.cosc322.engine.heuristics.SwitchHeuristic;
import ubc.cosc322.engine.players.MonteCarloPlayer;
import ubc.cosc322.engine.players.RandomPlayer;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

/** An adapter between the profs game server/client and our engine. */
public class COSC322Test extends GamePlayer {

	COSC322Timer timer;

	private MonteCarloPlayer ai;
	private Color aiColor;

	private GameClient gameClient = null; 
	private BaseGameGUI gamegui = null;
	
	private String userName = null;
	private String passwd = null;
 
	/** entry-point */
	public static void main(String[] args) {
		// parse arguments
		String userName;
		String passwd;
		boolean nogui = false;
		if (args.length == 0) {
			Random random = new Random();
			userName = "MonteJava#" + random.nextInt(1000);
			passwd = "password";
		} else if (args.length < 2) {
			throw new IllegalArgumentException("not enough arguments");
		} else {
			userName = args[0];
			passwd = args[1];
			if (args.length > 2 && args[2].equals("nogui")) {
				nogui = true;
			}
		}

		COSC322Test player = new COSC322Test(userName, passwd);
		// if a room is specified assume no gui is needed, terminal only
		if (!nogui) {
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
	public COSC322Test(String userName, String passwd) {
		this.timer = new COSC322Timer();
		this.userName = userName;
		this.passwd = passwd;
		// create the monte carlo ai
		// uniform random rollout player
		// switch to hybrid rollout after 50 moves
		// availableProcessors returns the number of cores in the system
		// 28sec < 30sec deadline
		// 0.3 exploration factor
		this.ai = new MonteCarloPlayer(
			() -> new SwitchHeuristic(
				60,
				new RolloutHeuristic(new RandomPlayer(new LegalMoveGenerator())),
				new HybridRolloutHeuristic(new RandomPlayer(new ContestedMoveGenerator()))
			),
			() -> new LegalMoveGenerator(),
			Runtime.getRuntime().availableProcessors()-1, 28000, 1.0
		);
		// warmup the ai
		ai.useBoard(new Board());
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
		} else {
			List<Room> rooms = gameClient.getRoomList();
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				System.out.println("[" + i + "] " + room.getName() + " " + room.getUserCount() + "/2");
			}
			try (Scanner scanner = new Scanner(System.in)) {
				System.out.print("Enter room number: ");
				int roomIndex = scanner.nextInt();
				gameClient.joinRoom(rooms.get(roomIndex).getName());
			}
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
		Board board = COSC322Converter.decodeBoardState(msgDetails);
		System.out.println(board);
		ai.useBoard(board);
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
		timer.start(ai.getBoard().getColorToMove());
		new Thread(new Runnable() {
			public void run() {
				makeMove();
			}
		}).start();
	}

	private void handleGameMove(Map<String, Object> msgDetails) {
		if (gamegui != null) {
			gamegui.updateGameState(msgDetails);
		}
		Turn turn = COSC322Converter.decodeTurn(msgDetails, ai.getBoard().dimensions);
		timer.stop();
		COSC322Validator.validateAndLog(ai.getBoard(), turn);
		ai.doTurn(turn);
		timer.start(ai.getBoard().getColorToMove());
		logState();
		logStats(ai.getStats());
		new Thread(new Runnable() {
			public void run() {
				makeMove();
			}
		}).start();
	}

	/** makes a move and sends it to the server, if it is our turn */
	private void makeMove() {
		if (ai.getBoard().getColorToMove() != aiColor) {
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
			Map<String,Object> msgDetails = COSC322Converter.encodeTurn(turn, ai.getBoard().dimensions);
			logGameMessage("meta.sent-action.move", msgDetails);
			gameClient.sendMoveMessage(msgDetails);
			timer.stop();
			if (gamegui != null) {
				gamegui.updateGameState(msgDetails);
			}
			timer.start(ai.getBoard().getColorToMove());
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
		Board board = ai.getBoard();
		System.out.println(board);
		if (aiColor == null) {
			System.out.println("WE ARE SPECTATING");
		} else if (board.getColorToMove() == aiColor) {
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
