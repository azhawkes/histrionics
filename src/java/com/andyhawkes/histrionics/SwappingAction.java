package com.andyhawkes.histrionics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

/**
 * Action that wraps a MementoAction and takes care of swapping the byte arrays
 * (containing the before and after state) to a temp file when prompted. This is
 * used by the SwappingHistoryChain to save working memory when there get to be
 * too many actions.
 */
public class SwappingAction implements HistoryAction {
	private static final Logger log = Logger.getLogger(SwappingAction.class);

	private MementoAction action;
	private File swapDir;
	private File swap;

	public SwappingAction(MementoAction action, File swapDir) {
		this.action = action;
		this.swapDir = swapDir;
	}

	public void swap() throws IOException {
		try {
			swap = File.createTempFile("history-", ".tmp", swapDir);

			log.debug("created swap file at " + swap.getAbsolutePath());

			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(swap));

			output.writeObject(action.before);
			output.writeObject(action.after);
			output.close();

			action.before = null;
			action.after = null;
		} catch (Exception e) {
			throw new IOException("failed to swap history action to temp file " + swap, e);
		}
	}

	public void unswap() throws IOException {
		if (swap != null && swap.exists()) {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(swap));

				action.before = (byte[]) input.readObject();
				action.after = (byte[]) input.readObject();

				input.close();

				swap.delete();
				swap = null;
			} catch (Exception e) {
				throw new IOException("failed to unswap history action from temp file " + swap, e);
			}
		}
	}

	public void run() {
		try {
			unswap();
			action.run();
		} catch (Exception e) {
			log.error("couldn't run memento action", e);
		}
	}

	public void undo() {
		try {
			unswap();
			action.undo();
		} catch (Exception e) {
			log.error("couldn't undo memento action", e);
		}
	}

	public void redo() {
		try {
			unswap();
			action.redo();
		} catch (Exception e) {
			log.error("couldn't redo memento action", e);
		}
	}
}