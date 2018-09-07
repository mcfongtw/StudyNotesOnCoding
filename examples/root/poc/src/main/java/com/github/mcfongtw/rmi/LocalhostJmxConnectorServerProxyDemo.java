package com.github.mcfongtw.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
            port = p;
        }

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            return ServerSocketFactory.getDefault().createServerSocket(port, 0, InetAddress.getByName("localhost"));
        }
    }

    private static class DefaultRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return SocketFactory.getDefault().createSocket(host, port);
        }
    }

    private static void showUsage() {
        System.err.println("java -jar LocalhostJmxConnectorServerProxyDemo.jar <rmiServerPort> <rmiRegistryPort> (<sleepTimeInSecond>)");
    }

    public static void main(String[] args) throws IOException, JMException, InterruptedException {
        if(args.length < 2) {
            showUsage();
        }

        int rmiServerPort = Integer.valueOf(args[0]);
        int rmiRegistryPort = Integer.valueOf(args[1]);


        String serviceUrl = String.format("service:jmx:rmi://localhost:%1$d/jndi/rmi://localhost:%2$d/jmxrmi", rmiServerPort, rmiRegistryPort);

        logger.info("serviceUrl: {}", serviceUrl);

        LocalhostRMIServerSocketFactory localhostRMIServerSocketFactory = new LocalhostRMIServerSocketFactory(rmiRegistryPort);
        DefaultRMIClientSocketFactory defaultRMIClientSocketFactory = new DefaultRMIClientSocketFactory();

        //create the local RMI registry.
        Registry registry = LocateRegistry.createRegistry(rmiRegistryPort, defaultRMIClientSocketFactory, localhostRMIServerSocketFactory);

        SimpleJmxConnectorServerProxy jmxConnectorServerProxy = new SimpleJmxConnectorServerProxy(serviceUrl, localhostRMIServerSocketFactory, defaultRMIClientSocketFactory);

        jmxConnectorServerProxy.start();

        int sleepTime = DEFAULT_SLEEP_TIME;
        if (args.length == 3) {
            sleepTime = Integer.valueOf(args[2]) * 1000;
        }

        if(sleepTime < 0) {
            //sleep forever
            Thread.currentThread().join();
        } else {
            Thread.sleep(sleepTime);
        }

        jmxConnectorServerProxy.stop();
    }
}
