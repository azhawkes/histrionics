package com.andyhawkes.histrionics;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.junit.Before;
import org.junit.Test;

public class SwappingActionTest {
	private static final Logger log = Logger.getLogger(SwappingActionTest.class);

	private File swapDir;
	private StringBuffer buf = new StringBuffer("...");

	@Before
	public void configure() {
		Logger.getLogger("com.andyhawkes.histrionics").addAppender(new ConsoleAppender(new TTCCLayout()));
		Logger.getLogger("com.andyhawkes.histrionics").setLevel(Level.DEBUG);

		swapDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "SwappingActionTest-" + System.currentTimeMillis());
		swapDir.mkdirs();

		log.info("created swap dir at " + swapDir.getAbsolutePath());
	}

	@Test
	public void testSwappingAndUnswapping() throws Exception {
		AppendAction action = new AppendAction();
		SwappingAction swapper = new SwappingAction(action, swapDir);

		action.run();

		assertTrue(buf.toString().equals("...."));

		action.undo();
		action.redo();

		assertTrue(buf.toString().equals("...."));

		swapper.swap();

		assertTrue("File should be swapped", swapDir.listFiles().length == 1);

		swapper.undo();

		assertTrue("File should be unswapped", swapDir.listFiles().length == 0);
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
