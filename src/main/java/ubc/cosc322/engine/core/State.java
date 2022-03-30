package ubc.cosc322.engine.core;

import java.util.EnumMap;

import ubc.cosc322.engine.util.ConsoleColors;
import ubc.cosc322.engine.util.IntList;

/** A complete mutable board state of the game of amazons. */
public class State {

	public static final int MAX_QUEENS_PER_COLOR = 4;

	public final Dimensions dimensions;

	private byte[] board;

	private Color colorToMove;

	private MoveType nextMoveType;

	private int moveCount;

	private EnumMap<Color,IntList> untrappedQueens;

	private int lastQueenSource, lastQueenDestination, lastArrowMove;

	public State(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.board = new byte[dimensions.arrayWidth*dimensions.arrayHeight];
		this.colorToMove = Color.WHITE;
		this.nextMoveType = MoveType.QUEEN;
		this.untrappedQueens = new EnumMap<>(Color.class);
		this.untrappedQueens.put(Color.WHITE, new IntList(MAX_QUEENS_PER_COLOR));
		this.untrappedQueens.put(Color.BLACK, new IntList(MAX_QUEENS_PER_COLOR));
		this.lastQueenSource = -1;
		this.lastQueenDestination = -1;
		this.lastArrowMove = -1;
		this.moveCount = 0;
	}

	public State() {
		this(new Dimensions(10, 10));
		placeQueen(Color.WHITE, dimensions.position(0, 3));
		placeQueen(Color.WHITE, dimensions.position(3, 0));
		placeQueen(Color.WHITE, dimensions.position(6, 0));
		placeQueen(Color.WHITE, dimensions.position(9, 3));
		placeQueen(Color.BLACK, dimensions.position(0, 6));
		placeQueen(Color.BLACK, dimensions.position(3, 9));
		placeQueen(Color.BLACK, dimensions.position(6, 9));
		placeQueen(Color.BLACK, dimensions.position(9, 6));
	}

	private State(State other) {
		this.dimensions = other.dimensions;
		this.board = other.board.clone();
		this.colorToMove = other.colorToMove;
		this.nextMoveType = other.nextMoveType;
		this.lastQueenSource = other.lastQueenSource;
		this.lastQueenDestination = other.lastQueenDestination;
		this.lastArrowMove = other.lastArrowMove;
		this.moveCount = other.moveCount;
		this.untrappedQueens = new EnumMap<>(Color.class);
		this.untrappedQueens.put(Color.WHITE, other.untrappedQueens.get(Color.WHITE).clone());
		this.untrappedQueens.put(Color.BLACK, other.untrappedQueens.get(Color.BLACK).clone());
	}

	public void placeQueen(Color color, int position) {
		placePiece(Piece.queenOfColor(color), position);
		untrappedQueens.get(color).push(position);
	}

	public void placeArrow(int position) {
		placePiece(Piece.ARROW, position);
	}

	private void placePiece(byte piece, int position) {
		if (dimensions.outOfBounds(position)) {
			throw new IllegalArgumentException("invalid board position");
		}
		if (board[position] != Piece.NONE) {
			throw new IllegalArgumentException("position is not empty");
		}
		board[position] = piece;
	}

	public void doMove(int move) {
		doMove(nextMoveType, move);
	}

