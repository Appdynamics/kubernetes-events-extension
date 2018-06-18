# AppDynamics Kubernetes Events Monitor Extension

This extension works only with the standalone machine agent.

## Use Case

Monitor Metrics provided by the Kubernetes Events API and let them report into the AppDynamics Analytics Events.

## Prerequisites

 * This extension requires the Java Machine Agent
 * The AppDynamics platform needs the Events Service set up
 * You will need one or more Transaction Analytics/APM Peak licenses to consume the data

## Installation

Either [Download the Extension from the latest Github release](https://github.com/Appdynamics/kubernetes-events-extension/files/2093924/KubernetesEventsMonitor-0.2.zip) or Build from Source.

1. Deploy the `KuberentesEventsMonitor-<VERSION>.zip` file into the `<machine agent home>/monitors` directory.

  `> unzip KuberentesEventsMonitor-<VERSION>.zip -d <machine agent home>/monitors/`

2. Set up `config.yml`. At minimum this is:
  ```
    # Path to your kubectl Client configuration
    kubeClientConfig: "$HOME/.kube/config"

    # Events API Key obtained from AppDynamics --> Analytics --> Configuration API Keys --> Add
    # The API Key you create needs to be able to Manage and Publish Custom Analytics Events
    eventsApiKey: ""

    # Global Account Name obtained from
    # AppDynamics --> Settings --> License --> Accounts --> Global Account Name
    accountName: ""
  ```

3. Restart the Machine Agent.

## Build from Source

1. Clone this repository
2. Run `mvn -DskipTests clean install`
3. The `KuberentesEventsMonitor-<VERSION>.zip` file can be found in the `target` directory

## Directory Structure

<table><tbody>
<tr>
<th align = 'left'> Directory/File </th>
<th align = 'left'> Description </th>
</tr>
<tr>
<td class='confluenceTd'> src/main/resources/config </td>
<td class='confluenceTd'> Contains monitor.xml and config.yml</td>
</tr>
<tr>
<td class='confluenceTd'> src/main/java </td>
<td class='confluenceTd'> Contains source code for the Kubernetes Events monitoring extension </td>
</tr>
<tr>
<td class='confluenceTd'> src/test/java </td>
<td class='confluenceTd'> Contains test code for the Kubernetes Events monitoring extension </td>
</tr>
<tr>
<td class='confluenceTd'> target </td>
<td class='confluenceTd'> Only obtained when using maven. Run 'maven clean install' to get the distributable .zip file. </td>
</tr>
<tr>
<td class='confluenceTd'> pom.xml </td>
<td class='confluenceTd'> maven build script to package the project (required only if changing Java code) </td>
</tr>
</tbody>
</table>

## Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/michaelenglert/kubernetes-events-extension).

## Troubleshooting

1. Verify Machine Agent Data: Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. config.yml: Validate the file [here](http://www.yamllint.com/)
3. Check Logs: There could be some obvious errors in the machine agent logs. Please take a look.
4. `The config cannot be null` error.
   This usually happenes when on a windows machine in monitor.xml you give config.yaml file path with linux file path separator */*. Use Windows file path separator *\* e.g. *monitors\Monitor\config.yaml*. For Windows, please specify
   the complete path
5. Collect Debug Logs: Edit the file, *<MachineAgent>/conf/logging/log4j.xml* and update the level of the appender *com.appdynamics* and *com.singularity* to debug. Let it run for 5-10 minutes and attach the logs to a support ticket.

## Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).
