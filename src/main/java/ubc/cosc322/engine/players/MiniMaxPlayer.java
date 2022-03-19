package ubc.cosc322.engine.players;

import ubc.cosc322.engine.core.Color;
import ubc.cosc322.engine.core.Move;
import ubc.cosc322.engine.core.Turn;
import ubc.cosc322.engine.generators.MoveGenerator;
import ubc.cosc322.engine.heuristics.Heuristic;

/** A player that uses a depth limited Minimax search with a heuristic. */
public class MiniMaxPlayer extends Player {

	private MoveGenerator moveGenerator;
	private Heuristic heuristic;
	private int maxDepth;
	
	public MiniMaxPlayer(MoveGenerator moveGenerator, Heuristic heuristic, int maxDepth) {
		this.moveGenerator = moveGenerator;
		this.heuristic = heuristic;
		this.maxDepth = maxDepth;
	}

	@Override
	public Move suggestMove() {
		return miniMax(maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE).move;
	}

	@Override
	public Turn suggestTurn() {
		Node node = miniMax(maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
		if (node.child == null) {
			return null;
		}
		return new Turn(
			node.move,
			node.child.move
		);
	}

	private Node miniMax(int depth, int alpha, int beta) {

		Node node = new Node();

		if (depth == 0) {

			node.value = heuristic.evaluate(state);

		} else if (state.getColorToMove() == Color.WHITE) { // white is the maximizing player

			node.value = Integer.MIN_VALUE;
			for (Move move : moveGenerator.generateMoves(state)) {
				state.doMove(move);
				Node child = miniMax(depth-1, alpha, beta);
				state.undoMove();
				if (child.value >= node.value) {
					node.child = child;
					node.value = child.value;
					node.move = move;
				}
				alpha = Math.max(alpha, node.value);
				if (node.value >= beta) {
					break;
				}
			}

		} else if (state.getColorToMove() == Color.BLACK) { // white is the minimizing player

			node.value = Integer.MAX_VALUE;
			for (Move move : moveGenerator.generateMoves(state)) {
				state.doMove(move);
				Node child = miniMax(depth-1, alpha, beta);
				state.undoMove();
				if (child.value <= node.value) {
					node.child = child;
					node.value = child.value;
					node.move = move;
				}
				beta = Math.min(beta, node.value);
				if (node.value <= alpha) {
					break;
				}
			}

		}

		return node;

	}

	private class Node {
		public Node child;
		public Move move;
		public int value;
	}

}
