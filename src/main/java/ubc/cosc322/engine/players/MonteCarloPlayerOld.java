// package ubc.cosc322.engine.players;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.function.Supplier;

// import ubc.cosc322.engine.core.Color;
// import ubc.cosc322.engine.core.Move;
// import ubc.cosc322.engine.core.State;
// import ubc.cosc322.engine.core.Turn;
// import ubc.cosc322.engine.generators.MoveGenerator;

// /** A player that uses a multi-threaded Monte Carlo Tree Search. */
// public class MonteCarloPlayer extends Player implements AutoCloseable {

// 	private volatile boolean running;
// 	private Supplier<Player> rolloutPlayerSupplier;
// 	private MoveGenerator moveGenerator;
// 	private int millisecondsPerMove;
// 	private double explorationFactor;
// 	private Thread[] workerThreads;
// 	private volatile Node root;
// 	private int threadCount;
// 	private int maxDepth;

// 	public MonteCarloPlayer(MoveGenerator moveGenerator, Supplier<Player> rolloutPlayerSupplier, int threadCount, int millisecondsPerMove, double explorationFactor) {
// 		this.running = false;
// 		this.moveGenerator = moveGenerator;
// 		this.millisecondsPerMove = millisecondsPerMove;
// 		this.explorationFactor = explorationFactor;
// 		this.threadCount = threadCount;
// 		this.rolloutPlayerSupplier = rolloutPlayerSupplier;
// 	}

// 	@Override
// 	public void useState(State state) {
// 		try {
// 			close();
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 		super.useState(state);
// 		this.root = new Node(state);
// 		this.running = true;
// 		this.maxDepth = 0;
// 		this.workerThreads = new Thread[threadCount];
// 		for (int i = 0; i < threadCount; i++) {
// 			this.workerThreads[i] = new Thread(new Worker());
// 			this.workerThreads[i].start();
// 		}
// 	}

// 	@Override
// 	public void doMove(Move move) {
// 		this.maxDepth = 0;
// 		// rebase the search tree on the move if possible
// 		boolean childFound = false;
// 		for (int i = 0; i < root.children.length; i++) {
// 			if (root.moves.get(i).equals(move)) {
// 				Node child = root.children[i];
// 				if  (child != null) {
// 					root = root.children[i];
// 					childFound = true;
// 				}
// 				break;
// 			}
// 		}
// 		super.doMove(move);
// 		// didn't find the move to rebase, need to start from scratch
// 		if (!childFound) {
// 			this.root = new Node(state);
// 		}
// 	}

// 	public Stats getStats() {
// 		Stats stats = new Stats();
// 		stats.whiteWinRatio = computeWinRatio(Color.WHITE);
// 		stats.simulations = root.simulations;
// 		stats.maxDepth = maxDepth;
// 		return stats;
// 	}

// 	@Override
// 	public Move suggestMove() {
// 		try {
// 			Thread.sleep(millisecondsPerMove);
// 		} catch (InterruptedException e) {
// 			e.printStackTrace();
// 		}
// 		List<Move> moves = suggestMultiple(1); 
// 		if (moves.size() != 1) {
// 			return null;
// 		}
// 		return moves.get(0);
// 	}

// 	@Override
// 	public Turn suggestTurn() {
// 		try {
// 			Thread.sleep(2*millisecondsPerMove);
// 		} catch (InterruptedException e) {
// 			e.printStackTrace();
// 		}
// 		List<Move> moves = suggestMultiple(2); 
// 		if (moves.size() != 2) {
// 			return null;
// 		}
// 		return new Turn(moves.get(0), moves.get(1));
// 	}

// 	private List<Move> suggestMultiple(int n) {
// 		List<Move> moves = new ArrayList<>();
// 		Node node = root;
// 		for (int d = 0; d < n && node != null; d++) {
// 			double maxWinRatio = 0.0;
// 			Move selectedMove = null;
// 			Node selectedChild = null;
// 			for (int i = 0; i < node.children.length; i++) {
// 				Node child = node.children[i];
// 				if (child == null) {
// 					if (selectedMove == null) {
// 						selectedMove = node.moves.get(i);
// 						selectedChild = child;
// 					}
// 					break;
// 				}
// 				double winRatio = computeWinRatio(child, state.getColorToMove());
// 				if (winRatio >= maxWinRatio) {
// 					maxWinRatio = winRatio;
// 					selectedMove = node.moves.get(i);
// 					selectedChild = child;
// 				}
// 			}
// 			if (selectedMove != null) {
// 				moves.add(selectedMove);
// 			}
// 			node = selectedChild;
// 		}
// 		return moves;
// 	}


