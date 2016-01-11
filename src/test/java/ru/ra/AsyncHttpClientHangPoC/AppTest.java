package ru.ra.AsyncHttpClientHangPoC;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ListenableFutureAdapter;

public class AppTest {
    
    private static AsyncHttpClient createClient() {
        final AsyncHttpClientConfig.Builder configBuilder =
            new AsyncHttpClientConfig.Builder();
        configBuilder.setFollowRedirect(true);
        configBuilder.setAllowPoolingConnections(true);
        configBuilder.setAllowPoolingSslConnections(true);
        configBuilder.setCompressionEnforced(true);
        configBuilder.setIOThreadMultiplier(1);
        configBuilder.setConnectTimeout(10000);
        configBuilder.setRequestTimeout(10000);
        return new AsyncHttpClient(configBuilder.build());
    }

    @Test
    public void notRedirecting() throws MalformedURLException,
            InterruptedException, ExecutionException, TimeoutException {
        AsyncHttpClient httpClient = createClient();
        try {
            String addressThatWillNotRedirect =
                "http://download.eclipse.org/technology/epp/downloads/release/mars/1/eclipse-jee-mars-1-win32-x86_64.zip";
            ListenableFuture<Response> head =
                reqHead(httpClient, new URL(addressThatWillNotRedirect));
            head.get(10, TimeUnit.SECONDS);
        } finally {
            httpClient.close();
        }
    }

    @Test
    public void redirecting() throws InterruptedException, ExecutionException,
            TimeoutException, MalformedURLException {
        AsyncHttpClient httpClient = createClient();
        try {
            String addressThatWillRedirect =
                "http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/mars/1/eclipse-jee-mars-1-win32-x86_64.zip";
            ListenableFuture<Response> head =
                reqHead(httpClient, new URL(addressThatWillRedirect));
            head.get(10, TimeUnit.SECONDS);
        } finally {
            httpClient.close();
        }
    }

    public static ListenableFuture<Response> reqHead(AsyncHttpClient httpClient, final URL url) {
        final long started = System.currentTimeMillis();
        com.ning.http.client.ListenableFuture<Response> head =
            httpClient.prepareHead(url.toExternalForm()).execute(
                new AsyncCompletionHandler<Response>() {
                    @Override
                    public STATE onHeadersReceived(HttpResponseHeaders headers)
                            throws Exception {
                        System.out.println("headers received@"
                            + (System.currentTimeMillis() - started) + " "
                            + headers.getHeaders());
                        return super.onHeadersReceived(headers);
                    }

                    @Override
                    public STATE onStatusReceived(HttpResponseStatus status)
                            throws Exception {
                        System.out.println("status received@"
                            + (System.currentTimeMillis() - started) + " "
                            + status.getStatusCode());
                        return super.onStatusReceived(status);
                    }

                    @Override
                    public Response onCompleted(final Response paramResponse)
                            throws Exception {
                        System.out.println("complete@"
                            + (System.currentTimeMillis() - started));
                        return paramResponse;
                    }

                    @Override
                    public void onThrowable(final Throwable t) {
                        System.out.println("throwable " + t);
                    }
                });
        ListenableFuture<Response> headFuture =
            ListenableFutureAdapter.asGuavaFuture(head);
        return headFuture;
    }
}
