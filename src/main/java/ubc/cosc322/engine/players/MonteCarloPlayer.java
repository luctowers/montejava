package ubc.cosc322.engine.players;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.MoveType;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.heuristics.Heuristic;
import ubc.cosc322.engine.util.IntList;

/** A player that uses a multi-threaded Monte Carlo Tree Search. */
public class MonteCarloPlayer extends Player implements AutoCloseable {

	private volatile boolean running;
	private Supplier<Heuristic> heuristicSupplier;
	private int thinkingMillis;
	private double explorationFactor;
	private Thread[] workerThreads;
	private volatile Node root;
	private int threadCount;
	private volatile RootStats rootStats;
	private Stats publicStats;

	public MonteCarloPlayer(Supplier<Heuristic> heuristicSupplier, int threadCount, int thinkingMillis, double explorationFactor) {
		this.running = false;
		this.thinkingMillis = thinkingMillis;
		this.explorationFactor = explorationFactor;
		this.threadCount = threadCount;
		this.heuristicSupplier = heuristicSupplier;
	}

	@Override
	public void useState(State state) {
		try {
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.useState(state);
		// state.computeChambers();
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
		// state.computeChambers();
		if (!rebase(move)) {
			this.root = new Node(state);
			this.rootStats = new RootStats();
		}
	}

	@Override
	public void doTurn(Turn turn) {
		publicStats = new Stats(rootStats);
		super.doTurn(turn);
		// state.computeChambers();
		if (!rebase(turn.queenPick) || !rebase(turn.queenMove) || !rebase(turn.arrowShot)) {
			this.root = new Node(state);
			this.rootStats = new RootStats();
		}
	}

	private boolean rebase(int move) {
		int index = root.moves.search(move);
		if (index == -1) {
			return false;
		}
		Node child = root.children[index];
		if (child == null) {
			return false;
		}
		RootStats newStats = new RootStats();
		newStats.maxDepth = rootStats.maxDepth - 1;
		newStats.evaluations.set((int) root.childrenEvaluations[index]);
		double rewards;
		if (root.color != child.color) {
			rewards = root.childrenEvaluations[index] - root.childrenRewards[index];
		} else {
			rewards = root.childrenRewards[index];
		}
		newStats.rewards.set((int) rewards);
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
				double reward = node.childrenRewards[i] / node.childrenEvaluations[i];
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

		private Heuristic heuristic;
		private IntList indexTrace;
		private ArrayList<Node> nodeTrace;

		public Worker() {
			this.heuristic = heuristicSupplier.get();
			this.indexTrace = new IntList(state.dimensions.boardSize * MoveType.COUNT);
			this.nodeTrace = new ArrayList<>(state.dimensions.boardSize * MoveType.COUNT);
		}

		private void search(State searchState) {

			// get a reference to the stats object now
			// if we got it later, it might have changed
			// and stats would be added to the wrong object!
			RootStats cachedRootStats = rootStats;

			// selection
			nodeTrace.clear();
			indexTrace.clear();
			double parentEvaluations = cachedRootStats.evaluations.doubleValue();
			Node previousNode = null;
			Node selectedNode = root;
			int selectedChild = -1;
			do {
				// this if statement is needed to prevent inconcsistent views when multithreading
				if (selectedNode.moveCount != searchState.getMoveCount()) {
					// System.out.println("MISMATCH PREVENTED");
					return;
				}
				selectedChild = select(parentEvaluations, selectedNode);
				if (selectedChild == -1) {
					break;
				}
				indexTrace.push(selectedChild);
				nodeTrace.add(selectedNode);
				searchState.doMove(selectedNode.moves.get(selectedChild));
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
				winner = searchState.getColorToMove().other();

				// DEBUG ASSERTION
				// check for bad terminal states
				// IntList moves = new IntList(100);
				// searchState.generateMoves(moves);
				// if (moves.size() != 0) {
				// 	System.out.println("BAD TERMINAL " + moves + " " + selectedNode.moves + " " + selectedNode.moveCount + " " + previousNode.moveCount);
				// 	System.out.println(searchState);
				// }

			} else {

				// expansion
				Node expandedNode = new Node(searchState);
				// simulation
				winner = evaluate(searchState);
				// attach expanded node
				previousNode.children[selectedChild] = expandedNode;

			}

			backpropogate(cachedRootStats, winner);

		}

		private int select(double parentEvaluations, Node node) {
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
				// TODO: fix this so it isn't needed
				if (Double.isNaN(score)) {
					return i;
				}
				if (score >= maxScore) {
					maxScore = score;
					maxScoreIndex = i;
				}
			}
			return maxScoreIndex;
		}

		private Color evaluate(State state) {
			int eval = heuristic.evaluate(state);
			// System.out.println(eval);
			if (eval > 0) {
				return Color.WHITE;
			} else if (eval < 0) {
				return Color.BLACK;
			} else {
				return null;
			}
		}

		private void backpropogate(RootStats cachedRootStats, Color winner) {
			cachedRootStats.evaluations.incrementAndGet();
			if (state.getColorToMove() == winner) {
				cachedRootStats.rewards.incrementAndGet();
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

		public int moveCount;
		public Color color;
		public IntList moves;
		public Node[] children;
		public double[] childrenEvaluations;
		public double[] childrenRewards;
		public int nextToExpand;

		public Node(State state) {
			this.moveCount = state.getMoveCount();
			this.color = state.getColorToMove();
			this.moves = new IntList(state.getMaxMoves());
			state.generateMoves(moves);
			this.children = new Node[moves.size()];
			this.childrenEvaluations = new double[moves.size()];
			this.childrenRewards = new double[moves.size()];
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

		public AtomicInteger rewards;
		public AtomicInteger evaluations;
		public int maxDepth;

		public RootStats() {
			rewards = new AtomicInteger();
			evaluations = new AtomicInteger();
		}

	}

	public class Stats {

		public final double whiteWinRatio;
		public final int evaluations;
		public final int maxDepth;

		public Stats(RootStats stats) {
			this.evaluations = stats.evaluations.intValue();
			this.maxDepth = stats.maxDepth;
			double rewardRatio = stats.rewards.doubleValue() / stats.evaluations.doubleValue();
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
