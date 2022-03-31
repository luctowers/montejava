package ubc.cosc322.engine.core;

import ubc.cosc322.engine.util.ConsoleColors;
import ubc.cosc322.engine.util.IntList;

/** A complete mutable board board of the game of amazons. */
public class Board {

	public static final int MAX_QUEENS_PER_COLOR = 4;

	public final Dimensions dimensions;

	private byte[] board;

	private ChamberAnalyzer chamberAnalyzer;

	private Color colorToMove;

	private MoveType nextMoveType;

	private int moveCount;

	private IntList blackQueens;
	private IntList whiteQueens;

	private boolean chambersUpdated;
	private IntList blackChambers;
	private IntList whiteChambers;
	private IntList allChambers;

	private int lastQueenSource, lastQueenDestination, lastArrowMove;

	public Board(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.board = new byte[dimensions.arraySize];
		this.chamberAnalyzer = new ChamberAnalyzer(dimensions);
		this.colorToMove = Color.WHITE;
		this.nextMoveType = MoveType.QUEEN;
		this.whiteQueens = new IntList(MAX_QUEENS_PER_COLOR);
		this.blackQueens = new IntList(MAX_QUEENS_PER_COLOR);
		this.lastQueenSource = -1;
		this.lastQueenDestination = -1;
		this.lastArrowMove = -1;
		this.moveCount = 0;
	}

	public Board() {
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

	private Board(Board other) {
		this.dimensions = other.dimensions;
		this.colorToMove = other.colorToMove;
		this.nextMoveType = other.nextMoveType;
		this.lastQueenSource = other.lastQueenSource;
		this.lastQueenDestination = other.lastQueenDestination;
		this.lastArrowMove = other.lastArrowMove;
		this.moveCount = other.moveCount;
		this.board = other.board.clone();
		this.chamberAnalyzer = other.chamberAnalyzer.clone();
		this.whiteQueens = other.whiteQueens.clone();
		this.blackQueens = other.blackQueens.clone();
	}

	public void placeQueen(Color color, int position) {
		placePiece(Piece.queenOfColor(color), position);
		getQueens(color).push(position);
	}

	public void placeArrow(int position) {
		chamberAnalyzer.placeArrow(position);
		chambersUpdated = false;
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
				placeArrow(move);
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
		IntList positions = getQueens(color);
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

	public IntList getQueens(Color color) {
		switch (color) {
			case WHITE:
				return whiteQueens;
			case BLACK:
				return blackQueens;
			default:
				throw new IllegalArgumentException("invalid color");
		}
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

	private boolean surroundedByArrows(int position) {
		for (int d = 0; d < Direction.COUNT; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int adjacent = position + offset;
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

	public void generateQueenMoves(IntList output) {
		IntList queensToMove = getQueens(colorToMove);
		for (int q = queensToMove.size() - 1; q >= 0; q--) {
			int outputBase = output.size();
			int queen = queensToMove.get(q);
			traceAll(queen, output);
			if (output.size() == outputBase && surroundedByArrows(queen)) {
				queensToMove.removeIndex(q);
			}
			for (int i = outputBase; i < output.size(); i++) {
				output.set(i, Move.encodeQueenMove(queen, output.get(i)));
			}
		}
	}

	public void generateQueenMoves(IntList output, int position) {
		int outputBase = output.size();
		traceAll(position, output);
		if (output.size() == outputBase && surroundedByArrows(position)) {
			Color color = Piece.colorOfQueen(board[position]);
			getQueens(color).removeValue(position);
		}
		for (int i = outputBase; i < output.size(); i++) {
			output.set(i, Move.encodeQueenMove(position, output.get(i)));
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

	public void computeChambers() {
		if (chambersUpdated) {
			return;
		}
		if (whiteChambers == null) {
			whiteChambers = new IntList(MAX_QUEENS_PER_COLOR);
		} else {
			whiteChambers.clear();
		}
		if (blackChambers == null) {
			blackChambers = new IntList(MAX_QUEENS_PER_COLOR);
		} else {
			blackChambers.clear();
		}
		if (allChambers == null) {
			allChambers = new IntList(2*MAX_QUEENS_PER_COLOR);
		} else {
			allChambers.clear();
		}
		chamberAnalyzer.update();
		for (int i = 0; i < whiteQueens.size(); i++) {
			int queen = whiteQueens.get(i);
			int chamber = chamberAnalyzer.getPositionChamber(queen);
			whiteChambers.push(chamber);
			allChambers.push(chamber);
		}
		for (int i = 0; i < blackQueens.size(); i++) {
			int queen = blackQueens.get(i);
			int chamber = chamberAnalyzer.getPositionChamber(queen);
			blackChambers.push(chamber);
			allChambers.push(chamber);
		}
		allChambers.sort();
		chambersUpdated = true;
	}

	public IntList getWhiteChambers() {
		return whiteChambers;
	}

	public IntList getBlackChambers() {
		return blackChambers;
	}

	public IntList getQueenChambers(Color color) {
		switch (color) {
			case WHITE:
				return whiteChambers;
			case BLACK:
				return blackChambers;
			default:
				throw new IllegalArgumentException("invalid color");
		}
	}

	public IntList getQueenChambers() {
		return allChambers;
	}

	public int getChamberSize(int chamber) {
		return chamberAnalyzer.getChamberSize(chamber);
	}

	@Override
	public Board clone()
    {
		return new Board(this);
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
