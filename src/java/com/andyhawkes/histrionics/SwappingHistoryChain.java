package com.andyhawkes.histrionics;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * A history chain that swaps actions to a disk cache when there are too many of
 * them in the undoable queue. People rarely undo/redo more than a few steps at
 * a time, so keeping large stacks of them in memory all the time is a waste,
 * especially with large memento actions.
 */
public class SwappingHistoryChain implements HistoryChain {
	private static final Logger log = Logger.getLogger(SwappingHistoryChain.class);

	protected Stack<HistoryAction> undoables = new Stack<HistoryAction>();
	protected Stack<HistoryAction> redoables = new Stack<HistoryAction>();

	private File swapDir;
	private int maxMementoActionsInMemory;

	public SwappingHistoryChain(int maxMementoActionsInMemory) {
		this.swapDir = new File(System.getProperty("java.io.tmpdir"));
		this.maxMementoActionsInMemory = maxMementoActionsInMemory;
	}

	public SwappingHistoryChain(File swapDir, int maxMementoActionsInMemory) {
		this.swapDir = swapDir;
		this.maxMementoActionsInMemory = maxMementoActionsInMemory;
	}

	public void run(HistoryAction action) {
		undoables.push(action);

		action.run();

		while (redoables.size() > 0) {
			HistoryAction redoable = redoables.pop();

			try {
				if (redoable instanceof SwappingAction) {
					((SwappingAction) redoable).unswap();
				}
			} catch (Exception e) {
				log.warn("failed to clean up swapped memento action", e);
			}
		}

		swapIfNecessary();
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
		while (undoables.size() > 0) {
			HistoryAction action = undoables.pop();

			try {
				if (action instanceof SwappingAction) {
					((SwappingAction) action).unswap();
				}
			} catch (Exception e) {
				log.warn("failed to clean up swapped memento action", e);
			}
		}

		while (redoables.size() > 0) {
			HistoryAction action = redoables.pop();

			try {
				if (action instanceof SwappingAction) {
					((SwappingAction) action).unswap();
				}
			} catch (Exception e) {
				log.warn("failed to clean up swapped memento action", e);
			}
		}
	}

	private void swapIfNecessary() {
		int actionsInMemory = countActionsInMemory();

		if (actionsInMemory > maxMementoActionsInMemory) {
			log.debug("more than " + maxMementoActionsInMemory + " unswapped memento actions in memory; swapping");

			for (int i = 0; i < undoables.size(); i++) {
				HistoryAction undoable = undoables.get(i);

				if (undoable instanceof MementoAction) {
					SwappingAction swapper = new SwappingAction((MementoAction) undoable, swapDir);

					try {
						swapper.swap();
						undoables.set(i, swapper);

						actionsInMemory--;
					} catch (IOException e) {
						log.warn("swapping action failed to swap", e);
					}
				}

				if (actionsInMemory <= maxMementoActionsInMemory) {
					break;
				}
			}
		}
	}

	private int countActionsInMemory() {
		int count = 0;

		for (HistoryAction undoable : undoables) {
			if (undoable instanceof MementoAction) {
				count++;
			}
		}

		return count;
	}
}
