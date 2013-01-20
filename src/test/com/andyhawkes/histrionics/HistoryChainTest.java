package com.andyhawkes.histrionics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the history chain and swapping history chain.
 */
public class HistoryChainTest {
	private Random random = new Random();
	private int[] testData = new int[] { 1, 4, 9, 3, 7, 2, 1, 5, 8, 6 };
	private int[] workingData = Arrays.copyOf(testData, testData.length);

	@Before
	public void configureLog4j() {
		Logger.getLogger("com.andyhawkes.histrionics").addAppender(new ConsoleAppender(new TTCCLayout()));
		Logger.getLogger("com.andyhawkes.histrionics").setLevel(Level.DEBUG);
	}

	@Test
	public void testSimpleHistoryChain() {
		HistoryChain chain = new SimpleHistoryChain();

		for (int i = 0; i < 50; i++) {
			chain.run(createRandomAction());
		}

		assertTrue(chain.canUndo());
		assertFalse(chain.canRedo());

		for (int i = 0; i < 20; i++) {
			chain.undo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 10; i++) {
			chain.redo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 5; i++) {
			chain.undo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 10; i++) {
			chain.run(createRandomAction());
		}

		assertTrue(chain.canUndo());
		assertFalse(chain.canRedo());

		for (int i = 0; i < 45; i++) {
			chain.undo();
		}

		assertFalse(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < testData.length; i++) {
			assertEquals("Test data must be the same as we started with", testData[i], workingData[i]);
		}
	}

	@Test
	public void testSwappingHistoryChain() {
		HistoryChain chain = new SwappingHistoryChain(7);

		for (int i = 0; i < 50; i++) {
			chain.run(createRandomAction());
		}

		assertTrue(chain.canUndo());
		assertFalse(chain.canRedo());

		for (int i = 0; i < 20; i++) {
			chain.undo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 10; i++) {
			chain.redo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 5; i++) {
			chain.undo();
		}

		assertTrue(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < 10; i++) {
			chain.run(createRandomAction());
		}

		assertTrue(chain.canUndo());
		assertFalse(chain.canRedo());

		for (int i = 0; i < 45; i++) {
			chain.undo();
		}

		assertFalse(chain.canUndo());
		assertTrue(chain.canRedo());

		for (int i = 0; i < testData.length; i++) {
			assertEquals("Test data must be the same as we started with", testData[i], workingData[i]);
		}
	}

	private HistoryAction createRandomAction() {
		if (random.nextBoolean()) {
			return new RandomOperationAction();
		} else {
			return new ShuffleAction();
		}
	}

	/**
	 * Simple action that performs a random addition/subtraction operation on a
	 * random index in the test data array.
	 */
	private class RandomOperationAction implements HistoryAction {
		int index = (int) (Math.random() * testData.length);
		int shift = (int) (Math.random() * 6) - 3;

		public void run() {
			workingData[index] += shift;
		}

		public void undo() {
			workingData[index] -= shift;
		}

		public void redo() {
			workingData[index] += shift;
		}
	}

	private class ShuffleAction extends MementoAction implements Serializable {
		private static final long serialVersionUID = 1L;

		protected void runInternal() {
			Collections.shuffle(Arrays.asList(workingData));
		}

		protected byte[] captureState() throws IOException {
			ByteArrayOutputStream state = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(state);

			output.writeObject(workingData);
			output.close();

			return state.toByteArray();
		}

		protected void restoreState(byte[] state) throws IOException {
			try {
				ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(state));
				int[] cannedData = (int[]) input.readObject();

				for (int i = 0; i < workingData.length; i++) {
					workingData[i] = cannedData[i];
				}

				input.close();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}
}
