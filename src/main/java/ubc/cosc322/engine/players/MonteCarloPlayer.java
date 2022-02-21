package ubc.cosc322.engine.players;

import java.util.List;

import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.State;
import ubc.cosc322.engine.generators.MoveGenerator;

public class MonteCarloPlayer extends Player implements AutoCloseable {

	MoveGenerator moveGenerator;
	int millisecondsPerMove;
	Thread[] workerThreads;
	Node root;

	public MonteCarloPlayer(MoveGenerator moveGenerator, int threadCount, int millisecondsPerMove) {
		this.moveGenerator = moveGenerator;
		this.millisecondsPerMove = millisecondsPerMove;
		this.workerThreads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			this.workerThreads[i] = new Thread(new Worker());
		}
	}

	@Override
	public void useState(State state) {
		super.useState(state);
		Node node = new Node();
		node.moves = moveGenerator.generateMoves(state);
		node.children = new Node[node.moves.size()];
		this.root = node;
	}

	@Override
	public Move suggestMove() {
		// TODO Auto-generated method stub
		return null;
	}

	private class Worker implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}

	private class Node {
		public List<Move> moves;
		public Node[] children;
		public int simulations;
		public int whiteWins;
	}

	@Override
	public void close() throws Exception {
		for (Thread thread : workerThreads) {
			thread.join();
		}
	}
	
}
