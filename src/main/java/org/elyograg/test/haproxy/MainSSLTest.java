package org.elyograg.test.haproxy;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Command(name = "haproxytestssl", sortOptions = false, scope = ScopeType.INHERIT, description = ""
    + "A program to pound haproxy with SSL requests", footer = StaticStuff.USAGE_OPTION_SEPARATOR_TEXT)
public final class MainSSLTest implements Runnable {
  /**
   * A logger object. Gets the fully qualified class name so this can be used
   * as-is for any class.
   */
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** Help option. */
  @Option(names = { "-h", "--help",
      "--usage" }, arity = "0", usageHelp = true, scope = ScopeType.INHERIT, description = ""
          + "Display this command usage.")
  private static boolean help;

  /** Verbose option. */
  @Option(names = { "-v", "--verbose",
      "--debug" }, arity = "0", scope = ScopeType.INHERIT, description = ""
          + "Log any available debug messages.")
  private static boolean verbose;

  /**
   * An argument group in which one of the options is required.
   */
  @ArgGroup(multiplicity = "1")
  private static RequiredOpts requiredOpts;

  private static final class RequiredOpts {
    @Option(names = { "-u", "--url" }, arity = "0", scope = ScopeType.INHERIT, description = ""
        + "The URL to test.")
    private static String url;

    /** A hidden --exit option used by the shell script. */
    @Option(names = {
        "--exit" }, arity = "0", hidden = true, scope = ScopeType.INHERIT, description = ""
            + "Exit the program as soon as it starts.")
    private static boolean exitFlag;
  }

  /** Optional option. */
  @Option(names = { "-t",
      "--thread-count" }, arity = "1", defaultValue = "8", scope = ScopeType.INHERIT, description = ""
          + "The number of threads to run. Default '${DEFAULT-VALUE}'")
  private static int threadCount;

  public static final void main(final String[] args) {
    final CommandLine cmd = new CommandLine(new MainSSLTest());
    cmd.setHelpFactory(StaticStuff.createLeftAlignedUsageHelp());
    final int exitCode = cmd.execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    log.info("Program starting");

    if (RequiredOpts.exitFlag) {
      StaticStuff.exitProgram();
    }

    final CloseableHttpClient client = HttpClients.custom().build();

    final long startNanos = System.nanoTime();
    final Set<Thread> threads = Collections.synchronizedSet(new HashSet<>());
    for (int i = 0; i < threadCount; i++) {
      final Thread t = new TestThread(RequiredOpts.url, client);
      t.start();
      threads.add(t);
    }

    boolean notDone;
    do {
      notDone = false;
      for (final Thread t : threads) {
        if (t.isAlive()) {
          notDone = true;
        }
      }
      StaticStuff.sleep(100, TimeUnit.MILLISECONDS);
    } while (notDone);
    final long elapsedNanos = System.nanoTime() - startNanos;
    final long elapsedMillis = TimeUnit.MILLISECONDS.convert(elapsedNanos, TimeUnit.NANOSECONDS);
    final double perSecond = StaticStuff.requestTimes.size() / ((double) elapsedMillis / 1000);
    System.out.println("");

    final Percentile p = new Percentile();
    final double[] doubleArray = new double[StaticStuff.requestTimes.size()];
    int index = 0;
    for (final Double value : StaticStuff.requestTimes) {
      doubleArray[index++] = value;
    }
    Arrays.sort(doubleArray);

    p.setQuantile(10);
    final double p10 = p.evaluate(doubleArray);
    p.setQuantile(25);
    final double p25 = p.evaluate(doubleArray);
    p.setQuantile(50);
    final double p50 = p.evaluate(doubleArray);
    p.setQuantile(75);
    final double p75 = p.evaluate(doubleArray);
    p.setQuantile(95);
    final double p95 = p.evaluate(doubleArray);
    p.setQuantile(99);
    final double p99 = p.evaluate(doubleArray);
    p.setQuantile(99.9);
    final double p999 = p.evaluate(doubleArray);
    log.info("Count {} {}/s", StaticStuff.requestTimes.size(), String.format("%.2f", perSecond));
    log.info("10th % {} ms", String.format("%.0f", p10));
    log.info("25th % {} ms", String.format("%.0f", p25));
    log.info("Median {} ms", String.format("%.0f", p50));
    log.info("75th % {} ms", String.format("%.0f", p75));
    log.info("95th % {} ms", String.format("%.0f", p95));
    log.info("99th % {} ms", String.format("%.0f", p99));
    log.info("99.9 % {} ms", String.format("%.0f", p999));

    try {
      client.close();
    } catch (final Exception e) {
      throw new RuntimeException("Error stopping http client.", e);
    }
  }
}
