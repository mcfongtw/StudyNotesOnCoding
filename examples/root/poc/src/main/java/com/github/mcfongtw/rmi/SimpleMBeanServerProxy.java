package com.github.mcfongtw.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class SimpleMBeanServerProxy {

    /**
     * {@code Log} instance for this class.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The {@code MBeanServer} instance being used to register beans.
     */
    protected MBeanServer mbeanServer;

    /**
     * The beans that have been registered by this exporter.
     */
    private final Set<ObjectName> registeredObjectNames = new LinkedHashSet<>();


    /**
     * Specify the {@code MBeanServer} instance with which all beans should
     * be registered. The {@code MBeanExporter} will attempt to locate an
     * existing {@code MBeanServer} if none is supplied.
     */
    public void setMbeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    /**
     * Return the {@code MBeanServer} that the beans will be registered with.
     */
    public final MBeanServer getMbeanServer() {
        return this.mbeanServer;
    }



    protected void doRegister(Object mbean, ObjectName objectName) throws JMException {
        ObjectName actualObjectName;

        synchronized (this.registeredObjectNames) {
            ObjectInstance registeredBean = null;
            try {
                registeredBean = this.mbeanServer.registerMBean(mbean, objectName);
            }
            catch (InstanceAlreadyExistsException ex) {
                    throw ex;
            }

            // Track registration and notify listeners.
            actualObjectName = (registeredBean != null ? registeredBean.getObjectName() : null);
            if (actualObjectName == null) {
                actualObjectName = objectName;
            }
            this.registeredObjectNames.add(actualObjectName);
        }

        onRegister(actualObjectName, mbean);
    }

    /**
     * Unregisters all beans that have been registered by an instance of this class.
     */
    protected void unregisterBeans() {
        Set<ObjectName> snapshot;
        synchronized (this.registeredObjectNames) {
            snapshot = new LinkedHashSet<>(this.registeredObjectNames);
        }
        if (!snapshot.isEmpty()) {
            logger.debug("Unregistering JMX-exposed beans");
            for (ObjectName objectName : snapshot) {
                doUnregister(objectName);
            }
        }
    }

    /**
     * Actually unregister the specified MBean from the mbeanServer.
     * @param objectName the suggested ObjectName for the MBean
     */
    protected void doUnregister(ObjectName objectName) {
        boolean actuallyUnregistered = false;

        synchronized (this.registeredObjectNames) {
            if (this.registeredObjectNames.remove(objectName)) {
                try {
                    // MBean might already have been unregistered by an external process
                    if (this.mbeanServer.isRegistered(objectName)) {
                        this.mbeanServer.unregisterMBean(objectName);
                        actuallyUnregistered = true;
                    }
                    else {
                        if (logger.isInfoEnabled()) {
                            logger.info("Could not unregister MBean [" + objectName + "] as said MBean " +
                                    "is not registered (perhaps already unregistered by an external process)");
                        }
                    }
                }
                catch (JMException ex) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Could not unregister MBean [" + objectName + "]", ex);
                    }
                }
            }
        }

        if (actuallyUnregistered) {
            onUnregister(objectName);
        }
    }

    /**
     * Return the {@link ObjectName ObjectNames} of all registered beans.
     */
    protected final ObjectName[] getRegisteredObjectNames() {
        synchronized (this.registeredObjectNames) {
            return this.registeredObjectNames.toArray(new ObjectName[0]);
        }
    }


    /**
     * Called when an MBean is registered under the given {@link ObjectName}. Allows
     * subclasses to perform additional processing when an MBean is registered.
     * <p>The default implementation delegates to {@link #onRegister(ObjectName)}.
     * @param objectName the actual {@link ObjectName} that the MBean was registered with
     * @param mbean the registered MBean instance
     */
    protected void onRegister(ObjectName objectName, Object mbean) {
        onRegister(objectName);
    }

    /**
     * Called when an MBean is registered under the given {@link ObjectName}. Allows
     * subclasses to perform additional processing when an MBean is registered.
     * <p>The default implementation is empty. Can be overridden in subclasses.
     * @param objectName the actual {@link ObjectName} that the MBean was registered with
     */
    protected void onRegister(ObjectName objectName) {
    }

    /**
     * Called when an MBean is unregistered under the given {@link ObjectName}. Allows
     * subclasses to perform additional processing when an MBean is unregistered.
     * <p>The default implementation is empty. Can be overridden in subclasses.
     * @param objectName the {@link ObjectName} that the MBean was registered with
     */
    protected void onUnregister(ObjectName objectName) {
    }

}