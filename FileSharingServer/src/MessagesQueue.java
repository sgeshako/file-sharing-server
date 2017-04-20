import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;


public class MessagesQueue extends AbstractQueue<FileDataMessage> {
	
	private LinkedList<FileDataMessage> queue = new LinkedList<FileDataMessage>();
	
	public synchronized FileDataMessage takeOrBlock() {
		while (queue.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		return poll();
	}
	
	public synchronized void put(FileDataMessage e) {
		offer(e);
		if (queue.size() == 1) {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean offer(FileDataMessage e) {
		queue.addLast(e);
		return true;
	}

	@Override
	public synchronized FileDataMessage poll() {
		return queue.removeFirst();
	}

	@Override
	public synchronized FileDataMessage peek() {
		if (queue.isEmpty()) {
			return null;
		}
		return queue.getFirst();
		
	}

	@Override
	public synchronized Iterator<FileDataMessage> iterator() {
		return queue.iterator();
	}

	@Override
	public synchronized int size() {
		return queue.size();
	}

}
