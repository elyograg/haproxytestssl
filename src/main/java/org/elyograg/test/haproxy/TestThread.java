package org.elyograg.test.haproxy;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestThread extends Thread implements Runnable {
  /**
   * A logger object. Gets the fully qualified class name so this can be used
   * as-is for any class.
   */
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final AtomicInteger threadCounter = new AtomicInteger();
  private final CloseableHttpClient httpClient;
  private final String url;

  /**
   * Constructor.
   * 
   * @param testUrl The URL to test.
   * @param hc      the http client to use.
   */
  public TestThread(final String testUrl, final CloseableHttpClient hc) {
    httpClient = hc;
    url = testUrl;
  }

  @Override
  public void run() {
    this.setName(String.format("test_thread_%s", threadCounter.incrementAndGet()));
    for (int i = 0; i < 1000; i++) {
      final long startNanos = System.nanoTime();
      try {
        doGet();
      } catch (final Exception e) {
        log.error("Problem making request!", e);
        throw new RuntimeException("Problem making request!", e);
      }
      StaticStuff.requestTimes.add((double) TimeUnit.MILLISECONDS
          .convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS));
    }
    log.warn("Thread ending, {} left", threadCounter.decrementAndGet());
  }

  private String doGet() throws ClientProtocolException, IOException, URISyntaxException {
    final URI uri = new URIBuilder(url).build();

    final HttpGet httpGet = new HttpGet(uri);
    httpGet.addHeader("Connection", "close");
    // httpMethod.setRequestHeader("Connection", "close")

    final HttpResponse response = httpClient.execute(httpGet);
    final HttpEntity entity = response.getEntity();
    final String responseBody;
// Check if the response was successful (status code 200)
    if (response.getStatusLine().getStatusCode() == 200) {
      responseBody = EntityUtils.toString(entity);
    } else {
      final String msg = String.format("Request failed with status code: %s",
          response.getStatusLine().getStatusCode());
      throw new RuntimeException(msg);
    }

    EntityUtils.consume(entity); // Ensure the entity content is fully consumed
    final String output = String.format("Accessed count %s   ",
        StaticStuff.requestCounter.incrementAndGet());
    StaticStuff.animate(output, 10);
    return responseBody;
  }
}
