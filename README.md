# CS 441 Homework 3 : Implementation of Chord algorithm using Akka Simulations.

## Overview
The Homework aims to make simulations to demonstrate working of Cloud Simulator using Chord Algorithm, for the implementation we are using Akka which is a
open source toolkit for designing scalable, resilient systems that span processor cores and networks.

### Project Members

- Aishwarya Sahani
- Ansul Goenka
- Gautamkumar Ojha
- Mihir Kelkar

### Instructions to run the simulations
#### Prerequisites
- Install [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html)
- Install [Cassandra](https://phoenixnap.com/kb/install-cassandra-on-windows)
- To install Cassandra you will need Python 2.7 & Java SE Development Kit 8u251.
- After installation, to enable Cassandra, set the value of the flag *enableCassandra* to *true* in the application.conf file.

#### Run 
1. Clone the project - ```git clone https://ojhagautam97@bitbucket.org/cs441-fall2020/group_6.git```
2. Navigate to the project folder - cd group_6/
3. Run the simulations with the command : ```sbt clean compile run```
4. Run the test cases using the command : ```sbt clean compile test```
5. Open the link generated by the WebService at ```http://localhost:8080/```


### Homework Files 

- WebService 
    - This is the entry point for our homework, this scala file uses Akka-HTTP library for making routes for different options to run the simulations 
    by calling methods for required simulation task.
    - After running this object file you get a link ```http://localhost:8080/``` which will redirect to the webpage with 4 buttons:
        - Add Node : Clicking this button calls method createServerNode() which adds node.
        - Load Data : Clicking this button calls method loadData(id.toInt) which loads the result in the form of string in the server. To load data append ?id=<any integer> to your link.
        - Lookup Data : Clicking this button calls method getData(id.toInt) which is used by the user to look data over a server node using Chord Protocol. To look-up data append ?id=<any integer> to your link.
        - Snapshot : Clicking this button simply returns all the result for the simulation.
        - Montecarlo : Clicking this button invokes the Rclient object to randomly select the 4 options from above. The 4 options are generated randomly and they are : 1.AddNode, 2.Snapshot, 3.LoadData, 4.LookupData. To use Monte-Carlo append ?number=<any integer> to your link.
        
- ActorDriver
    - This object file defines the number of users, servers, ActorSystem, Actors (serverActor, userActor, supervisorActor).
    - It also defines methods used by Webservices and defined in the actor class files to load data, lookup data and display the result.

-  ServerActor
    - This class file represents actor-server which implements chord algorithm and defines messages as follows:
        - case initializeFirstFingerTable(nodeIndex: Int) : It initializes the first finger table for the first server-node.
        - case initializeFingerTable(nodeIndex: Int) : It initializes the finger table for all the server nodes after the first one.
        - case updateOthers(nodeIndex: Int) : It updates all nodes whose finger table should refer to the new node which joins the network.
        - case updateTable(predecessorValue:Int, nodeIndex: Int, i: Int) : It is invoked by updateOthers which recursively updates finger-table.
        - case updatePredecessor(nodeIndex: Int) : This case class updates the predecessor.
        - case getSuccessor() : Returns the successor of the given entry.
        - case getDataServer(id: Int, m: Int, hash: Int) : Returns output when looked-up is performed. 
    - Also, the class defines following methods :
        - getData(id: Int, m: Int, hash: Int) : Returns result in the form of string when invoked by getDataServer(id: Int, m: Int, hash: Int).
        - findSuccessor(nodeIndex: Int) : Returns successor value for the given node by fetching successor value for an arbitrary node and eventually updating the successor value for the given node.
        - findPredecessor(nodeIndex: Int) : Returns predecessor value for the given node by invoking another method closestPrecedingFinger(nodeIndex: Int) which returns finger table value for the given node.
        - belongs(s:Int, n: Int, successorValue: Int) : Invoked by updateTable() to check whether the node belongs within the range of predecessor and fingerIthEntry value.
        
- UserActor
    - This class file represents actor-user and defines messages as follows:
        - case class loadData(data:EntityDefinition) : Returns result of the loaded data from the server to the user. 
        - case getDataUserActor(id) : Returns result by looking up data from the server.
        - case createUserActor(id) : Returns path of created user actor.
- SupervisorActor 
    - This class acts as a bridge between user and the server actor. The user actor invokes the messages defined in this class which returns results by invoking messages defined in the ServerActor.
    
- Utility 
    - This object file takes a string and number of bits to return hashed value used for generating keys inserted into DHT and for data units.
    - The hashing algorithm used is MD5.
    
## Results

1.Adding Node : Adding the created node.

- First Node Added : 13

```
INFO  [SupervisorActor]: Sever Actor Created: 13
INFO  [ServerActor]: LinkedHashMap(14 -> 13, 15 -> 13, 1 -> 13, 5 -> 13)
```

- Second Node Added : 6

```
INFO  [SupervisorActor]: Sever Actor Created: 6
INFO  [ServerActor]: ActorSelection[Anchor(akka://actorSystem/), Path(/user/server_actor_13)]
```

2.Load Data : Using id=7 to load data at any server node (The id has to be passed at the end of the url as follows: ?id=7)


```
INFO  [WebService$]: In loadData webservice
INFO  [WebService$]: In loadData webservice
INFO  [ActorDriver$]: In loadData driver
INFO  [UserActor]: In loadData UserActor
INFO  [SupervisorActor]: In loadDataSupervisor SupevisorActor
INFO  [ServerActor]: Checking if 4 belongs in the range 7 - 9
INFO  [ServerActor]: Checking if 4 belongs in the range 8 - 11
INFO  [ServerActor]: Checking if 4 belongs in the range 10 - 15
INFO  [ServerActor]: Checking if 4 belongs in the range 14 - 8
INFO  [ServerActor]: Data stored at 6
```
- WebService result
    - Loaded Data at 6 : ```Added: Id: 7, Name: Waiting For Forever```

3.Lookup Data : Looking up data with id = 7 to check whether the data loaded at 6 can be retrieved. (The id has to be passed at the end of the url as follows: ?id=7)

```
INFO  [ServerActor]: Checking if 4 belongs in the range 7 - 9
INFO  [ServerActor]: Checking if 4 belongs in the range 8 - 11
INFO  [ServerActor]: Checking if 4 belongs in the range 10 - 15
INFO  [ServerActor]: Checking if 4 belongs in the range 14 - 8
INFO  [ServerActor]: Data was stored at 6  
```

- WebService result
    - Looking Up data at 6 : ```Lookup value: 7 Some(Waiting For Forever)```

4.Snapshot : Returns the overall Fingertable value.

```
INFO  [WebService$]: Snapshot Web Service
INFO  [ActorDriver$]: Print Snapshot Driver
INFO  [SupervisorActor]: Get Snapshot
INFO  [SupervisorActor]: LinkedHashMap(7 -> 13, 8 -> 13, 10 -> 13, 14 -> 6)
INFO  [SupervisorActor]: Get Snapshot
INFO  [SupervisorActor]: LinkedHashMap(14 -> 13, 15 -> 13, 1 -> 13, 5 -> 6)
```

- Webservice result
    - Snapshot created : ```6 -> LinkedHashMap(7 -> 13, 8 -> 13, 10 -> 13, 14 -> 6) 13 -> LinkedHashMap(14 -> 13, 15 -> 13, 1 -> 13, 5 -> 6)```
    
5.MonteCarlo : Generates random requests based on the number specified. In order to introduce randomness, the eval function of the R client is used.

Here we have used numbers = 5.
- First choice is 1.AddNode, thus a server actor is created at 0 as below:
```
INFO  [WebService$]: choice = 1
INFO  [Slf4jLogger]: Slf4jLogger started
INFO  [SupervisorActor]: Sever Actor Created: 0
INFO  [ServerActor]: LinkedHashMap(1 -> 0, 2 -> 0, 4 -> 0, 8 -> 0)
```

- Second choice is 3.LoadData with id=49 is loaded as below and stored at 0:
```
INFO  [WebService$]: choice = 3
INFO  [ActorDriver$]: In loadData driver
INFO  [UserActor]: In loadData UserActor
INFO  [SupervisorActor]: In loadDataSupervisor SupevisorActor
INFO  [ServerActor]: Checking if 7 belongs in the range 1 - 3
INFO  [ServerActor]: Checking if 7 belongs in the range 2 - 5
INFO  [ServerActor]: Checking if 7 belongs in the range 4 - 9
INFO  [ServerActor]: Data stored at 0
```

- Third choice is 1.AddNode, thus a server actor is created at 10 as belows:
```
INFO  [WebService$]: choice = 1
INFO  [SupervisorActor]: Sever Actor Created: 10
```

- Fourth choice is 3.LoadData with id = 34 is loaded as below and stored at 0:
```
INFO  [WebService$]: choice = 3
INFO  [ActorDriver$]: In loadData driver
INFO  [UserActor]: In loadData UserActor
INFO  [ServerActor]: Data stored at 0
```

- Fifth choice is 1.AddNode, thus a server actor is created at 5 as belows:
```
INFO  [WebService$]: choice = 1
INFO  [ServerActor]: Successor Found, value = 0
INFO  [ServerActor]: Second Instance: LinkedHashMap(11 -> 0, 12 -> 0, 14 -> 0, 2 -> 0)
INFO  [SupervisorActor]: Sever Actor Created: 5
```

- Finally this is how the overall result is for numbers=5
```
INFO  [WebService$]: 1.AddNode: NodeAdded
3.LoadData(49): Id: 49, Name: Knocked Up
1.AddNode: NodeAdded
3.LoadData(34): Id: 34, Name: New Year's Eve
1.AddNode: NodeAdded
```

- Webservice result
    - MonteCarlo result : ```1.AddNode: NodeAdded 3.LoadData(49): Id: 49, Name: Knocked Up 1.AddNode: NodeAdded 3.LoadData(34): Id: 34, Name: New Year's Eve 1.AddNode: NodeAdded```
    






        
        
        




