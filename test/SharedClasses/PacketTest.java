package SharedClasses;

import Shared.Packet;
import Shared.Packet.PacketType;
import Shared.PacketLogger;

public class PacketTest {
    public static void main(String[] args) {
        PacketLogger pktLog = new PacketLogger();
        pktLog.newIn(new Packet(PacketType.SYN, "localhost", "localhost", null, "Luc", "Sparidans", null));
        System.out.println(pktLog.getLastIn());
    }
}
