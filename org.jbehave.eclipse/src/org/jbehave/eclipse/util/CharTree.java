package org.jbehave.eclipse.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharTree<T> {
	private Map<Integer, CharTree<T>> children = new HashMap<Integer, CharTree<T>>();
	private final int key;
	private T value;

	public CharTree(int key) {
		this(key, null);
	}

	public CharTree(int key, T value) {
		this.key = key;
		this.value = value;
	}

	public T lookup(CharSequence sequence) {
		CharTree<T> tree = this;
		T found = this.value;
		for (int i = 0, n = sequence.length(); i < n; i++) {
			tree = tree.get(sequence.charAt(i));
			if (tree == null) {
				break;
			}
			if (tree.value != null) {
				found = tree.value;
			}
		}
		return found;
	}

	public void print(int level) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < level; i++) {
			builder.append(" |  ");
		}
		System.out.print(builder.toString());
		System.out.print("[");
		System.out.print((char) key);
		System.out.print("] ");
		System.out.println(value == null ? "n/a" : value);

		List<Integer> keys = new ArrayList<Integer>(children.keySet());
		Collections.sort(keys);
		for (Integer key : keys) {
			children.get(key).print(level + 1);
		}
	}

	public void push(CharSequence seq, T value) {
		push(seq, 0, value);
	}

	private void push(CharSequence sequence, int position, T value) {
		if (position < sequence.length()) {
			int c = sequence.charAt(position);
			CharTree<T> child = getOrCreate(c);
			child.push(sequence, position + 1, value);
		} else {
			this.value = value;
		}
	}

	private CharTree<T> get(int c) {
		return children.get(c);
	}

	private CharTree<T> getOrCreate(int c) {
		CharTree<T> tree = children.get(c);
		if (tree == null) {
			tree = new CharTree<T>(c);
			children.put(c, tree);
		}
		return tree;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}
}
