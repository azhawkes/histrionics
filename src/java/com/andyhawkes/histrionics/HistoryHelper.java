package com.andyhawkes.histrionics;

/**
 * History chain that can run ordinary runnables, first wrapping them in a
 * history action.
 */
public abstract class HistoryHelper extends SimpleHistoryChain {
	public abstract void runWithHistory(Runnable runnable);
}
