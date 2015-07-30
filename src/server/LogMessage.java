package server;

import java.io.Serializable;

public class LogMessage implements Serializable {
	private static final long serialVersionUID = 2476491781148155985L;
	private String msgType = null;
	private String key = null;
	private String value = null;
	private String message = null;
	private String replicaIP = null;
	
	/** Get a message type
	 * @return message type  
	 */
	public String getMsgType() {
		return msgType;
	}

	/** Set a message type
	 * @param msgType is a type of a message.  
	 */
	public final void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	/** Get a key
	 * @return value of a key  
	 */
	public final String getKey() {
		return key;
	}

	/** Set a key
	 * @param key is a value of a key.  
	 */
	public final void setKey(String key) {
		this.key = key;
	}

	/** Get a value
	 * @return value   
	 */
	public final String getValue() {
		return value;
	}

	/** Set a value
	 * @param value   
	 */
	public final void setValue(String value) {
		this.value = value;
	}

	/** Get a message log
	 * @return message
	 */
	public final String getMessage() {
		return message;
	}

	/** Set a message log
	 * @param message
	 */
	public final void setMessage(String message) {
		this.message = message;
	}

	/** Get an IP of a replica
	 * @return IP
	 */
	public final String getReplicaIP() {
		return replicaIP;
	}

	/** Set a an IP of a replica
	 * @param replicaip is an IP of a replica
	 */
	public final void setReplicaIP(String replicaip) {
		this.replicaIP = replicaip;
	}
	
	/** Get a string of message type, key, value, message, and replicaIP
	 * @param a string
	 */
	public String toString() {
		return msgType + " " + key + " " + value + " " + message + " "+ replicaIP;
	}
}
