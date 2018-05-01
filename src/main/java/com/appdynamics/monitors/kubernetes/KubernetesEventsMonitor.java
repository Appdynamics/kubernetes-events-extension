package com.appdynamics.monitors.kubernetes;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1EventList;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class KubernetesEventsMonitor extends AManagedMonitor {
    private static final Logger logger = LoggerFactory.getLogger(KubernetesEventsMonitor.class);
    private MonitorConfiguration configuration;

    public KubernetesEventsMonitor() { logger.info(String.format("Using Kubernetes Events Monitor Version [%s]", getImplementationVersion())); }

    private void initialize(Map<String, String> argsMap) {
        MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(this);
        MonitorConfiguration conf = new MonitorConfiguration("Custom Metrics|K8S",
                new TaskRunnable(), metricWriteHelper);
        final String configFilePath = argsMap.get("config-file");
        conf.setConfigYml(configFilePath);
        conf.setMetricWriter(MetricWriteHelperFactory.create(this));
        conf.checkIfInitialized(MonitorConfiguration.ConfItem.CONFIG_YML,
                MonitorConfiguration.ConfItem.EXECUTOR_SERVICE,
                MonitorConfiguration.ConfItem.METRIC_PREFIX,
                MonitorConfiguration.ConfItem.METRIC_WRITE_HELPER);
        this.configuration = conf;
    }

    @Override
    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        try{
            if(map != null){
                if (logger.isDebugEnabled()) {logger.debug("The raw arguments are {}", map);}
                initialize(map);
                configuration.executeTask();
                return new TaskOutput("Finished executing Kubernetes Events Monitor");
            }
        }
        catch(Exception e) {
            logger.error("Failed to execute the Kubernetes Events Monitor task", e);
        }
        throw new TaskExecutionException("Kubernetes Events Monitor task completed with failures.");
    }

    private class TaskRunnable implements Runnable {
        @SuppressWarnings("unchecked")
        public void run() {
            Map<String, String> config = (Map<String, String>) configuration.getConfigYml();
            if (config != null) {
                String apiKey = config.get("eventsApiKey");
                String accountName = config.get("accountName");
                URL publishUrl = Utilities.getUrl(config.get("eventsUrl") + "/events/publish/" + config.get("eventsSchemaName"));
                URL schemaUrl = Utilities.getUrl(config.get("eventsUrl") + "/events/schema/" + config.get("eventsSchemaName"));
                String requestBody = config.get("eventsSchemaDefinition");

                if(EventsRestOperation.doRequest(schemaUrl, accountName, apiKey, "", "GET") == null){
                    logger.info("Schema Url {} does not exists", schemaUrl);
                    EventsRestOperation.doRequest(schemaUrl, accountName, apiKey, requestBody, "POST");
                    logger.info("Schema Url {} created", schemaUrl);
                }
                else {
                    logger.info("Schema Url {} exists", schemaUrl);
                }

                ApiClient client;
                try {
                    client = Config.fromConfig(config.get("kubeClientConfig"));
                    Configuration.setDefaultApiClient(client);
                    CoreV1Api api = new CoreV1Api();

                    V1EventList eventList;
                    eventList = api.listEventForAllNamespaces(null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);
                    String payload = Utilities.createEventPayload(eventList).toString();
                    if(!payload.equals("[]")){
                        EventsRestOperation.doRequest(publishUrl, accountName, apiKey, payload, "POST");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getImplementationVersion() { return KubernetesEventsMonitor.class.getPackage().getImplementationTitle(); }
}
