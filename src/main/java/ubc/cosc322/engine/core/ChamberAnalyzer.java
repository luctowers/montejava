package ubc.cosc322.engine.core;

import ubc.cosc322.engine.util.IntList;

public class ChamberAnalyzer {

	public final Dimensions dimensions;
	private int[] arrowBitmap;
	private int[] groupBoard;
	private int[] groupMappings;
	private int[] groupSizes;
	private IntList groupMasks;
	
	public ChamberAnalyzer(Dimensions dimensions) {
		this.dimensions = dimensions;
		this.arrowBitmap = new int[dimensions.boardHeight];
		this.groupBoard = new int[dimensions.arraySize];
		this.groupMappings = new int[dimensions.boardSize];
		this.groupSizes = new int[dimensions.boardSize];
		this.groupMasks = new IntList(dimensions.boardSize);
	}

	private ChamberAnalyzer(ChamberAnalyzer other) {
		this.dimensions = other.dimensions;
		this.arrowBitmap = other.arrowBitmap.clone();
		this.groupBoard = other.groupBoard.clone();
		this.groupMappings = other.groupMappings.clone();
		this.groupSizes = other.groupSizes.clone();
		this.groupMasks = other.groupMasks.clone();
	}

	public void placeArrow(int position) {
		arrowBitmap[dimensions.y(position)] |= 1 << dimensions.x(position);
	}

	public void update() {
		groupMasks.clear();
		int previousBase = 0;
		computeGroupMasks(arrowBitmap[0], 0);
		for (int y = 1; y < arrowBitmap.length; y++) {
			int nextBase = groupMasks.size();
			computeGroupMasks(arrowBitmap[y], y);
			for (int i = nextBase; i < groupMasks.size(); i++) {
				int parentMask = groupMasks.get(i);
				int connectionMask = connectionMask(parentMask);
				for (int j = previousBase; j < nextBase; j++) {
					int childMask = groupMasks.get(j);
					if ((connectionMask & childMask) != 0) {
						if (groupMappings[j] == j) {
							groupMappings[j] = groupMappings[i];
							groupSizes[i] += groupSizes[j];
						} else {
							groupMappings[i] = groupMappings[groupMappings[j]];
							groupSizes[groupMappings[j]] += groupSizes[i];
						}
					}
				}
			}
			previousBase = nextBase;
		}

		// System.out.println(Arrays.toString(groupMappings));
		// for (int y = 0; y < dimensions.boardHeight; y++) {
		// 	for (int x = 0; x < dimensions.boardWidth; x++) {
		// 		int mapping = groupBoard[dimensions.position(x, y)];
		// 		System.out.printf("%02d", mapping);
		// 		System.out.print(',');
		// 		if (x == dimensions.boardWidth - 1) {
		// 			System.out.println();
		// 		}
		// 	}
		// }
	}

	private void computeGroupMasks(int row, int y) {
		int group = 0;
		int shift = 0;
		int yOffset = y*dimensions.arrayWidth;
		do {
			while (((row >> shift) & 1) != 0 && shift < 10) {
				shift++;
			}
			int size = 0;
			while (((row >> shift) & 1) == 0 && shift < 10) {
				group = group | (1 << shift);
				groupBoard[shift+yOffset] = groupMasks.size();
				shift++;
				size++;
			}
			if (group != 0) {
				int groupIndex = groupMasks.size();
				groupMasks.push(group);
				groupMappings[groupIndex] = groupIndex;
				groupSizes[groupIndex] = size;
				group = 0;
			}
		} while (shift < 10);
	}

	private static int connectionMask(int row) {
		return (row | (row << 1) | (row >> 1)) & ((1 << 10) - 1);
	}

	public int getPositionChamber(int position) {
		// WARNING THIS MIGHT GO INTO AN INFINITE LOOP IF THE POSITION IS AN ARROW OR INVALID
		// UNDEFINED BEHAVIOUR, DON'T EVEN TRY IT
		// NOT CHECKING FOR PERFORMANCE REASONS
		int mapping = groupBoard[position];
		while (groupMappings[mapping] != mapping) {
			mapping = groupMappings[mapping];
		}
		return mapping;
	}

	public int getChamberSize(int chamber) {
		return groupSizes[chamber];
	}

	public ChamberAnalyzer clone() {
		return new ChamberAnalyzer(this);
	}

}
