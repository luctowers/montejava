package ubc.cosc322.engine.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ubc.cosc322.engine.util.ConsoleColors;

/** A complete mutable board state of the game of amazons. */
public class State {

	public final Dimensions dimensions;

	/** A single dimensional array of size width*weight for piece lookups. */
	private Piece[] board;

	/** The next color that should make a move. */
	private Color colorToMove;

	/** The next move type that is expected. */
	private MoveType nextMoveType;

	/** A stack of previously performed moves. Used primarily to undo moves. */
	private Stack<Move> moves;

	/** A list of queen positions for each color. This is a helper structs that
	 * allows easy location of queens without the need to iterate through the 
	 * entire board. This is efficient because there aren't that many queens.
	 */
	private Map<Color,ArrayList<Integer>> queenPositions;

	/** Creates a custom Amazons board of any size with any number of queens. */
	public State(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.board = new Piece[dimensions.arrayWidth*dimensions.arrayHeight];
		this.colorToMove = Color.WHITE;
		this.nextMoveType = MoveType.QUEEN;
		this.moves = new Stack<>();
		this.queenPositions = new EnumMap<>(Color.class);
		this.queenPositions.put(Color.WHITE, new ArrayList<>(4));
		this.queenPositions.put(Color.BLACK, new ArrayList<>(4));
	}

	/** Creates a standard 10x10 amazons board with 4 queens per color. */
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

	@SuppressWarnings("unchecked")
	private State(State other) {
		this.dimensions = other.dimensions;
		this.board = other.board.clone();
		this.colorToMove = other.colorToMove;
		this.nextMoveType = other.nextMoveType;
		this.moves = (Stack<Move>) other.moves.clone();
		this.queenPositions = new EnumMap<>(Color.class);
		this.queenPositions.put(Color.WHITE, new ArrayList<>(other.queenPositions.get(Color.WHITE)));
		this.queenPositions.put(Color.BLACK, new ArrayList<>(other.queenPositions.get(Color.BLACK)));
	}

	public void placeQueen(Color color, int position) {
		placePiece(Piece.queenOfColor(color), position);
		queenPositions.get(color).add(position);
	}

	public void placeArrow(int position) {
		placePiece(Piece.ARROW, position);
	}

	private void placePiece(Piece piece, int position) {
		if (dimensions.outOfBounds(position)) {
			throw new IllegalArgumentException("invalid board position");
		}
		if (board[position] != null) {
			throw new IllegalArgumentException("position is not empty");
		}
		board[position] = piece;
	}

