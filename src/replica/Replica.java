package replica;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Map;

import server.Coordinator;
import common.StoreInterface;

public class Replica extends UnicastRemoteObject implements StoreInterface, Runnable {
	private static final long serialVersionUID = -766086615922686504L;
	private static String mIP = "localhost";
	private int mPort = 5555;
	private static long timeout = 10000;
	private int numberServer = 1;

	private LogMessage entry;
	private Store KVStore = new Store();
	private Log log = new Log("ReplicaLog.txt");
	public boolean locked = false;
	private long startTime; 
	public boolean vote = false;
	
	/** Constructor 
	 * Provides methods for storing and obtaining references to remote objects in a remote object registry.
	 */
	public Replica() throws RemoteException {
		for(int i = 0; i < numberServer; i++) {
			try {
				System.out.println("connecting to server " + mIP + " : " + mPort);
				LocateRegistry.createRegistry(mPort);
				Naming.bind("rmi://"+mIP+":"+mPort+"/replica",this); 
				mPort++;
			} catch (RemoteException | AlreadyBoundException | MalformedURLException e) { 
				e.printStackTrace(); 
			}
		}
	}
	
	/** Creates a log message and write to the log file
	 * @param type. It is the type of message 
	 * @param key. It is a key.
	 * @param value. It is the value of the key.
	 * @param oldValue. It is the previous value of the key.
	 */
	private void writeLog(String type, String key, String value, String oldValue) {			
		entry = new LogMessage();
		entry.setMsgType(type);
		entry.setKey(key);
		entry.setValue(value);
		entry.setOldValue(oldValue);
		log.addLog(entry);		
	}
	
	/** Run the thread
	 */
	public void run() {
		while (true) {
			if (locked) {
				long elapsedTime = new Date().getTime() - startTime;
				//if the delay between the two phases of commit is more than 10 seconds, release the lock (coordinator crashed?).
				if (elapsedTime > timeout) {
					locked = false;
				}
			}
		}
	}

	/** Ready for commit if it was not locked.
	 * @return true if the replica is not busy otherwise returns false.
	 */
	public boolean commitReady() throws RemoteException {
		if (locked) {
			System.out.println("busy, not ready for commit!");
			return false;
		}
		System.out.println("ready for commit ...");
		locked = true;
		startTime = new Date().getTime();
		return true;
	}

	/** Receive a key and a value, and then calls put function in the Store file.
	 * @param key. It is a key.
	 * @param value. It is the value of the key.
	 * @return true if key put in database successfully otherwise returns false.
	 */
	public boolean put(String key, String val) throws RemoteException {
		boolean success = true;
		String oldValue = null;
		if (!locked) {
			System.out.println("cannot put without commit vote!");
			return false;
		}
		try {
			oldValue = KVStore.get(key);
			success = KVStore.put(key, val);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		if (success) {
			writeLog("put", key, val, oldValue);
		}
		locked = false;
		return success;
	}

	/** Get the value of the key  
	 * @param key
	 * @return the value of the key
	 */
	public String get(String key) throws RemoteException {
		if (!locked) {
			return KVStore.get(key);
		}
		return "locked!";
	}

	/** Delete a key 
	 * @param key
	 * @return true if everything is done otherwise returns false
	 */
	public boolean delete(String key) throws RemoteException {
		if (!locked) {
			System.out.println("cannot delete without commit vote!");
			return false;
		}
		String oldValue = KVStore.get(key);
		boolean success = KVStore.delete(key);
		if (success) {
			writeLog("del", key, "", oldValue);
		}
		
		locked = false;
		return success;
	}

	/** Disable lock status  
	 * @return true 
	 */
	public boolean abort() throws RemoteException {
		locked = false;
		System.out.println("Aborted!");
		return true;
	}
	
	/** Recover to the previous step
	 * @param type. It is a type of a message 
	 * @param key. It is a key.
	 * @param val. Value of key.
	 * @return true if everything is done otherwise returns false
	 */
	public boolean undo(String type, String key, String val) throws RemoteException {
		boolean success = true;
		LogMessage lastEntry = log.latest();
		try {
			//if the command was successfully committed on this node, undo it
			if (lastEntry.getMsgType().equals(type) && lastEntry.getKey().equals(key) && lastEntry.getValue().equals(val)) {
				success = KVStore.put(key, lastEntry.getOldValue());
				if (success) {
					writeLog("put", key, lastEntry.getOldValue(), lastEntry.getValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		locked = false;
		return success;
	}

	/** Get the list of available in the database 
	 * @return list of keys and values
	 */
	public Map<String,String> loadDB() throws RemoteException {
		return KVStore.load();
	}
	
	/** Ask to vote for a replica  
	 * @return true if the replica gives YES to the candidate otherwise false
	 */
	public boolean reqVote() throws RemoteException {
		if (!vote)
			vote = true;
		else 
			return false;
		return vote;
	}
	
	public boolean resetVote() throws RemoteException {
		vote = false;
		return true;
	}
	
	/** Ask other replica who is the leader 
	 * @return the status of the replica
	 */
	public boolean isLeader() throws RemoteException {
		String role = Coordinator.getRole();
		if (role.equals("LEADER"))
			return true;
		return false;
	}
	
	/** Main point of the Replica application 
	 * @param args
	 */
	/*public static void main(String[] args) {
		Replica replica;
		try {
			replica = new Replica();
			System.out.println("connecting to server " + mIP + " : " + mPort);
			replica.run();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}*/
}
