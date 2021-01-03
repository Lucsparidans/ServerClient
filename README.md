# ServerClient
Assignment for Large Scale IT & Cloud Computing  
Deadline: 03-01-2021  
Intellij Project

## Client
The two main activities that a client will execute are checking for messages that were not yet received and executing 
the actions that were specified in their JSON files. To perform the two activities mentioned before, each client must ensure a secure connection to the main server.
There are two types of clients, it can represent either a person or an entire organization. Depending on the client type, the way it
functions differs, using two completely different classes. One is the Client.java class ( used for a client representing a person ) and the other one
is Organization.java class. The Client class mainly waits for incoming messages and executes the listed actions. On the other hand the
Organization class waits for incoming actions to perform, based on other clients sent instructions and checks for permissions. Each person needs to be registered by an
organization to send instructions to it ( for more detailed information, look into the comments inside the class itself ).  
 
## Server
The server acts as a common database for all threads that handle the clients that are connected to the server. The server initializes all the organizations, 
each on a separate thread. It ensures a connection between all the communication partners and make sure to have a secure communication using end-to-end encryption.    

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

## Packets
The Packet class is our way of creating an abstraction for the formatting of messages and data linked to them that are sent over sockets.

### PacketType
The PacketType enum is a way to create an indicator of the content of the Packet that was sent/received.

### JSON_files
6 JSON files including five clients configs and one for the organizations, all storing the respective client's data and set of actions

##How to Run
1. Go to _Edit configurations_
2. Add App.java as _application_ (and allow multiple instances if needed)
3. Add Server.java as _application_ (and allow multiple instances if needed)
4. Create a _Compound_ using the aforementioned applications
5. Run the _Compound_
6. After the process is complete, if you want to close the server, you can type "Quit" in the command prompt
