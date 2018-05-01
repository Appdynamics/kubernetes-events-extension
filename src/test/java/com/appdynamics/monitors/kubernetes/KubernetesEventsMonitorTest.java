package com.appdynamics.monitors.kubernetes;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1EventList;
import io.kubernetes.client.util.Config;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertTrue;

public class KubernetesEventsMonitorTest {

    @Test
    public void testKubernetesEvents(){
        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "src/test/resources/conf/integration-test-config.yml");
        try {
            testKubernetesEventsMonitorRun(taskArgs);
        } catch (TaskExecutionException e) {
            e.printStackTrace();
        }
    }

    private void testKubernetesEventsMonitorRun(Map<String, String> taskArgs) throws TaskExecutionException {
        TaskOutput result = new KubernetesEventsMonitor().execute(taskArgs, null);
        assertTrue(result.getStatusMessage().contains("Finished executing"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testKubernetesEventsMonitor() throws Exception {
        MetricWriteHelper writer = Mockito.mock(MetricWriteHelper.class);
        Runnable runner = Mockito.mock(Runnable.class);
        MonitorConfiguration conf = new MonitorConfiguration("Custom Metrics|Kubernetes", runner, writer);
        conf.setConfigYml("src/test/resources/conf/integration-test-config.yml");
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocationOnMock) {
                Object[] args = invocationOnMock.getArguments();
                System.out.println(args[0] + "=" + args[1]);
                return null;
            }
        }).when(writer).printMetric(Mockito.anyString(), Mockito.any(BigDecimal.class), Mockito.anyString());
        conf.setMetricWriter(writer);
        Map<String, String> config = (Map<String, String>) conf.getConfigYml();

        String apiKey = config.get("eventsApiKey");
        String accountName = config.get("accountName");
        URL publishUrl = Utilities.getUrl(config.get("eventsUrl") + "/events/publish/" + config.get("eventsSchemaName"));
        URL schemaUrl = Utilities.getUrl(config.get("eventsUrl") + "/events/schema/" + config.get("eventsSchemaName"));
        String requestBody = config.get("eventsSchemaDefinition");

        if(EventsRestOperation.doRequest(schemaUrl, accountName, apiKey, "", "GET") == null){
            EventsRestOperation.doRequest(schemaUrl, accountName, apiKey, requestBody, "POST");
        }

        ApiClient client = Config.fromConfig(config.get("kubeClientConfig"));
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        V1EventList eventList = api.listEventForAllNamespaces(null,
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
            System.out.println(payload);
            EventsRestOperation.doRequest(publishUrl, accountName, apiKey, payload, "POST");
        }
    }
}
