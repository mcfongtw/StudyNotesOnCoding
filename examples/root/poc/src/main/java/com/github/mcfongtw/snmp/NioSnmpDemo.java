package com.github.mcfongtw.snmp;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

class SnmpUtils {


    public enum SnmpTransportType {
        TCP,
        UDP
    }


    static TransportMapping<?> getTransport(SnmpUtils.SnmpTransportType type, String host, int port) throws IOException {
        TransportMapping transport = null;
        if(type == SnmpUtils.SnmpTransportType.TCP) {
            transport = new DefaultTcpTransportMapping((TcpAddress) getAddress(type, host, port));
        } else {
            transport = new DefaultUdpTransportMapping((UdpAddress) getAddress(type, host, port));
        }

        return transport;
    }

    static Address getAddress(SnmpUtils.SnmpTransportType type, String host, int port) throws UnknownHostException {
        Address address = null;
        if(type == SnmpUtils.SnmpTransportType.TCP) {
            address = new TcpAddress(InetAddress.getByName(host), port);
        } else if(type == SnmpUtils.SnmpTransportType.UDP) {
            address = new UdpAddress(InetAddress.getByName(host), port);
        } else {
            throw new IllegalStateException("Unknown Transport Type [" + type.getClass().getSimpleName() + "]");
        }

        return address;
    }
}

class NetworkEndpointManager {

    private Snmp snmp = null;
    private String serverTransportString = "";
    private List<Address> listOfEpAddress = Lists.newArrayList();

    /**
     * Constructor
     */
    public NetworkEndpointManager() throws IOException {
    }

    public String getServerTransport() {
        return serverTransportString;
    }



    public void addEndPointAddress(SnmpUtils.SnmpTransportType type, String host, int port) {
        try {
            Address address = SnmpUtils.getAddress(type, host, port);
            listOfEpAddress.add(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws IOException
     */
    public void start(SnmpUtils.SnmpTransportType type, String host, int port) throws IOException {
        TransportMapping serverTransport = SnmpUtils.getTransport(type, host, port);
        serverTransportString = type + "://" + host + ":" + port;
        snmp = new Snmp(serverTransport);
// Do not forget this line!
        try {
            serverTransport.listen();
        } catch (BindException e) {
            throw e;
        } finally {
            System.out.println(serverTransportString);
        }
    }

    /**
     * Method which takes a single OID and returns the response from the agent as a String.
     * @param oid
     * @return
     * @throws IOException
     */
    public String getAsString(OID oid) throws IOException {
        StringBuilder builder = new StringBuilder();
        for(Address epAddress: listOfEpAddress) {
            ResponseEvent event = get(new OID[]{oid}, epAddress);
            builder.append(event.getResponse().get(0).getVariable().toString() + "\t");
        }

        return builder.toString();
    }

    /**
     * This method is capable of handling multiple OIDs
     * @param oids
     * @return
     * @throws IOException
     */
    public ResponseEvent get(OID oids[], Address epAddress) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, getTarget(epAddress), null);
        if(event != null) {
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
     * This method returns a Target, which contains information about
     * where the data should be fetched and how.
     * @return
     */
    private Target getTarget(Address targetAddress) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

}

class NetworkManagementEndpoint extends BaseAgent {
    private TransportMapping transport;

    public NetworkManagementEndpoint()  {

        /**
         * Creates a base listOfEps with boot-counter, config file, and a
         * CommandProcessor for processing SNMP requests. Parameters:
         * "bootCounterFile" - a file with serialized boot-counter information
         * (read/write). If the file does not exist it is created on shutdown of
         * the listOfEps. "configFile" - a file with serialized configuration
         * information (read/write). If the file does not exist it is created on
         * shutdown of the listOfEps. "commandProcessor" - the CommandProcessor
         * instance that handles the SNMP requests.
         */
        super(new File("/tmp/conf.listOfEps"), new File("/tmp/bootCounter.listOfEps"),
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())));
    }

    /**
     * Adds community to security name mappings needed for SNMPv1 and SNMPv2c.
     */
    @Override
    protected void addCommunities(SnmpCommunityMIB communityMIB) {
        Variable[] com2sec = new Variable[] { new OctetString("public"),
                new OctetString("cpublic"), // security name
                getAgent().getContextEngineID(), // local engine ID
                new OctetString("public"), // default context name
                new OctetString(), // transport tag
                new Integer32(StorageType.nonVolatile), // storage type
                new Integer32(RowStatus.active) // row status
        };
        SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
                new OctetString("public2public").toSubIndex(true), com2sec);
        communityMIB.getSnmpCommunityEntry().addRow(row);

    }

    /**
     * Adds initial notification targets and filters.
     */
    @Override
    protected void addNotificationTargets(SnmpTargetMIB arg0,
                                          SnmpNotificationMIB arg1) {

    }

    /**
     * Adds all the necessary initial users to the USM.
     */
    @Override
    protected void addUsmUser(USM arg0) {

    }

