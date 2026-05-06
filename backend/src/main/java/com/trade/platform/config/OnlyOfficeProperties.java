package com.trade.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "onlyoffice")
public class OnlyOfficeProperties {

    private String documentServer = "http://localhost:8080";
    private String callbackHost = "http://host.docker.internal:8081";
    private String jwtSecret = "";

    public String getDocumentServer() { return documentServer; }
    public void setDocumentServer(String documentServer) { this.documentServer = documentServer; }
    public String getCallbackHost() { return callbackHost; }
    public void setCallbackHost(String callbackHost) { this.callbackHost = callbackHost; }
    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
}
