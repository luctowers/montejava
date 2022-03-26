package ubc.cosc322.engine.util;

public final class IntList {
	
	private int[] array;
	private int size;
	
	public IntList(int capacity) {
		this.array = new int[capacity];
		this.size = 0;
	}

	public IntList(int[] array, int size) {
		this.array = array;
		this.size = size;
	}

	public int size() {
		return size;
	}

	public void push(int n) {
		array[size] = n;
		size++;
	}

	public int pop() {
		int n = array[size-1];
		size--;
		return n;
	}

	public int get(int i) {
		// DEBUG ASSERTION
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
		return array[i];
	}

	public void set(int i, int n) {
		// DEBUG ASSERTION
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
		array[i] = n;
	}

	public void remove(int i) {
		// DEBUG ASSERTION
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
		int copyLength = size - 1 - i;
		System.arraycopy(array, i+1, array, i, copyLength);
		size--;
	}

	public void clear() {
		size = 0;
	}

	public IntList clone() {
		return new IntList(array.clone(), size);
	}

}
