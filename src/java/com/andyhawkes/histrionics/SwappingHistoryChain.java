package com.andyhawkes.histrionics;

import java.io.IOException;
import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * A history chain that swaps actions to a disk cache when there are too many of
 * them in the undoable queue. This is to limit memory usage, obviously.
 */
public class SwappingHistoryChain implements HistoryChain {
	private static final Logger log = Logger.getLogger(SwappingHistoryChain.class);

	protected Stack<HistoryAction> undoables = new Stack<HistoryAction>();
	protected Stack<HistoryAction> redoables = new Stack<HistoryAction>();

	private int maxMementoActionsInMemory;

	public SwappingHistoryChain(int maxMementoActionsInMemory) {
		this.maxMementoActionsInMemory = maxMementoActionsInMemory;
	}

	public void run(HistoryAction action) {
		undoables.push(action);
		redoables.clear();

		action.run();

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
					SwappingAction swapper = new SwappingAction((MementoAction) undoable);

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
