package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface StoreInterface extends Remote {
	public boolean commitReady() throws RemoteException; 
    public boolean put(String key, String val) throws RemoteException; 
    public String get(String key) throws RemoteException; 
    public boolean delete(String key) throws RemoteException;
    public boolean abort() throws RemoteException; 
    public boolean undo(String type, String key, String val) throws RemoteException;
    public Map<String,String> loadDB() throws RemoteException;
    public boolean reqVote() throws RemoteException;
    public boolean isLeader() throws RemoteException;
    public boolean resetVote() throws RemoteException;
}
