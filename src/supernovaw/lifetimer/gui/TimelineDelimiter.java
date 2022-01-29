package supernovaw.lifetimer.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.function.Consumer;

/**
 * {@link TimelineDelimiter} is responsible for placement and marking
 * of delimiters on the timeline. Depending on how close the timeline
 * is zoomed, different levels of delimiters will show.
 */
public class TimelineDelimiter {
	private static final long D = 86400 * 1000; // 24 hours in milliseconds
	private static final Level[] LEVELS = {
			// seconds
			new Level(1000, 20_000, "ss", "MMM d HH:mm"),
			// minutes
			new Level(60_000, 450_000, "HH:mm", "MMM d"),
			// 5 minutes
			new Level(300_000, D / 16, "HH:mm", "MMM d"),
			// hours
			new Level(D / 24, D, "HH:mm", "MMM d"),
			// days
			new Level(D, 12 * D, "d", "MMMM") {
				@Override
				public void listTimestamps(long from, long to, Consumer<Long> consumer) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(from);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.add(Calendar.DAY_OF_MONTH, 1);

					long t;
					while ((t = c.getTimeInMillis()) < to) {
						consumer.accept(t);
						c.add(Calendar.DAY_OF_MONTH, 1);
					}
				}

				@Override
				public String formatDelimiter(long t) {
					String num = super.formatDelimiter(t);
					return switch (num) {
						case "1", "21", "31" -> num + "st";
						case "2", "22" -> num + "nd";
						case "3", "23" -> num + "rd";
						default -> num + "th";
					};
				}
			},
			// weeks
			new Level(7 * D, 45 * D, "E, MMM d", "MMMM") {
				@Override
				public void listTimestamps(long from, long to, Consumer<Long> consumer) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(from);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());

					long t;
					while ((t = c.getTimeInMillis()) < to) {
						consumer.accept(t);
						c.add(Calendar.DAY_OF_YEAR, 7);
					}
				}
			},
			// months
			new Level(30 * D, 450 * D, "MMM", "yyyy") {
				@Override
				public void listTimestamps(long from, long to, Consumer<Long> consumer) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(from);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.DAY_OF_MONTH, 1);

					long t;
					while ((t = c.getTimeInMillis()) < to) {
						consumer.accept(t);
						c.add(Calendar.MONTH, 1);
					}
				}
			},
			// years
			new Level(365 * D, 6000 * D, "yyyy", null) {
				@Override
				public void listTimestamps(long from, long to, Consumer<Long> consumer) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(from);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR_OF_DAY, 0);
					c.set(Calendar.DAY_OF_YEAR, 1);

					long t;
					while ((t = c.getTimeInMillis()) < to) {
						consumer.accept(t);
						c.add(Calendar.YEAR, 1);
					}
				}
			},
			// decades
			new Level(3650 * D, 36500 * D, "yyyy's'", "G") {
				@Override
				public void listTimestamps(long from, long to, Consumer<Long> consumer) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(from);
					c.set(Calendar.MILLISECOND, 0);
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MINUTE, 0);
					c.set(Calendar.HOUR_OF_DAY, 24);
					c.set(Calendar.DAY_OF_YEAR, 1);
					c.set(Calendar.YEAR, c.get(Calendar.YEAR) / 10 * 10);

					long t;
					while ((t = c.getTimeInMillis()) < to) {
						consumer.accept(t);
						c.add(Calendar.YEAR, 10);
					}
				}
			}
	};

	/**
	 * Finds an appropriate level of delimiters to be used with a specific span
	 * of the timeline. Lower levels are denser, and therefore the value of this
	 * method minus 1 is the level for secondary (unmarked) delimiters (if any).
	 *
	 * @param length span of timeline in milliseconds
	 * @return the level to be passed to {@code getLevel(int))}
	 */
	public static int getPrimaryLevel(long length) {
		for (int i = 0; i < LEVELS.length; i++) {
			if (length < LEVELS[i].maxLength) return i;
		}
		return LEVELS.length - 1;
	}

	/**
	 * Returns a specific {@link Level} or null if outside defined levels list
	 */
	public static Level getLevel(int level) {
		if (level < 0 || level >= LEVELS.length) return null;
		return LEVELS[level];
	}

	public static class Level {
		private final long intervalEstimate;
		private final long maxLength;
		private final DateFormat delimiterFormat;
		private final DateFormat statusFormat;

		/**
		 * @param intervalEstimate an estimate of the span between consecutive delimiters of this level
		 * @param maxLength        defines the largest span a timeline can have to use this delimiters level
		 * @param delimiterFormat  {@link SimpleDateFormat} pattern to mark each delimiter or null
		 * @param statusFormat     {@link SimpleDateFormat} pattern to display the general period of the timeline span or null
		 */
		public Level(long intervalEstimate, long maxLength, String delimiterFormat, String statusFormat) {
			this.intervalEstimate = intervalEstimate;
			this.maxLength = maxLength;
			this.delimiterFormat = delimiterFormat == null ? null : new SimpleDateFormat(delimiterFormat);
			this.statusFormat = statusFormat == null ? null : new SimpleDateFormat(statusFormat);
		}

		/**
		 * For each delimiter of this type throughout {@code from} to {@code to} (in UNIX
		 * milliseconds), call {@code consumer} with the timestamp of the delimiter. By default,
		 * an equally spaced set of timestamps is used defined by {@code intervalEstimate}.
		 */
		public void listTimestamps(long from, long to, Consumer<Long> consumer) {
			long first = from - from % intervalEstimate + intervalEstimate;
			if (from < 0) first -= intervalEstimate;
			if (first > to) return;

			int n = (int) ((to - first) / intervalEstimate);
			for (int i = 0; i <= n; i++) consumer.accept(first + i * intervalEstimate);
		}

		public String formatDelimiter(long t) {
			return delimiterFormat == null ? "" : delimiterFormat.format(t);
		}

		public String formatStatus(long t) {
			return statusFormat == null ? "" : statusFormat.format(t);
		}
	}
}
