package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

import common.Message;

public class ClientHandler implements Runnable {
	Coordinator coordinator;
	Socket socket;
	
	/** Constructor
	 * @param socket is an instance of Socket
	 * @param coordinator is an instance of Coordinator
	 */
	public ClientHandler(Socket socket, Coordinator coordinator) {
		this.socket = socket;
		this.coordinator = coordinator;
	}

	/** Send a message
	 * @param  message is a get, put, or delete messages
	 */
	public void send(String message) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(message);
			System.out.println("Sent " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Receive a response from replicas
	 * @return the response message  
	 */
	private Message receive() throws SocketTimeoutException {
		ObjectInputStream input;
		Message message = new Message("");
		try {
			input = new ObjectInputStream(socket.getInputStream());
			message = (Message) input.readObject();
		}catch(SocketTimeoutException e){
			throw e;
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Received " + message.toString());
		return message;
	}
	
	/** This returns a value in the range [min,max]
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
	 */
	private int randomInt(int min, int max) {
	    Random rand = new Random();
	    // A random integer value in the range [min,max]
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	/*private String reTry(Message query) {
		
		timer = new Timer();
		int rand = randomInt(150, 300);
		
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run()
			{
				res = coordinator.process(query);
				if (!res.equals("noleader")) {
					timer.cancel();
			        timer.purge();
				}
			}

		}, rand*1000, rand*1000);
		
		return res;
	}*/
	
	/** Send and receive messages
	 */
	@Override
	public void run() {
		String response;

		try {
			Message query;
			while ((query = receive()).toString() == null);
			response = coordinator.process(query);
			if (response != null)
				while (response.equals("noleader")) {
					Thread.sleep(randomInt(150, 300));
					response = coordinator.process(query);
				}
			
			send(response);
			socket.close();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
