
public class FileLock {
	
	private boolean locked = false;
	private int ownerId = 0;
	
	public synchronized boolean lockIfUnlocked(int ownerId) {
		if (!locked) {
			this.ownerId = ownerId;
			locked = true;
			return true;
		}
		return false;
	}

	public synchronized void unlock() {
		this.ownerId = 0;
		locked = false;
	}
	
	public synchronized boolean isLocked(int ownerId) {
		return locked && (this.ownerId == ownerId);
	}
}
