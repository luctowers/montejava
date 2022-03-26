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

	private EnumMap<Color,IntList> freeQueens;

	private int lastQueenPick, lastQueenMove, lastArrowShot;

	public State(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.board = new byte[dimensions.arrayWidth*dimensions.arrayHeight];
		this.colorToMove = Color.WHITE;
		this.nextMoveType = MoveType.PICK_QUEEN;
		this.freeQueens = new EnumMap<>(Color.class);
		this.freeQueens.put(Color.WHITE, new IntList(MAX_QUEENS_PER_COLOR));
		this.freeQueens.put(Color.BLACK, new IntList(MAX_QUEENS_PER_COLOR));
		this.lastQueenPick = -1;
		this.lastQueenMove = -1;
		this.lastArrowShot = -1;
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
		this.lastQueenPick = other.lastQueenPick;
		this.lastQueenMove = other.lastQueenMove;
		this.lastArrowShot = other.lastArrowShot;
		this.freeQueens = new EnumMap<>(Color.class);
		this.freeQueens.put(Color.WHITE, other.freeQueens.get(Color.WHITE).clone());
		this.freeQueens.put(Color.BLACK, other.freeQueens.get(Color.BLACK).clone());
	}

	public void placeQueen(Color color, int position) {
		placePiece(Piece.queenOfColor(color), position);
		freeQueens.get(color).push(position);
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
		switch (type) {
			case PICK_QUEEN:
				lastQueenPick = move;
				break;
			case MOVE_QUEEN:
				updateQueenPosition(lastQueenPick, move);
				lastQueenMove = move;
				break;
			case SHOOT_ARROW:
				board[move] = Piece.ARROW;
				lastArrowShot = move;
				colorToMove = colorToMove.other();
				break;
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
		nextMoveType = type.next();
	}

	public void doTurn(Turn turn) {
		doMove(MoveType.PICK_QUEEN, turn.queenPick);
		doMove(MoveType.MOVE_QUEEN, turn.queenMove);
		doMove(MoveType.SHOOT_ARROW, turn.arrowShot);
	}

	public void undoMove(int move) {
		undoMove(nextMoveType.previous(), move);
	}

	public void undoMove(MoveType type, int move) {
		switch (type) {
			case MOVE_QUEEN:
				undoQueenMove(move);
			case SHOOT_ARROW:
				undoArrowShot(move);
			default:
				throw new IllegalArgumentException("only queen and arrow moves can be undone");
		}
	}

	public void undoQueenMove(int move) {
		updateQueenPosition(move, lastQueenPick);
		nextMoveType = MoveType.MOVE_QUEEN.previous();
	}

	public void undoArrowShot(int move) {
		board[move] = Piece.NONE;
		nextMoveType = MoveType.SHOOT_ARROW.previous();
	}

	private void updateQueenPosition(int oldPosition, int newPosition) {
		byte queen = board[oldPosition];
		Color color = Piece.colorOfQueen(queen);
		if (color == null) {
			return;
		}
		IntList positions = freeQueens.get(color);
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

	public IntList getFreeQueens(Color color) {
		// TODO: make this return immutable
		return freeQueens.get(color);
	}

	public Color getColorToMove() {
		return colorToMove;
	}

	public MoveType getNextMoveType() {
		return nextMoveType;
	}

	public int getLastQueenPick() {
		return lastQueenPick;
	}

	public int getLastQueenMove() {
		return lastQueenMove;
	}

	public int getLastArrowShot() {
		return lastArrowShot;
	}

	/** Gets and the positions that can be reached orthogonally or diagonally from a position. */
	public void traceAll(int source, IntList output) {
		for (int d = 0; d < Direction.count; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int position = source + offset;
			while (!dimensions.outOfBounds(position) && board[position] == Piece.NONE) {
				output.push(position);
				position += offset;
			}
		}
	}

	public void generateMoves(IntList output) {
		switch (nextMoveType) {
			case PICK_QUEEN:
				generateQueenPicks(output);
				break;
			case MOVE_QUEEN:
				generateQueenMoves(output);
				break;
			case SHOOT_ARROW:
				generateArrowShots(output);
				break;
			default:
				throw new IllegalStateException("Illegal move type");
		}
	}

	private void generateQueenPicks(IntList output) {
		IntList freeQueensToMove = freeQueens.get(colorToMove);
		for (int q = freeQueensToMove.size() - 1; q >= 0; q--) {
			int queen = freeQueensToMove.get(q);
			boolean surroundedByArrows = true;
			boolean trapped = true;
			for (int d = 0; d < Direction.count; d++) {
				int offset = dimensions.getDirectionOffset(d);
				int adjacent = queen + offset;
				if (!dimensions.outOfBounds(adjacent)) {
					byte piece = board[adjacent];
					if (piece != Piece.ARROW) {
						surroundedByArrows = false;
					}
					if (piece == Piece.NONE) {
						trapped = false;
						break;
					}
				}
			}
			if (surroundedByArrows) {
				freeQueensToMove.remove(q);
			}
			if (!trapped) {
				output.push(queen);
			}
		}
	}

	private void generateQueenMoves(IntList output) {
		traceAll(lastQueenPick, output);
	}

	private void generateArrowShots(IntList output) {
		traceAll(lastQueenMove, output);
	}

	public byte getPiece(int position) {
		return board[position];
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
			case MOVE_QUEEN:
				highlightedQueen = lastQueenPick;
				break;
			case PICK_QUEEN:
				highlightedArrow = lastArrowShot;
			case SHOOT_ARROW:
				highlightedQueen = lastQueenMove;
				highlightedBlank = lastQueenPick;
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
