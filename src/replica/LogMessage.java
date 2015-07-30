package replica;

import java.io.Serializable;

public class LogMessage implements Serializable {
	private static final long serialVersionUID = 3732735084917584409L;
	private String msgType = null;
	private String key = null;
	private String value = null;
	private String oldValue = null;

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

	/** Get an old value
	 * @return old value of a key  
	 */
	public final String getOldValue() {
		return oldValue;
	}

	/** Set an old value of a key
	 * @param oldValue. It is an old value of a key.  
	 */
	public final void setOldValue(String oldVal) {
		this.oldValue = oldVal;
	}

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
	
	/** Get a string of message type, key, value, and oldvalue
	 * @param a string
	 */
	public String toString() {
		return msgType + " " + key + " " + value + " " + oldValue;
	}
}
