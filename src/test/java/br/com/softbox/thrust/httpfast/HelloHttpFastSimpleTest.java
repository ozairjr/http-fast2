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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import br.com.softbox.thrust.core.Thrust;

@TestMethodOrder(Alphanumeric.class)
public class HelloHttpFastSimpleTest {

	private static final Path projectPath = Paths.get("src/test/js/hello");
	private static final Path projectLibPath = projectPath.resolve(".lib");
	private static final Path projectMainPath = projectPath.resolve("hello-main.js");
	private static final Path bitcodeRootPath = projectLibPath
			.resolve(Paths.get("bitcodes", HttpFastWorkerThread.HTTP_FAST_BITCODE));
	private static ExecutorService thrustExecutorService;

	private Client restClient;

	@BeforeAll
	public static void prepare() throws Exception {
		mkdirOrClean();
		copyBitcodes();
		initThrust();
	}

	@AfterAll
	public static void finish() throws Exception {
		HttpFast httpFast = HttpFast.getInstance();
		if (httpFast != null) {
			Object[] ret = httpFast.stopServer();
			System.out.println("Finished httpserver. Errors: " + ret.length);
		}
		thrustExecutorService.shutdownNow();
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
		Assertions.assertTrue(ok.get(), "thrust must be running");
	}

	static void copyBitcodes() throws IOException {
		Path bitcodesSrcPath = Paths.get("lib");
		try (Stream<Path> walk = Files.list(bitcodesSrcPath)) {
			walk.forEach(HelloHttpFastSimpleTest::copyTo);
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

	@BeforeEach
	public void prepareForEachTest() {
		this.restClient = ClientBuilder.newClient();
	}

	private WebTarget target(String sufix) {
		String url = String.format("http://localhost:3000%s%s", sufix.startsWith("/") ? "" : "/", sufix);
		return restClient.target(url);
	}

	@Test
	public void test01NotFound() throws Exception {
		WebTarget webTarget = target("hello");
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getStatus(), 404);
	}

	@Test
	public void test02NoName() throws Exception {
		WebTarget webTarget = target("api/hello");
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getStatus(), 500);
	}

	@Test
	public void test03NameParam() throws Exception {
		final String keyResponse = "hello";
		String name = "someone";
		WebTarget webTarget = target("api/hello");
		Response response = webTarget.queryParam("name", name).request(MediaType.APPLICATION_JSON).get();
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getStatus(), 200);

		Assertions.assertTrue(response.hasEntity());

		String jsonStr = response.readEntity(String.class);
		Assertions.assertNotNull(jsonStr);
		JSONObject json = new JSONObject(jsonStr);
		Assertions.assertTrue(json.has(keyResponse));
		String nameResponse = json.getString(keyResponse);
		Assertions.assertEquals(nameResponse, name);
	}

	@Test
	public void test04HeaderParam() throws Exception {
		String name = "LuizaLabs";
		WebTarget webTarget = target("api/hello");
		Response response = webTarget.queryParam("name", name).request(MediaType.APPLICATION_JSON).header("x-name", "1")
				.get();
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getStatus(), 200);

		Assertions.assertTrue(response.hasEntity());

		String str = response.readEntity(String.class);
		Assertions.assertNotNull(str);
		Assertions.assertEquals(str, String.format("|%s|", name));
	}

	@Test
	public void test10ByeInvalid() throws Exception {
		WebTarget webTarget = target("api/bye/33");
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		Assertions.assertNotNull(response);
		Assertions.assertEquals(response.getStatus(), 500);
	}

	@Test
	public void test11ByeBreakApp() throws Exception {
		try {
			target("api/bye/1").request().buildGet().invoke();
			Assertions.fail("Cannot run this");
		} catch (Exception e) {
			Assertions.assertNotNull(e);
			Assertions.assertTrue(e instanceof ProcessingException);
		}
	}
}
