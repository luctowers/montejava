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

	/** The dimensions of the board. */
	public final int width, height;

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
	private Map<Color,List<Position>> queenPositions;

	/** A private constructor that is only used in the clone method. */
	private State(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/** Creates a custom Amazons board of any size with any number of queens. */
	public State(int width, int height, List<Position> whiteQueens, List<Position> blackQueens) {
		this.width = width;
		this.height = height;
		this.board = new Piece[width*height];
		this.colorToMove = Color.WHITE;
		this.nextMoveType = MoveType.QUEEN;
		this.moves = new Stack<>();
		this.queenPositions = new EnumMap<>(Color.class);
		queenPositions.put(Color.WHITE, new ArrayList<>(whiteQueens));
		queenPositions.put(Color.BLACK, new ArrayList<>(blackQueens));
		for (Position queenPosition : whiteQueens) {
			board[boardIndex(queenPosition)] = Piece.WHITE_QUEEN;
		}
		for (Position queenPosition : blackQueens) {
			board[boardIndex(queenPosition)] = Piece.BLACK_QUEEN;
		}
	}

	/** Creates a custom Amazons board of any size with any number of queens and some pre-existing arrows. */
	public State(int width, int height, List<Position> whiteQueens, List<Position> blackQueens, List<Position> arrows) {
		this(width, height, whiteQueens, blackQueens);
		for (Position arrow : arrows) {
			board[boardIndex(arrow)] = Piece.ARROW;
		}
	}

	/** Creates a standard 10x10 amazons board with 4 queens per color. */
	public State() {
		this(
			10, 10,
			Arrays.asList(
				new Position(0, 6),
				new Position(3, 9),
				new Position(6, 9),
				new Position(9, 6)
			),
			Arrays.asList(
				new Position(0, 3),
				new Position(3, 0),
				new Position(6, 0),
				new Position(9, 3)
			)
		);
	}

	/** Updates the game state by performing a move. */
	public void doMove(Move move) {
		switch (move.type) {
			case QUEEN:
				updateQueenPosition(colorToMove, move.source, move.destination);
				nextMoveType = MoveType.ARROW;
				break;
			case ARROW:
				board[boardIndex(move.destination)] = Piece.ARROW;
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
				board[boardIndex(move.destination)] = null;
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
	private void updateQueenPosition(Color color, Position oldPosition, Position newPosition) {
		List<Position> positions = queenPositions.get(color);
		for (int i = 0; i < positions.size(); i++) {
			if (positions.get(i).equals(oldPosition)) {
				positions.set(i, newPosition);
				break;
			}
		}
		board[boardIndex(oldPosition)] = null;
		board[boardIndex(newPosition)] = Piece.queenOfColor(color);
	}

	/** Get's an immutable list of teh positions of all queens of a given color. */
	public List<Position> getQueens(Color color) {
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
	public Position getLastMovedQueen() {
		Move move = moves.peek();
		if (move.type == MoveType.QUEEN) {
			return move.destination;
		} else {
			return null;
		}
	}

	/** Gets and the positions that can be reached orthogonally or diagonally from a position. */
	public List<Position> traceAll(Position source) {
		ArrayList<Position> destinations = new ArrayList<>(27);
		for (Direction direction : Direction.VALUES) {
			int maxDistance = traceMaxDistance(source, direction);
			Position movedPosition = source.offset(direction);
			for (int i = 0; i < maxDistance && getPiece(movedPosition) == null; i++) {
				destinations.add(movedPosition);
				movedPosition = movedPosition.offset(direction);
			}
		}
		return destinations;
	}

	/** Gets and the positions that can be reached in a given direction. */
	public List<Position> traceDirection(Position source, Direction direction) {
		ArrayList<Position> destinations = new ArrayList<>(9);
		int maxDistance = traceMaxDistance(source, direction);
		Position movedPosition = source.offset(direction);
		for (int i = 0; i < maxDistance && getPiece(movedPosition) == null; i++) {
			destinations.add(movedPosition);
			movedPosition = movedPosition.offset(direction);
		}
		return destinations;
	}

	/** Helper function to get the max distance before a trace would go off the board and out of bounds. */
	private int traceMaxDistance(Position source, Direction direction) {
		int maxDistance = Integer.MAX_VALUE;
		if (direction.x == 1) {
			maxDistance = Math.min(maxDistance, width - source.x - 1);
		} else if (direction.x == -1) {
			maxDistance = Math.min(maxDistance, source.x);
		}
		if (direction.y == 1) {
			maxDistance = Math.min(maxDistance, height - source.y - 1);
		} else if (direction.y == -1) {
			maxDistance = Math.min(maxDistance, source.y);
		}
		return maxDistance;
	}

	/** Gets the piece type at a given board index. */
	public Piece getPiece(int index) {
		return board[index];
	}

	/** Gets the piece type at a given board position. */
	public Piece getPiece(Position position) {
		return board[boardIndex(position)];
	}

	/** Calculates the board index of a position. */
	public int boardIndex(Position position) {
		return position.x + position.y * width;
	}

	/** Determines where a board position is on or off the board. */
	public boolean positionValid(Position position) {
		return (
			position.x >= 0 && position.x < width &&
			position.y >= 0 && position.y < height
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	/** Copies the current state to a new object. */
	public State clone()
    {
		State cloneState = new State(width, height);
		cloneState.board = board.clone();
		cloneState.colorToMove = colorToMove;
		cloneState.nextMoveType = nextMoveType;
		cloneState.moves = (Stack<Move>) moves.clone();
		cloneState.queenPositions = new EnumMap<>(Color.class);
		cloneState.queenPositions.put(Color.WHITE, new ArrayList<>(queenPositions.get(Color.WHITE)));
		cloneState.queenPositions.put(Color.BLACK, new ArrayList<>(queenPositions.get(Color.BLACK)));
        return cloneState;
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
				highlightedBlank = boardIndex(move.source);
				highlightedQueen = boardIndex(move.destination);
			} else if (move.type == MoveType.ARROW) {
				if (moves.size() >= 2) {
					Move previousMove = moves.get(moves.size()-2);
					highlightedBlank = boardIndex(previousMove.source);
				}
				highlightedQueen = boardIndex(move.source);
				highlightedArrow = boardIndex(move.destination);
			}
		}
		builder.append(ConsoleColors.GREEN);
		builder.append(line);
		builder.append('\n');
		builder.append(ConsoleColors.RESET);
		for (int i = 0; i < width * height; i++) {
			if (i % width == 0) {
				builder.append(ConsoleColors.GREEN);
				builder.append('|');
				builder.append(ConsoleColors.RESET);
			}
			builder.append(' ');
			Piece piece = board[i];
			if (piece == null) {
				if (i == highlightedBlank) {
					builder.append(ConsoleColors.YELLOW);
					builder.append('!');
				} else {
					builder.append(' ');
				}
			} else if (piece == Piece.ARROW) {
				if (i == highlightedArrow) {
					builder.append(ConsoleColors.YELLOW);
				} else {
					builder.append(ConsoleColors.GREEN);
				}
				builder.append('*');
			} else if (piece == Piece.WHITE_QUEEN) {
				if (i == highlightedQueen) {
					builder.append(ConsoleColors.YELLOW);
				}
				builder.append('W');
			} else if (piece == Piece.BLACK_QUEEN) {
				if (i == highlightedQueen) {
					builder.append(ConsoleColors.YELLOW);
				}
				builder.append('B');
			} else {
				builder.append('?');
			}
			builder.append(ConsoleColors.RESET);
			builder.append(ConsoleColors.GREEN);
			builder.append(' ');
			builder.append('|');
			if (i % width == width - 1) {
				builder.append('\n');
				builder.append(line);
				builder.append('\n');
			}
			builder.append(ConsoleColors.RESET);
		}
		builder.append(colorToMove.toString() + " to make " + nextMoveType.toString() + " move");
		return builder.toString();
	}

	/** Helper function for board toString. */
	private String horiztonalLine() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 4*width+1; i++) {
			if (i % 4 == 0) {
				builder.append('+');
			} else {
				builder.append('-');
			}
		}
		return builder.toString();
	}

}
