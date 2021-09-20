package com.ejossev.util;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.asynchttpclient.util.HttpConstants;

import java.util.AbstractMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class HttpAsyncClient {
    static private String BaseURL = "https://api.whatsonchain.com/v1/bsv/main/address/%s/balance";
    static private String BulkURL = "https://api.whatsonchain.com/v1/bsv/main/addresses/balance";

    AsyncHttpClient client;

    public HttpAsyncClient() {
        this.client = asyncHttpClient();
    }

    private Request createAsyncBulkRequest(List<String> addresses) {
        String body = "{ \"addresses\":[";
        addresses.replaceAll(a->"\"" + a + "\"");
        body += String.join(",", addresses);
        body += "]}";
        return new RequestBuilder(HttpConstants.Methods.POST)
                .setUrl(BulkURL)
                .setBody(body)
                .setHeader("Content-Type", "application/json")
                .build();
    }

    private Request createAsyncRequest(String address) {
        String url = String.format(BaseURL, address);
        return new RequestBuilder(HttpConstants.Methods.GET)
                .setUrl(url).build();
    }

    // Optionally, parse the response body and return pair of BigIntegers or floats
    public AddressBalances getOneBalance(String address) throws ExecutionException, InterruptedException {
        Request asyncRequest = createAsyncRequest(address);
        Future<Response> responseFuture = client.executeRequest(asyncRequest);
        Response res = responseFuture.get();
        return new AddressBalances(res.getResponseBody());
    }

    // Return key-value-pairs <address, balances> for the list of given addresses, unsorted
    public AbstractMap<String, AddressBalances> getMultipleBalances(List<String> addresses) {
        AbstractMap<String, AddressBalances> rv = new ConcurrentHashMap<String, AddressBalances>();
        List<CompletableFuture<Response>> futures = new Vector<CompletableFuture<Response>>();
        for (String address : addresses) {
            Request asyncRequest = createAsyncRequest(address);
            CompletableFuture<Response> future = client
                    .executeRequest(asyncRequest)
                    .toCompletableFuture()
                    .whenComplete((response, exception) -> {
                        if (exception != null) {
                            rv.put(address, new AddressBalances());
                        } else {
                            rv.put(address, new AddressBalances(response.getResponseBody()));
                        }
                    });
            futures.add(future);
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return rv;
    }

    public AbstractMap<String, AddressBalances> getBulkBalance(List<String> addresses) throws ExecutionException, InterruptedException {
        Request asyncRequest = createAsyncBulkRequest(addresses);
        Future<Response> responseFuture = client.executeRequest(asyncRequest);
        Response res = responseFuture.get();
        String body = res.getResponseBody();
        return AddressBalances.fromJsonArray(body);
    }
}
