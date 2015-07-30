package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import common.Message;

public class Redirect
{
	public String SERVER_IP = "";
	public int SERVER_PORT = 0;
	public int TIMEOUT = 5000;		// 5 seconds
    private Socket socket;
	
	/** Constructor  
	 */
	public Redirect() {
	}
	
	/** Send a message to a server
	 * @param message is string of a request.
	 */
	public void send(Message message){
		try {
			socket = new Socket(SERVER_IP, SERVER_PORT);
			socket.setSoTimeout(TIMEOUT);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(message);
			System.out.println("Sent " + message.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Receive a message from a server
	 * @return a response message from server 
	 */
	public String receive() throws SocketTimeoutException{
		ObjectInputStream input;
		String message = null;
		try {
			input = new ObjectInputStream(socket.getInputStream());
			message = (String) input.readObject();
		}catch(SocketTimeoutException e){
			throw e;
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Received " + message);
		return message;
	}
	
	/** Get user entries and check it out 
	 * Users can select a test case or type their commands  
	 */
	public String run(Message message, String serverIP, int port) {
		SERVER_IP = serverIP;
		SERVER_PORT = port;
		try {
			send(message);
			return receive();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		}
		return null;
	}
}