    /**
     * Adds initial VACM configuration.
     */
    @Override
    protected void addViews(VacmMIB vacm) {
        vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                        "cpublic"), new OctetString("v1v2group"),
                StorageType.nonVolatile);

        vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                new OctetString("fullWriteView"), new OctetString(
                        "fullNotifyView"), StorageType.nonVolatile);

        vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.3"),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);

    }

    /**
     * Unregister the basic MIB modules from the listOfEps's MOServer.
     */
    @Override
    protected void unregisterManagedObjects() {

    }

    /**
     * Register additional managed objects at the listOfEps's server.
     */
    @Override
    protected void registerManagedObjects() {

    }

    protected void initTransportMappings() throws IOException {
        transportMappings = new TransportMapping[1];
        transportMappings[0] = transport;
    }

    /**
     * Start method invokes some initialization methods needed to start the
     * listOfEps
     *
     * @throws IOException
     */
    public void start(SnmpUtils.SnmpTransportType type, String host, int port) throws IOException {
        transport = SnmpUtils.getTransport(type, host, port);
        init();
        // This method reads some old config from a file and causes
        // unexpected behavior.
        // loadConfig(ImportModes.REPLACE_CREATE);
        addShutdownHook();
        getServer().addContext(new OctetString("public"));
        finishInit();
        run();
        sendColdStartNotification();
    }

    /**
     * Clients can register the MO they need
     */
    public void registerManagedObject(ManagedObject mo) {
        try {
            server.register(mo, null);
        } catch (DuplicateRegistrationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void unregisterManagedObject(MOGroup moGroup) {
        moGroup.unregisterMOs(server, getContext(moGroup));
    }

}

/**
 * This class creates and returns ManagedObjects
 * @author Shiva
 *
 */
class MOCreator {
    public static MOScalar createReadOnly(OID oid, Object value ){
        return new MOScalar(oid,
                MOAccessImpl.ACCESS_READ_ONLY,
                getVariable(value));
    }

    private static Variable getVariable(Object value) {
        if(value instanceof String) {
            return new OctetString((String)value);
        }
        throw new IllegalArgumentException("Unmanaged Type: " + value.getClass());
    }

}

public class NioSnmpDemo {

    private static final Logger LOG = LoggerFactory.getLogger(NioSnmpDemo.class);

    private static final OID OID_CURRENT_DATE = new OID(".1.3.6.1.2.1.1.1.0");

    private static int ENDPOINT_START_PORT = 2000;

    private static int MANAGER_START_PORT = 12000;

    private static int NUMBER_OF_ENDPOINTS = 10;

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    private List<NetworkManagementEndpoint> listOfEps = Lists.newArrayList();

    /**
     * This is the listOfEpManagers which we have created earlier
     */
    private List<NetworkEndpointManager> listOfEpManagers = Lists.newArrayList();

    public static void main(String[] args) throws IOException, InterruptedException {
        NioSnmpDemo demo = new NioSnmpDemo();
        demo.initEndPoints();
        demo.initEpManager();
        demo.queryOidPeriodically();
    }

    private void initEndPoints() throws IOException {

        for(int i = 0; i < NUMBER_OF_ENDPOINTS; i++) {
            NetworkManagementEndpoint ep = new NetworkManagementEndpoint();

            ep.start(SnmpUtils.SnmpTransportType.TCP, "0.0.0.0", (ENDPOINT_START_PORT + i));

            // Since BaseAgent registers some MIBs by default we need to unregister
            // one before we register our own OID_CURRENT_DATE. Normally you would
            // override that method and register the MIBs that you need
            ep.unregisterManagedObject(ep.getSnmpv2MIB());

            // Register OID with current date
            ep.registerManagedObject(MOCreator.createReadOnly(OID_CURRENT_DATE,
                    now()));

            listOfEps.add(ep);
        }

    }

    private void initEpManager() throws IOException {
        for (int i = 0; i < NUMBER_OF_ENDPOINTS ; i++) {

            // Setup the listOfEpManagers to use our newly started listOfEps
            NetworkEndpointManager epManager = new NetworkEndpointManager();
            //XXX: add 100 to avoid AddressAlreadyInUse
            epManager.start(SnmpUtils.SnmpTransportType.TCP, "127.0.0.1", (MANAGER_START_PORT + i * 100));
            epManager.addEndPointAddress(SnmpUtils.SnmpTransportType.TCP, "127.0.0.1", (ENDPOINT_START_PORT + i));

            listOfEpManagers.add(epManager);
        }
    }


    private void queryOidPeriodically() throws IOException, InterruptedException {
        while(true) {
            for (NetworkEndpointManager epManager : listOfEpManagers) {
                String oidResult = epManager.getAsString(OID_CURRENT_DATE);
                LOG.info("{} | {} = {}", new Object[]{epManager.getServerTransport(), OID_CURRENT_DATE.getSyntaxString(), oidResult});
            }

            Thread.sleep(3000);
        }
    }

    private static String now() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(calendar.getTime());
    }

}