package supernovaw.lifetimer.gui;

/**
 * {@link TimelineNavigator} is responsible for handling computations related
 * to converting between pixel coordinates and UNIX timestamp values, as well
 * as smoothly zooming the timeline in and out with mouse scroll.
 */
public class TimelineNavigator {
	// the 'f' local variables in this class mean a point relative
	// to 'start' and 'end' where 0 is 'start' and 1 is 'end'
	public static final int TRANSITION_DURATION = 300;
	public static final long MAX_LENGTH = (long) 100 * 365 * 86400 * 1000;

	public int pixelWidth;
	private long start, end; // timestamp bounds of this timeline

	private long prevStart, prevEnd; // previous values for smooth transitions
	private long transitionTriggerTime;
	private boolean isTransitioning;

	public TimelineNavigator() {
		start = System.currentTimeMillis() - 3 * 86400_000;
		end = start + 6 * 86400_000;
	}

	// current start and end timestamps with account for smooth transition
	public long start() {
		if (!isTransitioning) return start;
		double phase = computeTransitionPhase();
		if (phase >= 1) {
			isTransitioning = false;
			return start;
		}
		return (long) (prevStart * (1 - phase) + start * phase);
	}

	public long end() {
		if (!isTransitioning) return end;
		double phase = computeTransitionPhase();
		if (phase >= 1) {
			isTransitioning = false;
			return end;
		}
		return (long) (prevEnd * (1 - phase) + end * phase);
	}

	/**
	 * Smoothly zooms the timeline out about {@code anchorX} with a factor of {@code Math.exp(amount)}.
	 *
	 * @param anchorX mouse position, the timestamp at this position will remain in place
	 * @param amount  negative values zoom in, positive values zoom out
	 */
	public void zoom(int anchorX, double amount) {
		long start = start();
		long end = end();
		if (isTransitioning) amount *= 1.5;

		double f = (double) anchorX / pixelWidth;
		long anchor = getTimestamp(anchorX);
		long presentLengthMs = end - start;
		double factor = Math.exp(amount);
		long newLengthMs = (long) (factor * presentLengthMs);
		if (newLengthMs < 100 && newLengthMs < presentLengthMs) return; // prevent deadlock
		if (newLengthMs > MAX_LENGTH && newLengthMs > presentLengthMs) return;

		isTransitioning = true;
		transitionTriggerTime = System.currentTimeMillis();
		prevStart = start;
		prevEnd = end;

		this.start = (long) (anchor - newLengthMs * f);
		this.end = (long) (anchor + newLengthMs * (1 - f));
	}

	public long getTimestamp(int x) {
		long start = start();
		long end = end();
		double f = (double) x / pixelWidth;
		return (long) (start + f * (end - start));
	}

	public int getX(long timestamp) {
		long start = start();
		long end = end();
		double f = (double) (timestamp - start) / (end - start);
		return (int) (f * pixelWidth);
	}

	// important: the value this method returns is only updated upon repaints
	// without any repaints it will return true indefinitely
	public boolean isTransitioning() {
		return isTransitioning;
	}

	// only run this when transitioning
	private double computeTransitionPhase() {
		long t = System.currentTimeMillis();
		double p = (double) (t - transitionTriggerTime) / TRANSITION_DURATION;
		if (p >= 1) return p;
		return MainWindow.easeFunction(p);
	}
}
