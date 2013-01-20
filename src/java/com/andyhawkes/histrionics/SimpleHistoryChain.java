package com.andyhawkes.histrionics;

import java.util.Stack;

/**
 * Simple history chain that stores all actions in memory.
 */
public class SimpleHistoryChain implements HistoryChain {
	protected Stack<HistoryAction> undoables = new Stack<HistoryAction>();
	protected Stack<HistoryAction> redoables = new Stack<HistoryAction>();

	public void run(HistoryAction action) {
		undoables.push(action);
		redoables.clear();

		action.run();
	}

	public void undo() {
		HistoryAction action = undoables.pop();

		undoables.remove(action);
		redoables.push(action);

		action.undo();
	}

	public void redo() {
		HistoryAction action = redoables.pop();

		action.redo();

		redoables.remove(action);
		undoables.push(action);
	}

	public boolean canUndo() {
		return undoables.size() > 0;
	}

	public boolean canRedo() {
		return redoables.size() > 0;
	}

	public void clear() {
		undoables.clear();
		redoables.clear();
	}
}