	/** Updates the game state by performing a move. */
	public void doMove(Move move) {
		switch (move.type) {
			case QUEEN:
				updateQueenPosition(colorToMove, move.source, move.destination);
				nextMoveType = MoveType.ARROW;
				break;
			case ARROW:
				board[move.destination] = Piece.ARROW;
				nextMoveType = MoveType.QUEEN;
				colorToMove = colorToMove.other();
				break;
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
		moves.add(move);
	}

	/** Updates the game state by performing a turn (2 moves by one player). */
	public void doTurn(Turn turn) {
		doMove(turn.queenMove());
		doMove(turn.arrowMove());
	}

	/** Updates the game state by undoing the last move performed. */
	public void undoMove() {
		Move move = moves.pop();
		switch (move.type) {
			case QUEEN:
				updateQueenPosition(colorToMove, move.destination, move.source);
				nextMoveType = MoveType.QUEEN;
				break;
			case ARROW:
				board[move.destination] = null;
				nextMoveType = MoveType.ARROW;
				colorToMove = colorToMove.other();
				break;
			default:
				throw new IllegalArgumentException("Illegal move type");
		}
	}

	/** Updates the game state by undoing the last two moves performed. */
	public void undoTurn(Turn turn) {
		undoMove();
		undoMove();
	}

	/** Helper method to update a queen's position in both the board and helper structs. */
	private void updateQueenPosition(Color color, int oldPosition, int newPosition) {
		ArrayList<Integer> positions = queenPositions.get(color);
		for (int i = 0; i < positions.size(); i++) {
			if (positions.get(i).intValue() == oldPosition) {
				positions.set(i, newPosition);
				break;
			}
		}
		board[oldPosition] = null;
		board[newPosition] = Piece.queenOfColor(color);
	}

	/** Get's an immutable list of teh positions of all queens of a given color. */
	public List<Integer> getQueens(Color color) {
		return Collections.unmodifiableList(queenPositions.get(color));
	}

	/** Returns the next color that is expected to make a move. */
	public Color getColorToMove() {
		return colorToMove;
	}

	/** Returns the next move type that is expected. */
	public MoveType getNextMoveType() {
		return nextMoveType;
	}

	/** Gets the location of the last queen to move. Used to determine where arrows are shot from. */
	public int getLastMovedQueen() {
		Move move = moves.peek();
		if (move.type == MoveType.QUEEN) {
			return move.destination;
		} else {
			throw new IllegalStateException("no queen has been moved yet");
		}
	}

	/** Gets and the positions that can be reached orthogonally or diagonally from a position. */
	public int[] traceAll(int source) {
		int[] destinations = new int[dimensions.maxTrace];
		int i = 0;
		for (int d = 0; d < Direction.count; d++) {
			int offset = dimensions.getDirectionOffset(d);
			int position = source + offset;
			while (!dimensions.outOfBounds(position) && board[position] == null) {
				destinations[i++] = position;
				position += offset;
			}
		}
		for (; i < dimensions.maxTrace; i++) {
			destinations[i] = -1;
		}
		return destinations;
	}

	public ArrayList<Move> generateMoves() {
		switch (nextMoveType) {
			case QUEEN:
				return generateQueenMoves();
			case ARROW:
				return generateArrowMoves();
			default:
				throw new IllegalStateException("Illegal move type");
		}
	}

	private ArrayList<Move> generateQueenMoves() {
		ArrayList<Integer> queens = queenPositions.get(colorToMove);
		ArrayList<Move> moves = new ArrayList<>(queens.size()*dimensions.maxTrace);
		for (int source : queens) {
			for (int destination : traceAll(source)) {
				if (destination == -1) {
					break;
				}
				moves.add(new Move(
					MoveType.QUEEN,
					source,
					destination
				));
			}
		}
		return moves;
	}

	private ArrayList<Move> generateArrowMoves() {
		ArrayList<Move> moves = new ArrayList<>(dimensions.maxTrace);
		int source = getLastMovedQueen();
		for (int destination : traceAll(source)) {
			if (destination == -1) {
				break;
			}
			moves.add(new Move(
				MoveType.ARROW,
				source,
				destination
			));
		}
		return moves;
	}

	/** Gets the piece type at a given board index. */
	public Piece getPiece(int position) {
		return board[position];
	}

	@Override
	/** Copies the current state to a new object. */
	public State clone()
    {
		return new State(this);
    }

	@Override
	/** Creates a fancy colored console representation of the current board state. */
	public String toString() { 
		StringBuilder builder = new StringBuilder();
		String line = horiztonalLine();
		int highlightedBlank = -1;
		int highlightedQueen = -1;
		int highlightedArrow = -1;
		if (moves.size() >= 1) {
			Move move = moves.peek();
			if (move.type == MoveType.QUEEN) {
				highlightedBlank = move.source;
				highlightedQueen = move.destination;
			} else if (move.type == MoveType.ARROW) {
				if (moves.size() >= 2) {
					Move previousMove = moves.get(moves.size()-2);
					highlightedBlank = previousMove.source;
				}
				highlightedQueen = move.source;
				highlightedArrow = move.destination;
			}
		}
		builder.append(ConsoleColors.GREEN);
		builder.append(line);
		builder.append('\n');
		for (int y = dimensions.boardHeight-1; y >= 0; y--) {
			builder.append("| ");
			for (int x = 0; x < dimensions.boardWidth; x++) {
				int position = dimensions.position(x, y);
				Piece piece = board[position];
				if (piece == null) {
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
