package Shared;

import java.util.ArrayList;

/**
 * The PacketLogger class logs all incoming and outgoing packets
 */
public class PacketLogger {

    // Data structure for storage
    private final ArrayList<Packet> incoming;
    private final ArrayList<Packet> outgoing;

    public PacketLogger() {
        incoming = new ArrayList<>();
        outgoing = new ArrayList<>();
    }
    public Packet newIn(Packet packet){
        incoming.add(packet);
        return packet;
    }
    public Packet newIn(Object object){
        return newIn((Packet)object);
    }
    public Packet newOut(Packet packet){
        outgoing.add(packet);
        return packet;
    }
    public Packet getLastIn(){
        return incoming.get(incoming.size()-1);
    }
    public Packet getLastOut(){
        return outgoing.get(outgoing.size()-1);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Incoming:\n");
        incoming.forEach(p->sb.append(p.toString()));
        sb.append("Outgoing:\n");
        outgoing.forEach(p->sb.append(p.toString()));
        return sb.toString();
    }
}