package connection;

import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

public class DNSConnection extends Connection {
    private DatagramChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocate(5087);
    private Map<Integer, SecondConnection> needAck = new HashMap<>();
    private Map<byte[], InetAddress> cachedHosts = new HashMap<>();
    private Map<Integer, Message> messageSendMap = new HashMap<>();
    private Map<Integer, Message> messageWaitMap = new HashMap<>();
    private Map<Integer, byte[]> addressesIds = new HashMap<>();
    private SocketAddress dnsRequestAddress;
    private static DNSConnection instance;
    private int counter = 0;
    private  int select = 0;


    public static void createInstance(ConnectionSelector connectionSelector) {
        if (instance == null) {
            instance = new DNSConnection(connectionSelector);
        }
    }

    public static DNSConnection getInstance() {
        if (instance == null) {
            throw new NullPointerException("DNSConnection is null");
        }
        return instance;
    }

    private DNSConnection(ConnectionSelector connectionSelector) {
        super(connectionSelector);
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            dnsRequestAddress = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 53);
            channel.connect(dnsRequestAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void newRegistration() {
        if (messageSendMap.size() > 0) select |= SelectionKey.OP_WRITE;
        if (messageWaitMap.size() > 0) select |= SelectionKey.OP_READ;
        connectionSelector.registerConnection(channel, this, select);
    }

    public void resolveAddress(byte[] address, SecondConnection connection) throws TextParseException {
        System.out.println("RESOLVE ADDRESS");
        if (cachedHosts.containsKey(address)) {
            System.out.println("Cached has address: " + cachedHosts.get(address));
            connection.setIpResolved(true, cachedHosts.get(address));
            return;
        }
        needAck.put(counter, connection);
        System.out.println("Resolving " + new String(address));
        Message message = new Message();
        Header header = message.getHeader();
        header.setOpcode(Opcode.QUERY);
        header.setID(counter);
        header.setRcode(Rcode.NOERROR);
        header.setFlag(Flags.RD);

        String hostAddress = new String(address);
        String newHostAddress = hostAddress + ".";
        Name name = new Name(newHostAddress);
        Record record;
        try {
            record = Record.newRecord(name, Type.A, DClass.IN);
        } catch (RelativeNameException e) {
            needAck.remove(counter);
            System.out.println("Cannot create record");
            connection.setIpResolved(false, null);
            return;
        }
        message.addRecord(record, Section.QUESTION);
        System.out.println(message.getHeader().getFlag(Flags.RD));
        messageSendMap.put(counter, message);
        addressesIds.put(counter, address);
        ++counter;
        newRegistration();
    }

    @Override
    public void make(SelectionKey key) throws IOException {

        if (key.isValid() && key.isWritable()) {
            System.out.println("WRITABLE DNS");
            Map.Entry<Integer, Message> record = messageSendMap.entrySet().iterator().next();
            int send = channel.send(ByteBuffer.wrap(record.getValue().toWire()), dnsRequestAddress);

            if (send > 0) {
                System.out.println("SOMETHING HAPPENED");
                messageWaitMap.put(record.getKey(), record.getValue());
                messageSendMap.remove(record.getKey());
                newRegistration();
            }
        }

        if (key.isValid() && key.isReadable()) {
            System.out.println("READABLE DNS");
            int read = channel.read(buffer);

            if (read == -1 || read == 0) {
                return;
            }
            byte[] resp = new byte[buffer.position()];
            System.arraycopy(buffer.array(), 0, resp, 0, resp.length);
            buffer.clear();
            Message response = new Message(resp);

            int id = response.getHeader().getID();
            if (!needAck.containsKey(id)) return;
            SecondConnection connection = needAck.get(id);
            needAck.remove(id);
            messageSendMap.remove(id);
            Record[] records = response.getSectionArray(Section.ANSWER);
            InetAddress resolverIp = null;

            for (Record record : records) {
                if (record.getType() == Type.A) {
                    try {
                        resolverIp = InetAddress.getByAddress(record.rdataToWireCanonical());
                    } catch (UnknownHostException ignored) {
                        continue;
                    }
                    break;
                }
            }

            if (resolverIp != null) {
                cachedHosts.put(addressesIds.remove(id), resolverIp);
                System.out.println("IP RESOLVED: " + resolverIp);
                connection.setIpResolved(true, resolverIp);
            } else {
                System.out.println("IP NOT RESOLVED");
                connection.setIpResolved(false, null);
            }
        }
        newRegistration();
    }

    @Override
    public void finish(SelectionKey key) {
        super.finish(key);
        connectionSelector.exit();
    }

    @Override
    public void finish() {
        connectionSelector.deleteConnection(channel);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}