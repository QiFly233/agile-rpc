package com.qifly.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * 轻量 HTTP 工具：直接返回 HttpResponse<String>
 */
public final class HttpClientUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private HttpClientUtil() {}

    public static HttpResponse<String> get(String url, Map<String,String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeoutOrDefault(timeout, 3))
                .GET();
        addHeaders(b, headers);
        return execute(b.build());
    }

    public static HttpResponse<String> putJson(String url, String body, Map<String,String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeoutOrDefault(timeout, 5))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        addHeaders(b, headers);
        return execute(b.build());
    }

    public static HttpResponse<String> putEmpty(String url, Map<String,String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeoutOrDefault(timeout, 3))
                .PUT(HttpRequest.BodyPublishers.noBody());
        addHeaders(b, headers);
        return execute(b.build());
    }

    public static HttpResponse<String> delete(String url, Map<String,String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeoutOrDefault(timeout, 3))
                .DELETE();
        addHeaders(b, headers);
        return execute(b.build());
    }

    private static Duration timeoutOrDefault(Duration d, int seconds) {
        return d == null ? Duration.ofSeconds(seconds) : d;
    }

    private static void addHeaders(HttpRequest.Builder b, Map<String,String> headers) {
        if (headers != null) {
            headers.forEach(b::header);
        }
    }

    private static HttpResponse<String> execute(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            log.warn("HTTP {} {} code={}", request.method(), request.uri(), resp.statusCode());
        } else {
            log.debug("HTTP {} {} code={}", request.method(), request.uri(), resp.statusCode());
        }
        return resp;
    }
}