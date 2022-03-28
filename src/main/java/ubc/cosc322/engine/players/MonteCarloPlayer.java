package ubc.cosc322.engine.players;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.MoveGenerator;
import ubc.cosc322.engine.util.IntList;

/** A player that uses a multi-threaded Monte Carlo Tree Search. */
public class MonteCarloPlayer extends Player implements AutoCloseable {

	private volatile boolean running;
	private Supplier<Player> rolloutPlayerSupplier;
	private MoveGenerator moveGenerator;
	private int thinkingMillis;
	private double explorationFactor;
	private Thread[] workerThreads;
	private volatile Node root;
	private int threadCount;
	private volatile RootStats rootStats;
	private Stats publicStats;

	public MonteCarloPlayer(MoveGenerator moveGenerator, Supplier<Player> rolloutPlayerSupplier, int threadCount, int thinkingMillis, double explorationFactor) {
		this.running = false;
		this.moveGenerator = moveGenerator;
		this.thinkingMillis = thinkingMillis;
		this.explorationFactor = explorationFactor;
		this.threadCount = threadCount;
		this.rolloutPlayerSupplier = rolloutPlayerSupplier;
	}

	@Override
	public void useState(State state) {
		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.useState(state);
		this.root = new Node(state);
		this.rootStats = new RootStats();
		this.running = true;
		this.workerThreads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			this.workerThreads[i] = new Thread(new Worker());
			this.workerThreads[i].start();
		}
	}

	@Override
	public void doMove(int move) {
		publicStats = new Stats(rootStats);
		super.doMove(move);
		if (!rebase(move)) {
			this.root = new Node(state);
			this.rootStats = new RootStats();
		}
	}

	@Override
	public void doTurn(Turn turn) {
		publicStats = new Stats(rootStats);
		super.doTurn(turn);
		if (!rebase(turn.queenPick) || !rebase(turn.queenMove) || !rebase(turn.arrowShot)) {
			this.root = new Node(state);
			this.rootStats = new RootStats();
		}
	}

	private boolean rebase(int move) {
		int index = root.moves.search(move);
		Node child = root.children[index];
		if (child == null) {
			return false;
		}
		RootStats newStats = new RootStats();
		newStats.maxDepth = rootStats.maxDepth - 1;
		rootStats.evaluations = root.childrenEvaluations[index];
		if (root.color != child.color) {
			rootStats.rewards = root.childrenEvaluations[index] - root.childrenRewards[index];
		} else {
			rootStats.rewards = root.childrenRewards[index];
		}
		rootStats = newStats;
		root = child;
		return true;
	}

	@Override
	public void suggestAndDoMoves(int maxMoves, IntList output) {
		try {
			Thread.sleep(thinkingMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// TODO: find a better way to handle these stats instead of caching here
		Stats stats = new Stats(rootStats);
		Node node = root;
		while (maxMoves-- > 0) {
			double maxReward = 0.0;
			int maxIndex = -1;
			for (int i = 0; node != null && i < node.size(); i++) {
				double reward = (double) node.childrenRewards[i] / node.childrenEvaluations[i];
				if (reward >= maxReward) {
					maxReward = reward;
					maxIndex = i;
				}
			}
			if (maxIndex == -1) {
				break;
			}
			int move = node.moves.get(maxIndex);
			doMove(move);
			output.push(move);
			node = node.children[maxIndex];
		}
		publicStats = stats;
	}

	public Stats getStats() {
		return publicStats;
	}

	private class Worker implements Runnable {

		private Player rolloutPlayer;
		IntList indexTrace;
		ArrayList<Node> nodeTrace;
		IntList simulationBuffer;

		public Worker() {
			this.rolloutPlayer = rolloutPlayerSupplier.get();
			this.indexTrace = new IntList(state.dimensions.boardSize * MoveType.COUNT);
			this.nodeTrace = new ArrayList<>(state.dimensions.boardSize * MoveType.COUNT);
			this.simulationBuffer = new IntList(MoveType.COUNT);
		}

		private void search(State state) {

			// get a reference to the stats object now
			// if we got it later, it might have changed
			// and stats would be added to the wrong object!
			RootStats cachedRootStats = rootStats;

			// selection
			nodeTrace.clear();
			indexTrace.clear();
			int parentEvaluations = cachedRootStats.evaluations;
			Node previousNode = null;
			Node selectedNode = root;
			int selectedChild = -1;
			do {
				selectedChild = select(parentEvaluations, selectedNode);
				if (selectedChild == -1) {
					break;
				}
				indexTrace.push(selectedChild);
				nodeTrace.add(selectedNode);
				state.doMove(selectedNode.moves.get(selectedChild));
				parentEvaluations = selectedNode.childrenEvaluations[selectedChild];
				previousNode = selectedNode;
				selectedNode = selectedNode.children[selectedChild];
			} while (selectedNode != null);

			int depth = indexTrace.size();
			if (depth > cachedRootStats.maxDepth) {
				cachedRootStats.maxDepth = depth;
			}

			Color winner;
			if (selectedChild == -1) {

				// terminal state
				winner = state.getColorToMove().other();

			} else {

				// expansion
				Node expandedNode = new Node(state);
				// simulation
				winner = simulate(state);
				// attach expanded node
				previousNode.children[selectedChild] = expandedNode;

			}

			backpropogate(cachedRootStats, winner);

		}

		private int select(int parentEvaluations, Node node) {
			if (node.needsExpansion()) {
				return node.nextToExpand++;
			}
			double logParentEvaluations = Math.log(parentEvaluations);
			double maxScore = 0.0;
			int maxScoreIndex = -1;
			for (int i = 0; i < node.size(); i++) {
				if (node.children[i] == null) {
					return i;
				}
				double score = ucb1(node.childrenRewards[i], node.childrenEvaluations[i], logParentEvaluations);
				if (score >= maxScore) {
					maxScore = score;
					maxScoreIndex = i;
				}
			}
			return maxScoreIndex;
		}

		private Color simulate(State state) {
			rolloutPlayer.useState(state);
			do {
				simulationBuffer.clear();
				rolloutPlayer.suggestAndDoMoves(MoveType.COUNT, simulationBuffer);
			} while (simulationBuffer.size() != 0);
			return state.getColorToMove().other();
		}

		private void backpropogate(RootStats cachedRootStats, Color winner) {
			cachedRootStats.evaluations += 1;
			if (state.getColorToMove() == winner) {
				cachedRootStats.rewards += 1;
			}
			for (int i = 0; i < indexTrace.size(); i++) {
				Node node = nodeTrace.get(i);
				int selectedChild = indexTrace.get(i);
				node.childrenEvaluations[selectedChild] += 1;
				if (node.color == winner) {
					node.childrenRewards[selectedChild] += 1;
				}
			}
		}

		private double ucb1(double childRewards, double childEvaluations, double logParentEvaluations) {
			double exploitation = childRewards / childEvaluations;
			double exploration = Math.sqrt(logParentEvaluations / childEvaluations);
			return exploitation + explorationFactor * exploration;
		}

		@Override
		public void run() {
			while (running) {
				if (root != null) {
					try {
						search(state.clone());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

	private class Node {

		public Color color;
		public IntList moves;
		public Node[] children;
		public int[] childrenEvaluations;
		public int[] childrenRewards;
		public int nextToExpand;

		public Node(State state) {
			this.color = state.getColorToMove();
			this.moves = new IntList(state.getMaxMoves());
			moveGenerator.generateMoves(state, moves);
			this.children = new Node[moves.size()];
			this.childrenEvaluations = new int[moves.size()];
			this.childrenRewards = new int[moves.size()];
			this.nextToExpand = 0;
		}

		public boolean needsExpansion() {
			return nextToExpand < children.length && children[nextToExpand] == null;
		}

		public int size() {
			return children.length;
		}

	}

	private class RootStats {

		public double rewards;
		public int evaluations;
		public int maxDepth;

	}

	public class Stats {

		public final double whiteWinRatio;
		public final int evaluations;
		public final int maxDepth;

		public Stats(RootStats stats) {
			System.out.println("REWARDS: " + stats.rewards);
			System.out.println("EVALUATIONS: " + stats.evaluations);
			this.evaluations = stats.evaluations;
			this.maxDepth = stats.maxDepth;
			double rewardRatio = (double) stats.rewards / stats.evaluations;
			rewardRatio = Math.max(0.0, Math.min(rewardRatio, 1.0));
			if (state.getColorToMove() == Color.WHITE) {
				this.whiteWinRatio = rewardRatio;
			} else {
				this.whiteWinRatio = 1.0 - rewardRatio;
			}
		}

	}

	@Override
	public void close() throws Exception {
		if (workerThreads != null) {
			running = false;
			for (Thread thread : workerThreads) {
				thread.join();
			}
			workerThreads = null;
		}
	}
	
}
