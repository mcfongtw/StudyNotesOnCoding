package com.github.mcfongtw.rmi;

import javax.annotation.Nullable;
import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.HashMap;
import java.util.Map;

public class SimpleJmxConnectorServerProxy extends SimpleMBeanServerProxy {
    @NotNull
    private Map<String, Object> environment = new HashMap<>();

    @Nullable
    private MBeanServerForwarder forwarder;

    @Nullable
    private ObjectName objectName;

    @NotNull
    private JMXConnectorServer connectorServer;

    @NotNull
    private String serviceUrl;

    public SimpleJmxConnectorServerProxy(String serviceUrl, RMIServerSocketFactory rmiServerSocketFactory) {
        this.serviceUrl = serviceUrl;
        this.environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, rmiServerSocketFactory);
    }

    public SimpleJmxConnectorServerProxy(String serviceUrl, RMIServerSocketFactory rmiServerSocketFactory, RMIClientSocketFactory rmiClientSocketFactory) {
        this(serviceUrl, rmiServerSocketFactory);

        this.environment.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, rmiClientSocketFactory);
    }


    public void start() throws JMException, IOException {
        if (this.mbeanServer == null) {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
        }

        // Create the JMX service URL.
        JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

        // Create the connector mbeanServer now.
        this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, this.environment, this.mbeanServer);

        // Set the given MBeanServerForwarder, if any.
        if (this.forwarder != null) {
            this.connectorServer.setMBeanServerForwarder(this.forwarder);
        }

        // Do we want to register the connector with the MBean mbeanServer?
//        this.objectName = ObjectName.getInstance(this.getClass().getName());
        if(this.objectName != null) {
            this.doRegister(this.connectorServer, this.objectName);
        }


        try {
            this.connectorServer.start();

            if (logger.isInfoEnabled()) {
                logger.info("JMX connector mbeanServer started: " + this.connectorServer);
            }
        } catch (IOException ex) {
            // Unregister the connector mbeanServer if startup failed.
            unregisterBeans();
            throw ex;
        }

    }

    public void stop() throws IOException {
        try {
            if (this.connectorServer != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Stopping JMX connector mbeanServer: " + this.connectorServer);
                }
                this.connectorServer.stop();
            }
        } finally {
            unregisterBeans();
        }
    }

}



