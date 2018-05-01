package com.appdynamics.monitors.kubernetes;

import com.appdynamics.monitors.kubernetes.config.Globals;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1EventList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

class Utilities {
    private static final Logger logger = LoggerFactory.getLogger(Utilities.class);

    static URL getUrl(String input){
        URL url = null;
        try {
            url = new URL(input);
        } catch (MalformedURLException e) {
            logger.error("Error forming our from String {}", input, e);
        }
        return url;
    }

    static ArrayNode createEventPayload(V1EventList eventList) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        for (V1Event item : eventList.getItems()) {
            if (item.getLastTimestamp().isAfter(Globals.previousRunTimestamp) || Globals.previousRunTimestamp == null){
                if (!item.getMetadata().getSelfLink().equals(Globals.previousRunSelfLink)){
                    ObjectNode objectNode = mapper.createObjectNode();
                    objectNode = checkAddObject(objectNode, item.getFirstTimestamp(), "firstTimestamp");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getAnnotations(), "annotations");
                    objectNode = checkAddObject(objectNode, item.getLastTimestamp(), "lastTimestamp");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getCreationTimestamp(), "creationTimestamp");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getDeletionTimestamp(), "deletionTimestamp");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getFinalizers(), "finalizers");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getInitializers(), "initializers");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getLabels(), "labels");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getOwnerReferences(), "ownerReferences");
                    objectNode = checkAddObject(objectNode, item.getInvolvedObject().getKind(), "object_kind");
                    objectNode = checkAddObject(objectNode, item.getInvolvedObject().getName(), "object_name");
                    objectNode = checkAddObject(objectNode, item.getInvolvedObject().getNamespace(), "object_namespace");
                    objectNode = checkAddObject(objectNode, item.getInvolvedObject().getResourceVersion(), "object_resourceVersion");
                    objectNode = checkAddObject(objectNode, item.getInvolvedObject().getUid(), "object_uid");
                    objectNode = checkAddObject(objectNode, item.getMessage(), "message");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getClusterName(), "clusterName");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getGenerateName(), "generateName");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getGeneration(), "generation");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getName(), "name");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getNamespace(), "namespace");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getResourceVersion(), "resourceVersion");
                    objectNode = checkAddObject(objectNode, item.getMetadata().getSelfLink(), "selfLink");
                    arrayNode.add(objectNode);
                    Globals.lastElementSelfLink = item.getMetadata().getSelfLink();
                }
                if(item.getLastTimestamp().isAfter(Globals.lastElementTimestamp) || Globals.lastElementTimestamp == null){
                    Globals.lastElementTimestamp = item.getLastTimestamp();
                }
            }
        }
        Globals.previousRunSelfLink = Globals.lastElementSelfLink;
        Globals.previousRunTimestamp = Globals.lastElementTimestamp;

        return arrayNode;
    }

    private static ObjectNode checkAddObject(ObjectNode objectNode, Object object, String fieldName){
        if(object != null){
            objectNode.put(fieldName, object.toString());
        }
        return objectNode;
    }
}
