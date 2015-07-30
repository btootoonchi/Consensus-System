package server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import replica.Replica;

public class TPCServer {
	public static int LISTEN_PORT = 5001;
	ArrayList<ClientHandler> clients;

	/** Constructor
	 * @param clients is a list of ClientHandlers
	 */
	public TPCServer(ArrayList<ClientHandler> clients) {
		this.clients = clients;
	}

	/** Run the thread
	 */
	public void run() {
		while (true) {
			try	{
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (ClientHandler hdl: clients) {
				if (hdl == null || clients.size() == 0)
					break;
				if (hdl.socket.isClosed()) {
					System.out.println("removing "+hdl.socket.getInetAddress()+":"+hdl.socket.getPort()+" clients: "+clients.size());
					clients.remove(hdl);
					break;
				}
			}
		}
	}
	
	/** Main function of server  
	 * @param args 
	 */
	public static void main(String[] args) {
		ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

		Replica replica = null;
		try	{
			replica = new Replica();
		} catch (RemoteException e)	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(replica).start();
		
		Coordinator coordinator = new Coordinator(replica);
		
		PortListener lt = new PortListener(LISTEN_PORT, clients, coordinator);
		new Thread(lt).start();
		
		TPCServer server = new TPCServer(clients);
		server.run();
	}
}
