package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import common.Message;
import common.StoreInterface;
import replica.Replica;
import server.Log;	
import server.LogMessage;

public class Coordinator {
	private Map<StoreInterface, String> replicas =  new ConcurrentHashMap<>();
	private Log log;
	private LogMessage entry;
	private int numberReplica;
	private static String role;
	private Replica replica = null;
	private String leaderIP = null;
	private boolean synced = true;
	private Timer timer;
	private boolean bServer = true;
	private boolean[] replicaList = new boolean[10]; 
	public static final String FOLLOWER = "FOLLOWER",
			CANDIDATE = "CANDIDATE",
			LEADER = "LEADER",
			REDIRECT = "REDIRECT";
	
	/** Constructor 
	 */
	public Coordinator(Replica r){
		Path path = Paths.get("Replicas.txt");
		readReplicaFile(path, 0, "all");
		log = new Log("CoordinatorLog.txt");
		role = FOLLOWER;
		this.replica = r;
		recovery();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run()
			{
				checkState();
				for(int i = 0; i < replicaList.length; i++) {
					if (!replicaList[i])
						readReplicaFile(path, i, "partial");
				}
					
			}

		}, 5*60*1000, 5*60*1000);
		
		/*task = new TimerTask() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				System.out.println("Timer task");
				coordinator.doSync();
			}
			
		};
		timer.scheduleAtFixedRate(task, 60, 120);*/
		
	}

	/** Read the replica file to get IPs and ports of replicas
	 * @param path is the name of a replica file  
	 */
	private void readReplicaFile(Path path, int index, String s) {
		String line = null;
		int i = 0;
		
		if (s.equals("all"))
			numberReplica = 0;
		
		try {
			BufferedReader reader = Files.newBufferedReader(path, Charset.forName("US-ASCII"));
			while ((line = reader.readLine()) != null) {
				String[] args = line.split(":");
				if (args.length != 2) {
					System.out.println("invalid line: "+line);
					continue;
				}
				/*replicas.put((StoreInterface) Naming.lookup("rmi://"+args[0]+":"+args[1]+"/replica"+i), args[0]);*/
				if (s.equals("all")) {
					lookup(args[0], args[1]);
					replicaList[i] = bServer;
					numberReplica++;
				} else if (i == index){
					lookup(args[0], args[1]);
					replicaList[i] = bServer;
				}
				i++;
			}
		} catch (IOException e) {
			System.out.println("replica "+line+"does not exist!");
			e.printStackTrace();
		}
	}

	private void lookup(String ip, String port) {
		try {
			replicas.put((StoreInterface) Naming.lookup("rmi://"+ip+":"+port+"/replica"), ip);
			bServer = true;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			bServer = false;
			e.printStackTrace();
		}
	}
	/** Do commit part of Two-phase commit protocol 
	 * @param type is type of command message
	 * @param key is a key 
	 * @param val is a value of a key
	 */
	private boolean commit(String type, String key, String val) {
		//Phase 1: Commit Vote
		try {
			writeLog(type, key, val, Log.START_2PC, "");
			for (StoreInterface replica: replicas.keySet()) {
				if (!replica.commitReady()) {
					System.out.println("Received a negative vote for commit "+type+" "+key+" "+val);
					writeLog(type, key, val, Log.VOTE_ABORT, replicas.get(replica));
					abort(type, key, val);
					return false;
				}
				writeLog(type, key, val, Log.VOTE_COMMIT, replicas.get(replica)); 
			}
		} catch (IOException e) {
			writeLog(type, key, val, Log.VOTE_ABORT, "");
			abort(type, key, val);
			System.out.println("Exception in vote for commit "+type+" "+key+" "+val);
			e.printStackTrace();
			return false;
		}

		//Phase 2: Commit
		try {
			writeLog(type, key, val, Log.GLOBAL_COMMIT,""); 
			for (StoreInterface replica: replicas.keySet()) {
				if ((type.equals("put") && !replica.put(key,val)) || type.equals("del") && !replica.delete(key)) {
					System.out.println("Commit '"+type+" "+key+" "+val+"' failed in one replica!");
					writeLog(type, key, val, Log.COMMIT_FAIL, replicas.get(replica));
					undo(type,key,val);
					return false;
				} 
			}
			writeLog(type, key, val, Log.COMMIT_DONE, replicas.get(replica));
		} catch (IOException e) {
			writeLog(type, key, val, Log.COMMIT_FAIL, "");
			undo(type,key,val);
			System.out.println("Exception in commit "+type+" "+key+" "+val);
			e.printStackTrace();
			return false;
		}
		System.out.println("Committed "+type+" "+key+" "+val+" to the store");
		return true;
	}

	/** Do get message 
	 * @param key is a key that users want to get its value
	 */
	private String get(String key) {
		String value = null;

		try {
			for (StoreInterface replica: replicas.keySet()) {
				value = replica.get(key);
				if (value == null || !value.equals("locked!")) {
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Exception in get!");
			e.printStackTrace();
		}
		return value;
	}

	/**  
	 * @param type is type of command message
	 * @param key is a key 
	 * @param val is a value of a key 
	 */
	private void abort(String type, String key, String val) {
		writeLog(type, key, val, Log.GLOBAL_ABORT, ""); 
		for (StoreInterface replica: replicas.keySet()) {
			try {
				replica.abort();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**  
	 * @param type is type of command message
	 * @param key is a key 
	 * @param val is a value of a key 
	 */
	private void undo(String type, String key, String val) {
		writeLog(type, key, val, Log.GLOBAL_UNDO, "");
		for (StoreInterface replica: replicas.keySet()) {
			try {
				replica.undo(type,key,val);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/** Write a log in the sever log file 
	 * @param msgType is type of command message
	 * @param key is a key 
	 * @param val is a value of a key
	 * @param logMsg is the log type
	 * @param replicaip is an IP of replica
	 */
	private void writeLog(String msgType, String key, String value, String logMsg, String replicaip) {			
		entry = new LogMessage();			
		entry.setMsgType(msgType);	
		entry.setKey(key);			
		entry.setValue(value);			
		entry.setMessage(logMsg);			
		entry.setReplicaIP(replicaip);			
		log.addLog(entry);			
	}			

	/** Check the database of replicas to make sure they are sync.
	 * @return the status of databases
	 */
	private String isSync() {
		Map<String,String> db1 = new ConcurrentHashMap<>();
		Map<String,String> db2 = new ConcurrentHashMap<>();

		for (StoreInterface replica: replicas.keySet()) {
			try {
				if (db1.isEmpty()) {
					db1 = replica.loadDB();
				} else {
					db2.clear();
					db2 = replica.loadDB();
					if (db1.size() != db2.size()) 
						return "out of sync!";
					for (String key: db1.keySet()) {
						if (!db2.containsKey(key) || db1.get(key) != db2.get(key)) 
							return "out of sync!";
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return db1.size()+" database entries are synced.";
	}

	/** Return the role
	 * @return role
	 */
	public static String getRole() {
		return role;
	}
	
	/** reset votes to false
	 * If a server votes for a candidate, at the end of the election, 
	 * the status of vote should be sat to false in order to be ready 
	 * for next election.
	 */
	private void resetVote() {
		for (StoreInterface replica: replicas.keySet())
			try	{
				replica.resetVote();
			} catch (RemoteException e)	{
				e.printStackTrace();
			}
	}
	/** Check the role of the server and if there is on leader in the network, tries to become a new leader.  
	 * @return the status 
	 */
	private String checkRole() {
		int voteCount = 0;
		if (role.equals(LEADER)) 
			return LEADER;
		else {
			leaderIP = whoIsLeader();
			System.out.println("The leader is: "+ leaderIP);
			if (leaderIP == null /*&& leaderIPPort.equals(replica)*/) {
				if (role.equals(FOLLOWER) &&  !replica.locked && !replica.vote) {
					try {
						role = CANDIDATE;  
						for (StoreInterface replica: replicas.keySet()) {
							if (!replica.reqVote())
								System.out.println("Received a negative vote for me "); 
							else
								voteCount++;
						}
						System.out.println("Number of vote is " + voteCount); 
					} catch (RemoteException e)	{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (voteCount > numberReplica / 2) {
						role = LEADER;
						resetVote();
						System.out.println(replicas.get(replica) + " is the leader!"); 
						return role;
					}
				}
			} else {
				return REDIRECT;
			}
		}
		role = FOLLOWER;
		resetVote();
		return role;
	}
	
	/** Ask other servers to find the leader if it is available  
	 * @return the replica otherwise null 
	 */
	private String whoIsLeader() {
		try { 
			for (StoreInterface replica: replicas.keySet()) {
				if (replica.isLeader()) {
					System.out.println("Found the leader: "+ replicas.get(replica));
					return replicas.get(replica);
				}
			}
		} catch (RemoteException e)	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** Process the requests which could be get, put, and delete a key or check the status of databases.
	 * @param query is message
	 * @return the status of process
	 */
	public synchronized String process (Message query) {
		String role = checkRole();
		if (!synced && !role.equals(LEADER)) {
			if (doSync())
				synced = true;
			/*else
				return "unstable";*/
		}
		if (role.equals(REDIRECT)) {
			Redirect redirect = new Redirect();
			return redirect.run(query, leaderIP, TPCServer.LISTEN_PORT);
		} else if (role.equals(FOLLOWER)){
			return "noleader";
		} else {
			if (query.type.equals("sync")) {
				System.out.println("checking database ...");
				return isSync();
			} else if (query.type.equals("put") || query.type.equals("del")) {
				System.out.println("starting two phase commit "+query.type+" ("+query.key+","+query.value+")");
				return commit(query.type, query.key, query.value)?"succeeded":"failed";
			} else if (query.type.equals("get")) {
				query.value = get(query.key);
				System.out.println("retreived "+query.value);
				return query.value;
			} else {
				System.out.println("unrecognized command.");
				return "unknown";
			}
		}
	}

	/** Do recovery
	 * get the latest message, and check if something is pending, it tries to do that   
	 */
	private void recovery() {	
		entry = new LogMessage();			
		log.loadFromFile();			
		entry = log.latest();			
		if (entry != null && !entry.getMessage().equals(Log.COMMIT_DONE)) {	
			System.out.println("Commit has not done! try to redo it.");
			redo();			
		}			
	}
	
	/** re commit 
	 * If a request did not complete, and there is not commit_done in the log, the server tries to do the transaction again.     
	 */
	private String redo() {
		String msg;
		entry = new LogMessage();
		entry = log.latestOperation();
		
		if (entry == null)
			return null;
		msg = entry.getMsgType() + " " + entry.getKey() + " " + entry.getValue();
		System.out.println("redo: Exception in commit "+entry.getMsgType()+" "+entry.getKey()+" "+entry.getValue());
		Message query = new Message(msg);
		return process(query);
	}
	
	/** Tries to sync its database with the leader's database.
	 */
	private synchronized boolean doSync() {
		StoreInterface replicaLeader = null;
		
		String leaderIPAddress = whoIsLeader();
		System.out.println("The leader is: "+ leaderIPAddress);
		if (leaderIPAddress != null) {
			Map<String,String> db1 = new ConcurrentHashMap<>();
			Map<String,String> db2 = new ConcurrentHashMap<>();
			
			for (StoreInterface replica: replicas.keySet()) {
				if (leaderIPAddress.equals(replicas.get(replica))) {
					replicaLeader = replica;
					break;
				}
			}
			
			try {
				db1 = replicaLeader.loadDB();
				db2 = replica.loadDB();

				for (String key: db1.keySet()) {
					if (!db2.containsKey(key) || db1.get(key) != db2.get(key))
						db2.put(key, db1.get(key));
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
	
	/** Check the database of replicas to make sure they are sync.
	 * @return the status of databases
	 */
	private String checkSync() {
		StoreInterface replicaLeader = null;
		
		String leaderIPAddress = whoIsLeader();
		System.out.println("The leader is: "+ leaderIPAddress);
		if (leaderIPAddress != null) {
			Map<String,String> db1 = new ConcurrentHashMap<>();
			Map<String,String> db2 = new ConcurrentHashMap<>();

			for (StoreInterface replica: replicas.keySet()) {
				if (leaderIPAddress.equals(replicas.get(replica))) {
					replicaLeader = replica;
					break;
				}
			}

			if (replicaLeader == null)
				return "no leader!";
			
			try {
				db1 = replicaLeader.loadDB();
				db2 = replica.loadDB();

				if (db1.size() != db2.size()) 
					return "out of sync!";
				for (String key: db1.keySet()) {
					if (!db2.containsKey(key) || db1.get(key) != db2.get(key)) 
						return "out of sync!";
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return "database entries are synced.";
	}
	
	/** Check the state of system. 
	 * The server sync with the leader or not.
	 */
	private void checkState() {
		System.out.println("Timer task " + synced);
		if (!synced) {
			if (doSync())
				synced = true;
		} else {
			if (checkSync().equals("out of sync!"))
				synced = false;
		}
		return;
	}

}
