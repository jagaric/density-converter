package at.favre.tools.dconvert.test;

import at.favre.tools.dconvert.Converter;
import at.favre.tools.dconvert.arg.Arguments;
import at.favre.tools.dconvert.arg.EPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests main converter class
 */
public class ConverterTest {

	private final static long WAIT_SEC = 12;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	private CountDownLatch latch;
	private File src;
	private File dst;

	@Before
	public void setUp() throws Exception {
		latch = new CountDownLatch(1);
		src = temporaryFolder.newFolder("convert-test", "src");
		dst = temporaryFolder.newFolder("convert-test", "out");
	}

	@Test
	public void testZeroFilesInput() throws Exception {
		TestCallback callback = new TestCallback(0, Collections.emptyList(), false, latch);
		new Converter().execute(new Arguments.Builder(null, Arguments.DEFAULT_SCALE).threadCount(4).skipParamValidation(true).build(), false, callback);
		assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
		checkResult(callback);
	}

	@Test
	public void testSingleFileIosPlatformConverter() throws Exception {
		List<File> files = AConverterTest.copyToTestPath(src, "png_example1_alpha_144.png");
		Arguments arg = new Arguments.Builder(src, Arguments.DEFAULT_SCALE).platform(EPlatform.IOS).dstFolder(dst).threadCount(4).build();
		TestCallback callback = new TestCallback(files.size(), Collections.emptyList(), false, latch);
		new Converter().execute(arg, false, callback);
		assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
		checkResult(callback);
		IOSConverterTest.checkOutDirIos(dst, arg, files);
	}

	@Test
	public void testAndroidPlatformConverter() throws Exception {
		List<File> files = AConverterTest.copyToTestPath(src, "png_example3_alpha_128.png", "png_example1_alpha_144.png", "jpg_example2_512.jpg", "gif_example_640.gif", "png_example4_500.png", "psd_example_827.psd");
		Arguments arg = new Arguments.Builder(src, Arguments.DEFAULT_SCALE).platform(EPlatform.ANDROID).dstFolder(dst).threadCount(4).build();
		TestCallback callback = new TestCallback(files.size(), Collections.emptyList(), false, latch);
		new Converter().execute(arg, false, callback);
		assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
		checkResult(callback);
		AndroidConverterTest.checkOutDirAndroid(dst, arg, files);
	}

	@Test
	public void testAllPlatformConverter() throws Exception {
		List<File> files = AConverterTest.copyToTestPath(src, "png_example3_alpha_128.png", "png_example1_alpha_144.png", "jpg_example2_512.jpg");
		Arguments arg = new Arguments.Builder(src, Arguments.DEFAULT_SCALE).platform(EPlatform.ALL).dstFolder(dst).threadCount(4).build();
		TestCallback callback = new TestCallback(files.size() * 3, Collections.emptyList(), false, latch);
		new Converter().execute(arg, false, callback);
		assertTrue(latch.await(WAIT_SEC, TimeUnit.SECONDS));
		checkResult(callback);
		AConverterTest.checkMultiPlatformConvert(dst, arg, files);
	}

	private void checkResult(TestCallback callback) {
		assertEquals(callback.expectedJobs, callback.actualJobs);
		assertEquals(callback.expectedExceptions, callback.actualExceptions);
		assertEquals(callback.expectedHaltDuringProcess, callback.actualHaltDuringProcess);
	}

	private static class TestCallback implements Converter.HandlerCallback {
		private final int expectedJobs;
		private final List<Exception> expectedExceptions;
		private final boolean expectedHaltDuringProcess;
		private final CountDownLatch latch;
		private int actualJobs;
		private List<Exception> actualExceptions;
		private boolean actualHaltDuringProcess;

		public TestCallback(int expectedJobs, List<Exception> expectedExceptions, boolean expectedHaltDuringProcess, CountDownLatch latch) {
			this.expectedJobs = expectedJobs;
			this.expectedExceptions = expectedExceptions;
			this.expectedHaltDuringProcess = expectedHaltDuringProcess;
			this.latch = latch;
		}

		@Override
		public void onProgress(float percent) {
		}

		@Override
		public void onFinished(int finishedJobs, List<Exception> exceptions, long time, boolean haltedDuringProcess, String log) {
			actualJobs = finishedJobs;
			actualExceptions = exceptions;
			actualHaltDuringProcess = haltedDuringProcess;
			latch.countDown();
		}
	}
}
