package com.appdynamics.monitors.kubernetes;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1WatchEvent;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import org.junit.Test;

import java.io.IOException;

public class KubernetesEventsMonitorTest {
    @Test
    public void testKubernetesEventsMonitor(){
        ApiClient client = null;
        try {
            client = Config.defaultClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        try {
            Watch<V1.Event> watchEvent = Watch.createWatch(client, api.listEventForAllNamespacesCall(null,
                    null,
                    false,
                    null,
                    10,
                    null,
                    null,
                    20,
                    true,
                    null,
                    null), new TypeToken<Watch.Response<V1WatchEvent>>() {}.getType());
            for (Watch.Response<V1.Event> item : watchEvent) {
                System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
