package com.qifly.core.discovery.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qifly.core.service.Provider;
import com.qifly.core.utils.HttpClientUtil;
import com.qifly.core.utils.IpUtil;
import com.qifly.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * consul注册中心
 */
public class ConsulRegistry implements Registry {

    Logger logger = LoggerFactory.getLogger(ConsulRegistry.class);

    /**
     * consul地址
     */
    private final String baseUrl;
    private final String serviceRegisterApi = "/v1/agent/service/register";
    private final String serviceDeregisterApi = "/agent/service/deregister/";
    private final String healthServiceApi = "/v1/health/service/";

    public ConsulRegistry(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void register(Provider provider) throws RegistryException {
        String localIp = IpUtil.getLocalIp();
        int port = provider.getPort();
        String serviceId = getServiceId(provider);
        String url = baseUrl + serviceRegisterApi;
        String body = """
                {
                  "Name":"%s",
                  "ID":"%s",
                  "Address":"%s",
                  "Port":%d,
                  "Check":{
                    "TCP":"%s:%d",
                    "Interval":"10s",
                    "Timeout":"2s",
                    "DeregisterCriticalServiceAfter":"1m"
                  }
                }
                """.formatted(provider.getServiceName(), serviceId, localIp, port, localIp, port);
        try {
            HttpResponse<String> resp = HttpClientUtil.putJson(
                    url,
                    body,
                    null,
                    Duration.ofSeconds(5));
            if (resp.statusCode() == 200) {
                logger.info("{} register consul GET {} success", serviceId, url);
            } else {
                logger.error("{} register consul GET {} fail, statusCode:{}", serviceId, url, resp.statusCode());
                throw new RegistryException("service register consul fail");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("{} register consul GET {} error", serviceId, url, e);
            throw new RegistryException("service register consul error", e);
        }
    }

    @Override
    public void deregister(Provider provider) throws RegistryException {
        String serviceId = getServiceId(provider);
        String url = baseUrl + serviceDeregisterApi + serviceId;
        try {
            HttpResponse<String> resp = HttpClientUtil.putEmpty(
                    url,
                    null,
                    Duration.ofSeconds(3));
            if (resp.statusCode() == 200) {
                logger.info("{} deregister consul PUT {} success", serviceId, url);
            } else {
                logger.error("{} deregister consul PUT {} fail, statusCode:{}", serviceId, url, resp.statusCode());
                throw new RegistryException("service deregister consul fail");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("{} deregister consul PUT {} error", serviceId, url, e);
            throw new RegistryException("service deregister consul error", e);
        }
    }

    @Override
    public List<String> discover(String serviceName) throws RegistryException {
        String url = baseUrl + healthServiceApi + serviceName + "?passing=true";
        try {
            HttpResponse<String> resp = HttpClientUtil.get(url, null, Duration.ofSeconds(3));
            if (resp.statusCode() != 200) {
                logger.error("{} discover consul GET {} fail, statusCode:{}", serviceName, url, resp.statusCode());
                throw new RegistryException("service discover consul fail");
            }
            return innerHealthService(resp);
        } catch (IOException | InterruptedException e) {
            logger.warn("{} discover consul GET {} error", serviceName, url, e);
            throw new RegistryException("consul discover service error", e);
        }
    }

    @Override
    public void subscribe(String serviceName, RegistryListener listener) {
        long lastIndex = 1L;
        while (true) {
            String url = baseUrl + healthServiceApi + serviceName + "?passing=true&wait=60s&index=" + lastIndex;
            try {
                HttpResponse<String> resp = HttpClientUtil.get(url, null, Duration.ofSeconds(63));
                if (resp.statusCode() != 200) {
                    logger.error("{} subscribe consul GET {} fail, statusCode:{}", serviceName, url, resp.statusCode());
                }
                String idxStr = resp.headers().firstValue("X-Consul-Index").orElse(null);
                long idx = (idxStr == null || idxStr.isBlank()) ? 0L : Long.parseLong(idxStr);
                if (idx != 0 && idx != lastIndex) {
                    lastIndex = idx;
                    List<String> endpoints = innerHealthService(resp);
                    listener.onChange(endpoints);
                }
            } catch (IOException | InterruptedException e) {
                logger.error("{} subscribe consul GET {} error", serviceName, url, e);
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {

                }
            }
        }
    }

    private List<String> innerHealthService(HttpResponse<String> resp) {
        List<HealthServiceResponse> healthServiceResponses = JsonUtil.fromJson(resp.body(), new TypeReference<>() {
        });
        Set<String> endpoints = new HashSet<>();
        for (HealthServiceResponse healthServiceResponse : healthServiceResponses) {
            if (healthServiceResponse == null || healthServiceResponse.service == null) {
                continue;
            }
            HealthServiceResponse.Service service = healthServiceResponse.service;
            String host = service.address;
            if (host == null || host.isEmpty()) {
                continue;
            }
            int port = service.port;
            if (port <= 0) {
                continue;
            }
            endpoints.add(host + ":" + port);
        }
        return endpoints.isEmpty() ? List.of() : List.copyOf(endpoints);
    }

    private String getServiceId(Provider provider) {
        return provider.getServiceName() + "-" + IpUtil.getLocalIp() + "-" + provider.getPort();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HealthServiceResponse {

        @JsonProperty("Node")
        public Node node;

        @JsonProperty("Service")
        public Service service;

        @JsonProperty("Checks")
        public List<Check> checks;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Node {
            @JsonProperty("Node")
            public String nodeName;
            @JsonProperty("Address")
            public String address;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Service {
            @JsonProperty("ID")
            public String id;
            @JsonProperty("Service")
            public String name;
            @JsonProperty("Address")
            public String address;
            @JsonProperty("Port")
            public int port;
            @JsonProperty("Tags")
            public List<String> tags;
            @JsonProperty("Meta")
            public Object meta;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Check {
            @JsonProperty("CheckID")
            public String checkId;
            @JsonProperty("Status")
            public String status; // passing / warning / critical
            @JsonProperty("ServiceID")
            public String serviceId;
            @JsonProperty("ServiceName")
            public String serviceName;
        }
    }
}
