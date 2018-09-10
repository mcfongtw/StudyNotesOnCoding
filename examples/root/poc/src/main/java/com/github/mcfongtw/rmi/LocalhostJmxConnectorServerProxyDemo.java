package com.github.mcfongtw.rmi;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.Nullable;
import javax.management.JMException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public class LocalhostJmxConnectorServerProxyDemo {

    private static final Logger logger = LoggerFactory.getLogger(LocalhostJmxConnectorServerProxyDemo.class);

    //5 minute
    private static final int DEFAULT_SLEEP_TIME = 5 * 60 * 1000;

    private static class LocalhostRMIServerSocketFactory implements RMIServerSocketFactory {

        private int port;

        public LocalhostRMIServerSocketFactory(int p) {
            super();
            port = p;
        }

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            InetAddress host = InetAddress.getByName("localhost");
            logger.info("Server Socket listening at [{}:{}]", host, port);
            return ServerSocketFactory.getDefault().createServerSocket(port, 0, host);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(this.port).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            LocalhostRMIServerSocketFactory that = (LocalhostRMIServerSocketFactory) obj;
            return new EqualsBuilder().append(this.port, that.port).isEquals();
        }
    }

    private static class DefaultRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            logger.info("Socket established at [{}:{}]", host, port);
            return SocketFactory.getDefault().createSocket(host, port);
        }
    }

    private static void showUsage() {
        System.err.println("java -jar LocalhostJmxConnectorServerProxyDemo.jar <rmiServerPort> <rmiRegistryPort> (<sleepTimeInSecond>)");
    }

    public static void main(String[] args) throws IOException, JMException, InterruptedException {
        if (args.length < 2) {
            showUsage();
            return;
        }

        // add SLF4JBridgeHandler to j.u.l's root logger
        SLF4JBridgeHandler.install();

        int rmiServerPort = Integer.valueOf(args[0]);
        int rmiRegistryPort = Integer.valueOf(args[1]);


        String serviceUrl = String.format("service:jmx:rmi://localhost:%1$d/jndi/rmi://localhost:%2$d/jmxrmi", rmiServerPort, rmiRegistryPort);

        logger.info("serviceUrl: {}", serviceUrl);

        LocalhostRMIServerSocketFactory localhostRMIServerSocketFactory = new LocalhostRMIServerSocketFactory(rmiRegistryPort);
        DefaultRMIClientSocketFactory defaultRMIClientSocketFactory = new DefaultRMIClientSocketFactory();

        //Locate or create the RMI registry.
//        LocateRegistry.createRegistry(rmiRegistryPort, defaultRMIClientSocketFactory, localhostRMIServerSocketFactory);
        LocateRegistry.createRegistry(rmiRegistryPort);
        Registry registry = LocateRegistry.getRegistry("localhost", rmiRegistryPort, defaultRMIClientSocketFactory);
        for (String name : registry.list()) {
            logger.info("[{}] registered to rmi registry. ", name);
        }

        SimpleJmxConnectorServerProxy jmxConnectorServerProxy = new SimpleJmxConnectorServerProxy(serviceUrl, localhostRMIServerSocketFactory, defaultRMIClientSocketFactory);

        jmxConnectorServerProxy.start();

        int sleepTimeInMillis = DEFAULT_SLEEP_TIME;
        if (args.length == 3) {
            sleepTimeInMillis = Integer.valueOf(args[2]) * 1000;
        }

        if (sleepTimeInMillis < 0) {
            //sleep forever
            Thread.currentThread().join();
        } else {
            Thread.sleep(sleepTimeInMillis);
        }

        jmxConnectorServerProxy.stop();
    }

}