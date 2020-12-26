# ServerClient
Assignment for Large Scale IT & Cloud Computing  
Deadline: 03-01-2021

## Server
The server acts as a common database for all threads that handle the clients that are connected to the server.

### Synchronisation
All requests to the server are synchronised by means of a synchronisation lock. A thread has to get this lock in
order to request or write data from and to the central server. This method makes sure that we do not get any 
race-conditions.
```java
class Server{
    // The LOCK variable is just a symbolic object that 
    // can be assigned to a thread to symbolise that that threads
    // has access to the synchronisation block
    public static final Object LOCK = new Object();
    public static void method() {
        synchronized (LOCK) {
            // Code that reads or writes to the data structures in the server
        }
    }
}
```

## Client
The two main activities that a client will execute are checking for messages that were not yet received and executing 
the actions that were specified in their JSON files

## Packets
The Packet class is our way of creating an abstraction for the formatting of messages that are sent over sockets.

### PacketType
The PacketType enum is a way to create an indicator of the content of the Packet that was sent/received.