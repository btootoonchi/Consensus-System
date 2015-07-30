package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PortListener implements Runnable {
	public static int MAX_CONCURRENT_REQUESTS = 50;
	private ServerSocket socket;
	ArrayList<ClientHandler> clients;
	Coordinator coordinator;
	private int port;
	
	/** Constructor
	 * @param port
	 * @param clients
	 * @param coordinator
	 */
	public PortListener(int port, ArrayList<ClientHandler> clients, Coordinator coordinator) {
		this.clients = clients;
		this.port = port;
		this.coordinator = coordinator;
	}

	/** 
	 */
	@Override
	public void run() {
		try {
			socket = new ServerSocket(port);
			System.out.println("listening on: " + port);

			while(true){
				Socket newConnection = null;
				try{
					if (clients != null && clients.size() >= MAX_CONCURRENT_REQUESTS) {	
						continue;
					}
					newConnection = socket.accept();
					InetAddress ip = newConnection.getInetAddress();
					int port = newConnection.getPort();
					ClientHandler newClient = new ClientHandler(newConnection, coordinator);
					clients.add(newClient);
					new Thread(newClient).start();
					System.out.println("connected to " + ip.toString() + ":" + port);
				}catch(IOException e){
					System.out.println("Could not accept client connection");
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 
	 */
	@Override
    protected void finalize() throws Throwable {
		socket.close();
        System.out.println("TPC server shutdown");
        super.finalize();
    }
}
