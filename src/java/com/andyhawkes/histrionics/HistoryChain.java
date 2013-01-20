package com.andyhawkes.histrionics;

/**
 * A history chain. This is used to run custom history actions, and keep them
 * around in undo/redo stacks so they can be invoked later, to restore the
 * application data to some previous state.
 */
public interface HistoryChain {
	public void run(HistoryAction action);

	public void undo();

	public void redo();

	public boolean canUndo();

	public boolean canRedo();

	public void clear();
}
