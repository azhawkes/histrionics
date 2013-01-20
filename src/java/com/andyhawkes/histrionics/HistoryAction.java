package com.andyhawkes.histrionics;

/**
 * An action that is capable of undoing and redoing itself.
 */
public interface HistoryAction {
	public void run();

	public void undo();

	public void redo();
}
