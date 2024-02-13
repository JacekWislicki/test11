package com.example.test11.commons.utils;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {

    private static final String VP_ENV = "VP_ENVIRONMENT";

    public static final String VP_LOCAL_ENV = "local";
    private static final String VP_DOCKER_ENV = "docker";
    private static final String VP_DEV_ENV = "dev";

    private static final String SERVICE_URL_LOCAL = "pulsar://localhost:6650";
    private static final String ADMIN_URL_LOCAL = "http://localhost:8080";
    private static final String ES_URL_LOCAL = "http://localhost:8080";

    private static final String SERVICE_URL_DOCKER = "pulsar://rtk-pulsar:6650";
    private static final String ADMIN_URL_DOCKER = "http://rtk-pulsar:8080";
    private static final String ES_URL_DOCKER = "http://rtk-pulsar:8080";

    private static final String SERVICE_URL_DEV = "pulsar://172.30.2.99:6650";
    private static final String ADMIN_URL_DEV = "http://172.30.2.99:8080";
    private static final String ES_URL_DEV = "http://172.30.2.99:8080";

    public static final String TOPIC_1_NAME = "persistent://public/test11/topic1";
    public static final String TOPIC_1_SUB = "topic1-flink";

    public static String getEnvironment() {
        return System.getenv(VP_ENV);
    }

    public static String getServiceUrl() {
        String environment = getEnvironment();
        if (StringUtils.isBlank(environment)) {
            environment = VP_LOCAL_ENV;
        }
        switch (environment) {
            case VP_DOCKER_ENV:
                return SERVICE_URL_DOCKER;
            case VP_DEV_ENV:
                return SERVICE_URL_DEV;
            default:
                return SERVICE_URL_LOCAL;
        }
    }

    public static String getAdminUrl() {
        String environment = getEnvironment();
        switch (environment) {
            case VP_DOCKER_ENV:
                return ADMIN_URL_DOCKER;
            case VP_DEV_ENV:
                return ADMIN_URL_DEV;
            default:
                return ADMIN_URL_LOCAL;
        }
    }

    public static String getEsUrl() {
        String environment = getEnvironment();
        switch (environment) {
            case VP_DOCKER_ENV:
                return ES_URL_DOCKER;
            case VP_DEV_ENV:
                return ES_URL_DEV;
            default:
                return ES_URL_LOCAL;
        }
    }
}
