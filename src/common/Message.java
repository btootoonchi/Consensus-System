package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Message implements Serializable{
	private static final long serialVersionUID = 3820620171747686884L;
	public String type;
	public String key;
	public String value;
	public Set<String> validCommands;
	
	/** Constructor
	 * @param msg   
	 */
	public Message(String msg){
		String[] args = msg.split(" ");
		this.type = args.length > 0 ? args[0].toLowerCase() : null;
		this.key = args.length > 1 ? args[1].toLowerCase() : null;
		this.value = args.length > 2 ? args[2].toLowerCase() : null;
		
		validCommands = new HashSet<String>();
		validCommands.add("put");
		validCommands.add("get");
		validCommands.add("del");
		validCommands.add("sync");
	}
	
	/** Constructor
	 * @param type is a type of message.
	 * @param key is a key.  
	 */
	public Message(String type, String key) {
		this.type = type;
		this.key = key;
		this.value = null;
	}
	
	/** Check equality of two messages
	 * @param other is a message  
	 */
	public boolean equals(Message other) {
		return this.toString().equals(other.toString());
	}
	
	/** Check a message out
	 * @return true if the message is valid otherwise returns false.  
	 */
	public boolean isValid() {
		if (type == null || (!type.equals("sync") && key == null) || !validCommands.contains(type))
			return false;
		if (type.equals("put") && value == null)
			return false;
		return true;
	}
	
	/** Get type, key, and value of a massage
	 * @return a string of type, key, and value
	 */
	public String toString() {
		return type+" "+key+" "+value;
	}
}
