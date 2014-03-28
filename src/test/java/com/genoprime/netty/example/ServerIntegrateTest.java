package com.genoprime.netty.example;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */

@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "server")
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 20, filePrefix = "hx-server")
public class ServerIntegrateTest {
    private static final Logger logger = LoggerFactory.getLogger(ServerIntegrateTest.class);

    public final static int DEFAULT_BENCHMARK_ROUNDS = 50;
    public final static int DEFAULT_WARMUP_ROUNDS = 5;

    public final static int DEFAULT_CONCURRENT_BENCHMARK_ROUNDS = 500;
    public final static int DEFAULT_CONCURRENT_WARMUP_ROUNDS = 10;

    private static Thread thread;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @BeforeClass
    public static void setUp() throws Exception {
        thread = new Thread(() -> {
            try {
                new Server().run();
            } catch (InterruptedException ie) {
                logger.info("Shutting down Server");
            } catch (Exception ex) {
                logger.error("Could not start Server for tests", ex);
            }
        });

        thread.start();

        // make sure server gets off the ground
        Thread.sleep(5000);
    }



    @BenchmarkOptions(benchmarkRounds = DEFAULT_BENCHMARK_ROUNDS, warmupRounds = DEFAULT_WARMUP_ROUNDS, concurrency = BenchmarkOptions.CONCURRENCY_SEQUENTIAL)
    @Test
    public void webSockets() throws Exception {
        tryWebSocket();
    }

    @BenchmarkOptions(benchmarkRounds = DEFAULT_CONCURRENT_BENCHMARK_ROUNDS, warmupRounds = DEFAULT_CONCURRENT_WARMUP_ROUNDS, concurrency = BenchmarkOptions.CONCURRENCY_AVAILABLE_CORES)
    @Test
    public void webSocketsConcurrent() throws Exception {
        tryWebSocket();
    }

    public void tryWebSocket() throws Exception {
        final String url = "ws://localhost:8182/websocket";
        final WebSocketClient client = new WebSocketClient(url);
        client.open();
        final String fatty = IntStream.range(0, 1024).mapToObj(String::valueOf).collect(Collectors.joining());
        client.<String>eval(UUID.randomUUID().toString() + ":" + fatty);
        client.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Thread.sleep(1000);

        thread.interrupt();
        while (thread.isAlive()) {
            Thread.sleep(250);
        }
    }
}
