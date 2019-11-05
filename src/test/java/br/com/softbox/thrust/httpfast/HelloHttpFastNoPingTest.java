package br.com.softbox.thrust.httpfast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.softbox.thrust.core.Thrust;

public class HelloHttpFastNoPingTest {

	private static final Path projectPath = Paths.get("src/test/js/hello");
	private static final Path projectLibPath = projectPath.resolve(".lib");
	private static final Path projectMainPath = projectPath.resolve("hello-main.js");
	private static final Path bitcodeRootPath = projectLibPath
			.resolve(Paths.get("bitcodes", HttpFastWorkerThread.HTTP_FAST_BITCODE));
	private static ExecutorService thrustExecutorService;

	@BeforeAll
	public static void prepare() throws Exception {
		System.out.println("* Preparing for tests.");
		mkdirOrClean();
		copyBitcodes();
		initThrust();
	}

	@AfterAll
	public static void finish() throws Exception {
		System.out.println("* Finishing tests.");
		HttpFast httpFast = HttpFast.getInstance();
		if (httpFast != null) {
			Object[] ret = httpFast.stopServer();
			System.out.println("Finished httpserver. Errors: " + ret.length);
		}
		thrustExecutorService.shutdownNow();
		System.out.println("......");
	}

	static void initThrust() throws Exception {
		thrustExecutorService = Executors.newFixedThreadPool(1);
		AtomicBoolean ok = new AtomicBoolean(true);
		thrustExecutorService.execute(() -> {
			try {
				Thrust.main(new String[] { projectMainPath.toAbsolutePath().toString() });
			} catch (Exception e) {
				ok.set(false);
			}
		});
		Thread.sleep(3214);
		Assertions.assertTrue(ok.get(), "Trhust must be running");
	}

	static void copyBitcodes() throws IOException {
		Path bitcodesSrcPath = Paths.get("lib");
		try (Stream<Path> walk = Files.list(bitcodesSrcPath)) {
			walk.forEach(HelloHttpFastNoPingTest::copyTo);
		}
	}

	static void copyTo(Path srcBitcodePath) {
		Path dstBitcodePath = bitcodeRootPath.resolve(srcBitcodePath.getFileName());
		try {
			Files.copy(srcBitcodePath, dstBitcodePath);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to copy " + srcBitcodePath, ioe);
		}
	}

	static void mkdirOrClean() throws IOException {
		clean();
		if (!bitcodeRootPath.toFile().mkdirs()) {
			throw new RuntimeException("Failed to create directory");
		}
	}

	private static void clean() throws IOException {
		if (Files.exists(bitcodeRootPath)) {
			try (Stream<Path> walk = Files.walk(projectLibPath)) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
	}

	@Test
	public void justWakeUp() throws Exception {
		System.out.println("Just wake up");
	}
}
