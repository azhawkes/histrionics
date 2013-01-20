package com.andyhawkes.histrionics;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.junit.Before;
import org.junit.Test;

public class SwappingActionTest {
	private StringBuffer buf = new StringBuffer("...");

	@Before
	public void configureLog4j() {
		Logger.getLogger("com.andyhawkes.histrionics").addAppender(new ConsoleAppender(new TTCCLayout()));
		Logger.getLogger("com.andyhawkes.histrionics").setLevel(Level.DEBUG);
	}

	@Test
	public void testSwappingAndUnswapping() throws Exception {
		AppendAction action = new AppendAction();
		SwappingAction swapper = new SwappingAction(action);

		action.run();

		assertTrue(buf.toString().equals("...."));

		action.undo();
		action.redo();

		assertTrue(buf.toString().equals("...."));

		swapper.swap();
		swapper.undo();

		assertTrue(buf.toString().equals("..."));

		swapper.redo();

		assertTrue(buf.toString().equals("...."));
	}

	private class AppendAction extends MementoAction {
		protected void runInternal() {
			buf.append(".");
		}

		protected byte[] captureState() throws IOException {
			return buf.toString().getBytes("UTF-8");
		}

		protected void restoreState(byte[] state) throws IOException {
			buf.replace(0, buf.length(), new String(state, "UTF-8"));
		}
	};
}
