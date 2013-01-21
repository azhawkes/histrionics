package com.andyhawkes.histrionics;

/**
 * An action (similar to Runnable) that is capable of undoing and redoing
 * itself.
 */
public interface HistoryAction {
	public void run();

	public void undo();

	public void redo();
}