	public void doMove(MoveType type, int move) {
		moveCount++;
		switch (type) {
			case QUEEN:
				int queenSource = Move.decodeQueenSource(move);
				int queenDestination = Move.decodeQueenDestination(move);
				updateQueenPosition(queenSource, queenDestination);
				lastQueenSource = queenSource;
				lastQueenDestination = queenDestination;
				break;
			case ARROW:
				board[move] = Piece.ARROW;
				lastArrowMove = move;
				colorToMove = colorToMove.other();
				break;
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
		nextMoveType = type.next();
	}

	public void doTurn(Turn turn) {
		doMove(MoveType.QUEEN, turn.queenMove);
		doMove(MoveType.ARROW, turn.arrowMove);
	}

	private void updateQueenPosition(int oldPosition, int newPosition) {
		byte queen = board[oldPosition];
		Color color = Piece.colorOfQueen(queen);
		if (color == null) {
			return;
		}
		IntList positions = untrappedQueens.get(color);
		for (int i = 0; i < positions.size(); i++) {
			if (positions.get(i) == oldPosition) {
				positions.set(i, newPosition);
				break;
			}
		}
		colorToMove = color;
		board[oldPosition] = Piece.NONE;
		board[newPosition] = queen;
	}

	public IntList getUntrappedQueens(Color color) {
		// TODO: make this return immutable
		return untrappedQueens.get(color);
	}

	public int getMoveCount() {
		return moveCount;
	}

	public Color getColorToMove() {
		return colorToMove;
	}

	public MoveType getNextMoveType() {
		return nextMoveType;
	}

	public void traceAll(int source, IntList output) {
		for (int d = 0; d < Direction.COUNT; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int position = source + offset;
			while (!dimensions.outOfBounds(position) && board[position] == Piece.NONE) {
				output.push(position);
				position += offset;
			}
		}
	}

	public boolean surroundedByArrows(int postion) {
		for (int d = 0; d < Direction.COUNT; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int adjacent = postion + offset;
			if (!dimensions.outOfBounds(adjacent)) {
				byte piece = board[adjacent];
				if (piece != Piece.ARROW) {
					return false;
				}
			}
		}
		return true;
	}

	public void generateMoves(IntList output) {
		switch (nextMoveType) {
			case QUEEN:
				generateQueenMoves(output);
				break;
			case ARROW:
				generateArrowShots(output);
				break;
			default:
				throw new IllegalStateException("illegal move type");
		}
	}

	private void generateQueenMoves(IntList output) {
		IntList untrappedQueensToMove = untrappedQueens.get(colorToMove);
		for (int q = untrappedQueensToMove.size() - 1; q >= 0; q--) {
			int outputBase = output.size();
			int queen = untrappedQueensToMove.get(q);
			traceAll(queen, output);
			if (output.size() == outputBase && surroundedByArrows(queen)) {
				untrappedQueensToMove.removeIndex(q);
			}
			for (int i = outputBase; i < output.size(); i++) {
				output.set(i, Move.encodeQueenMove(queen, output.get(i)));
			}
		}
	}

	private void generateArrowShots(IntList output) {
		traceAll(lastQueenDestination, output);
	}

	public byte getPiece(int position) {
		return board[position];
	}

	public int getMaxMoves() {
		switch (nextMoveType) {
			case QUEEN:
				return MAX_QUEENS_PER_COLOR*dimensions.maxTrace;
			case ARROW:
				return dimensions.maxTrace;
			default:
				throw new IllegalStateException("illegal move type");
		}
	}

	public int getMaxMovesAbsolute() {
		return MAX_QUEENS_PER_COLOR*dimensions.maxTrace;
	}

	@Override
	public State clone()
    {
		return new State(this);
    }

	@Override
	public String toString() { 
		StringBuilder builder = new StringBuilder();
		String line = horiztonalLine();
		int highlightedBlank = -1;
		int highlightedQueen = -1;
		int highlightedArrow = -1;
		switch (nextMoveType) {
			case QUEEN:
				highlightedArrow = lastArrowMove;
			case ARROW:
				highlightedBlank = lastQueenSource;
				highlightedQueen = lastQueenDestination;
		}
		builder.append(ConsoleColors.GREEN);
		builder.append(line);
		builder.append('\n');
		for (int y = dimensions.boardHeight-1; y >= 0; y--) {
			builder.append("| ");
			for (int x = 0; x < dimensions.boardWidth; x++) {
				int position = dimensions.position(x, y);
				byte piece = board[position];
				if (piece == Piece.NONE) {
					if (position == highlightedBlank) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('!');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(' ');
					}
				} else if (piece == Piece.ARROW) {
					if (position == highlightedArrow) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('*');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append('*');
					}
				} else if (piece == Piece.WHITE_QUEEN) {
					if (position == highlightedQueen) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('W');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(ConsoleColors.RESET);
						builder.append('W');
						builder.append(ConsoleColors.GREEN);
					}
				} else if (piece == Piece.BLACK_QUEEN) {
					if (position == highlightedQueen) {
						builder.append(ConsoleColors.YELLOW);
						builder.append('B');
						builder.append(ConsoleColors.GREEN);
					} else {
						builder.append(ConsoleColors.RESET);
						builder.append('B');
						builder.append(ConsoleColors.GREEN);
					}
				} else {
					builder.append('?');
				}
				builder.append(" | ");
			}
			builder.append('\n');
			builder.append(line);
			builder.append('\n');
		}
		builder.append(ConsoleColors.RESET);
		builder.append(colorToMove.toString() + " to make " + nextMoveType.toString() + " move");
		return builder.toString();
	}

	/** Helper function for board toString. */
	private String horiztonalLine() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 4*dimensions.boardWidth+1; i++) {
			if (i % 4 == 0) {
				builder.append('+');
			} else {
				builder.append('-');
			}
		}
		return builder.toString();
	}

}
