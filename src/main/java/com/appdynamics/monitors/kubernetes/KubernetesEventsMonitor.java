package com.appdynamics.monitors.kubernetes;

import com.google.gson.reflect.TypeToken;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1WatchEvent;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class KubernetesEventsMonitor extends AManagedMonitor {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesEventsMonitor.class);

    public KubernetesEventsMonitor() { logger.info(String.format("Using Kubernetes Events Monitor Version [%s]", getImplementationVersion())); }

    @Override
    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
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



        return new TaskOutput("Finished executing Kubernetes Events Monitor Monitor");
    }


    private static String getImplementationVersion() { return KubernetesEventsMonitor.class.getPackage().getImplementationTitle(); }
}
