package com.appdynamics.monitors.kubernetes.config;

import org.joda.time.DateTime;

public class Globals {
    public static DateTime lastElementTimestamp = null;
    public static DateTime previousRunTimestamp = null;
    public static String lastElementSelfLink = "";
    public static String previousRunSelfLink = "";
}
