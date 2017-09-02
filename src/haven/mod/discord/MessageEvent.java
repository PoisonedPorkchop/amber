package haven.mod.discord;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class MessageEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2848073909436036669L;

	public MessageEvent(Object msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

}

interface MessageListener {
	void messageReceived(String msg);
}

class Initiater {
	private List<MessageListener> listeners = new ArrayList<MessageListener>();

	public void addListener(MessageListener ml) {
		listeners.add(ml);
	}

	public void message(String msg) {
		System.out.println("Message Recieved: " + msg);

		// Notify everybody that may be interested.
		for (MessageListener ml : listeners)
			ml.messageReceived(msg);
	}
}

//class Responder implements MessageListener {
//	@Override
//	public void messageReceived(String msg) {
//		// TODO Auto-generated method stub
//
//	}
//}
