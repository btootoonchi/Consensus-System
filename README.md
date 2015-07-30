# Consensus-System project
=======================================

Distributed consensus systems have drawn considerable attention over the past few years due to the variety of their applications for example in payment systems and wireless sensor networks. Distributed consensus algorithms build a reliable distributed system over an unreliable infrastructure. In this work, we implemented a modified version of the Raft consensus algorithm over the two phase commit platform. Raft introduces a mechanism for coordinating changes to cluster members by applying the commits through a leader at each term. The modifications in our implementation were mostly in the messages that are transferred in the network and in the error handling parts. We believe that our implementation improves the Raft algorithm in terms of network traffic and speed. We have handled faults and failures in two layers, Raft level and Two-Phase Commit layer by using logs and recovery functions. 

TABLE OF CONTENTS
-----------------
    MINIMUM REQUIREMENTS
    INSTALLATION
    WHAT'S NEW

MINIMUM REQUIREMENTS:
---------------------
     o Windows Compatible PC with jdk 7 or later installed
     o The project packages: client, server, replica, and common
     o A text file that contains the list of replicas' "ip:port" information to be given to the server.
	 o Free space on disk for creating the databases and logs

SERVER SETUP:
-----------------------
    Place the server, replica and common packages on the server machine. Make sure to create a "Replicas.txt" file that contains the information about the host name and port number of the replicas in the following format: 
	
	Example "Replicas.txt":
	
	192.168.0.177:5555
	127.0.0.1:5555
	...
	
	Compile and run the source files using:
	javac (source files in the server, replica and common packages)
	java  TPCServer.class

	When you start running the program, a database "kvstore" will get created. You will be able to watch the transaction history on the console output. Also, all the transactions will be logged in ReplicaLog.txt. The logs are only readable by the program.

	After starting the server, it binds to the registry files for remote method invocation on replicas and listens for incoming requests from the client. The number of requests that can be processed concurrently is bound to 50 for this release. If more requests are received, there will be delay in processing their responses.
	The server stores its transaction history in CoordinatorLog.txt which is only readable by the program.
	
CLIENT SETUP:
-----------------------	
	Place the client and common packages on the client machines. 
	Compile and run the source files using:
	javac (source files in the client and common packages)
	java  Client.class
	
	In the client console, you can type "put", "del", or "get" commands in the following format. 
	command key value
	Example:
	put babak tootoonchi
	
	Note that the keys are unique and putting a different value to a key will replace to old value.	You can also type "test" to see the list of available unit test options. You can run all the unit tests by typing '0'. To exit the program type "exit".

RUN THE PROGRAM:
----------------
    To run the program, run the replicas on different machines and set the IP and port addresses in replicas.txt accordingly. Now you can use multiple machines to run as clients and send requests on port 5001.
	Please see https://github.com/btootoonchi/Consensus-System for project source codes.

Enjoy!

________________________________________________________________________
 Babak Tootoonchi, Overlay+Peer to Peer Networking Course Project 
 Computer Science Department, University of Victoria.
 July 2015
________________________________________________________________________
