package com.andyhawkes.histrionics;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * History action that stores before and after state in byte arrays.
 * Implementations should handle the serialization of state into those byte
 * arrays in whatever way makes sense for them.
 * 
 * If you're using a SwappingHistoryChain, this action may be swapped to disk to
 * save working memory.
 */
public abstract class MementoAction implements HistoryAction {
	private static final Logger log = Logger.getLogger(MementoAction.class);

	byte[] before;
	byte[] after;

	public void run() {
		try {
			before = captureState();

			runInternal();

			after = captureState();
		} catch (Exception e) {
			log.warn("failed to run memento action", e);
		}
	}

	public void undo() {
		try {
			restoreState(before);
		} catch (Exception e) {
			log.warn("failed to undo memento action", e);
		}
	}

	public void redo() {
		try {
			restoreState(after);
		} catch (Exception e) {
			log.warn("failed to redo memento action", e);
		}
	}

	protected abstract void runInternal();

	protected abstract byte[] captureState() throws IOException;

	protected abstract void restoreState(byte[] state) throws IOException;
}