// 	private class Worker implements Runnable {

// 		private Player rolloutPlayer;

// 		public Worker() {
// 			this.rolloutPlayer = rolloutPlayerSupplier.get();
// 		}

// 		@Override
// 		public void run() {
// 			while (running) {
// 				if (root != null) {
// 					try {
// 						search(root, state.clone(), 1);
// 					} catch (Exception e) {
// 						e.printStackTrace();
// 					}
// 				}
// 			}
// 		}

// 		private int search(Node node, State state, int depth) {

// 			if (depth > maxDepth) {
// 				maxDepth = depth;
// 			}

// 			double maxScore = 0.0;
// 			Move selecteMove = null;
// 			Node selectedChild = null;
// 			for (int i = 0; i < node.children.length; i++) {
// 				if (node.children[i] != null) {

// 					double score = ucb1(node, node.children[i], state.getColorToMove());
// 					if (score > maxScore) {
// 						maxScore = score;
// 						selecteMove = node.moves.get(i);
// 						selectedChild = node.children[i];
// 					}

// 				} else {

// 					// perform simulation
// 					state.doMove(node.moves.get(i));
// 					Node leaf = new Node(state);
// 					int simulationResult = simulate(state);
// 					// record simulation in leaf and attach leaf to parent
// 					leaf.whiteWins = simulationResult;
// 					leaf.simulations = 1;
// 					node.children[i] = leaf;
// 					// record simulation in parent
// 					node.simulations += 1;
// 					node.whiteWins += simulationResult;
// 					// back propogation
// 					return leaf.whiteWins;

// 				}
// 			}

// 			if (selectedChild == null || selecteMove == null) {

// 				node.simulations += 1;
// 				if (state.getColorToMove() != Color.WHITE) {
// 					node.whiteWins += 1;
// 					return 1;
// 				} else {
// 					return 0;
// 				}

// 			} else {

// 				state.doMove(selecteMove);
// 				int result = search(selectedChild, state, depth+1);
// 				node.simulations += 1;
// 				node.whiteWins += result;
// 				return result;

// 			}

// 		}

// 		private int simulate(State state) {
// 			rolloutPlayer.useState(state);
// 			Move move = rolloutPlayer.suggestMove();
// 			while (move != null) {
// 				rolloutPlayer.doMove(move);
// 				move = rolloutPlayer.suggestMove();
// 			}
// 			if (state.getColorToMove() == Color.WHITE) {
// 				return 0;
// 			} else {
// 				return 1;
// 			}
// 		}
		
// 	}

// 	private double ucb1(Node parent, Node child, Color color) {
// 		double winRatio = computeWinRatio(child, color);
// 		return winRatio + explorationFactor * Math.sqrt( Math.log(parent.simulations) / child.simulations );
// 	}

// 	private static double computeWinRatio(Node node, Color color) {
// 		double ratio = (double) node.whiteWins / node.simulations;
// 		if (color != Color.WHITE) {
// 			ratio = 1.0 - ratio;
// 		}
// 		return Math.min(Math.max(0.0, ratio), 1.0);
// 	}

// 	private double computeWinRatio(Color color) {
// 		return computeWinRatio(root, color);
// 	}

// 	private class Node {

// 		public List<Move> moves;
// 		public Node[] children;
// 		public int simulations;
// 		public int whiteWins;

// 		public Node(State state) {
// 			moves = moveGenerator.generateMoves(state);
// 			children = new Node[moves.size()];
// 		}

// 	}

// 	public class Stats {

// 		public double whiteWinRatio;
// 		public int simulations;
// 		public int maxDepth;

// 	}

// 	@Override
// 	public void close() throws Exception {
// 		if (workerThreads != null) {
// 			running = false;
// 			for (Thread thread : workerThreads) {
// 				thread.join();
// 			}
// 			workerThreads = null;
// 		}
// 	}
	
// }
