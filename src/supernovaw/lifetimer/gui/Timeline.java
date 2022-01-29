package supernovaw.lifetimer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Timeline extends JComponent {
	private static final int DELIMITER_TRANSITION_DURATION = 150;
	private static final DateFormat DEBUG_FORMAT = new SimpleDateFormat("G yyyy-MM-dd HH:mm:ss");
	private final Renderer renderer = new Renderer();
	private final TimelineNavigator navigator = new TimelineNavigator();
	private int mouseX;
	private int delimitersLevel;

	private int prevDelimiterLevel;
	private long delimiterTransitionTriggerTime;
	private boolean isDelimiterTransitioning;

	public Timeline() {
		initListeners();
		new Timer(1000 / MainWindow.screenRefreshRate, e -> {
			if (navigator.isTransitioning()) {
				onZoomUpdated();
				repaint();
			} else if (isDelimiterTransitioning) {
				repaint();
			}
		}).start();
		delimitersLevel = TimelineDelimiter.getPrimaryLevel(navigator.end() - navigator.start());
	}

	private void initListeners() {
		MouseAdapter mouseAdapter = new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				repaint();
			}

			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}

			public void mouseWheelMoved(MouseWheelEvent e) {
				navigator.zoom(e.getX(), e.getPreciseWheelRotation());
				repaint();
			}
		};
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				navigator.pixelWidth = getWidth();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getBackground().darker());
		g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
		g2d.drawString(DEBUG_FORMAT.format(navigator.getTimestamp(mouseX)), getWidth() - 150, getHeight() - 5);

		renderer.paint(g2d);
	}


	private void onZoomUpdated() {
		int lvl = TimelineDelimiter.getPrimaryLevel(navigator.end() - navigator.start());
		if (lvl != delimitersLevel) {
			prevDelimiterLevel = delimitersLevel;
			delimitersLevel = lvl;
			delimiterTransitionTriggerTime = System.currentTimeMillis();
			isDelimiterTransitioning = true;
		}
	}

	private class Renderer {
		// variables effective as of paint(Graphics2D) call to access from different methods
		private Graphics2D g;
		private int fontSize;
		private long start, end, mouseTimestamp;
		private int timelineBaseY;
		private TimelineDelimiter.Level primaryLevel, secondaryLevel; // secondary is nullable
		private TimelineDelimiter.Level prevPrimaryLevel, prevSecondaryLevel;

		void paint(Graphics2D g) {
			// assign variables
			this.g = g;
			fontSize = g.getFont().getSize();
			start = navigator.start();
			end = navigator.end();
			mouseTimestamp = navigator.getTimestamp(mouseX);
			timelineBaseY = getHeight() - 30;
			primaryLevel = TimelineDelimiter.getLevel(delimitersLevel);
			secondaryLevel = TimelineDelimiter.getLevel(delimitersLevel - 1);
			if (isDelimiterTransitioning) {
				prevPrimaryLevel = TimelineDelimiter.getLevel(prevDelimiterLevel);
				prevSecondaryLevel = TimelineDelimiter.getLevel(prevDelimiterLevel - 1);
			}

			paintTimeline();
		}

		void paintTimeline() {
			paintDelimiters();
			g.setColor(getForeground());
			g.drawLine(mouseX, 0, mouseX, getHeight() - 2);
			g.setColor(getForeground().darker());
			g.drawString(primaryLevel.formatStatus(mouseTimestamp), 10, timelineBaseY + fontSize + 10);
		}

		void paintDelimiters() {
			if (isDelimiterTransitioning) {
				long t = System.currentTimeMillis();
				t -= delimiterTransitionTriggerTime;
				if (t >= DELIMITER_TRANSITION_DURATION) {
					isDelimiterTransitioning = false;
					paintDelimitersStatic();
				} else {
					double phase = MainWindow.easeFunction((double) t / DELIMITER_TRANSITION_DURATION);
					paintDelimitersTransitioning((float) phase);
				}
			} else {
				paintDelimitersStatic();
			}
		}

		void paintDelimitersTransitioning(float phase) {
			Composite savedComposite = g.getComposite();

			// paint the previous delimiter level (fade out)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - phase));
			g.setColor(getForeground().darker().darker());
			paintSecondaryDelimiters(prevSecondaryLevel);
			g.setColor(getForeground());
			paintPrimaryDelimiters(prevPrimaryLevel);

			// paint the new delimiter level (fade in)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, phase));
			g.setColor(getForeground().darker().darker());
			paintSecondaryDelimiters(secondaryLevel);
			g.setColor(getForeground());
			paintPrimaryDelimiters(primaryLevel);

			g.setComposite(savedComposite);
		}

		void paintDelimitersStatic() {
			g.setColor(getForeground().darker().darker());
			paintSecondaryDelimiters(secondaryLevel);

			g.setColor(getForeground());
			paintPrimaryDelimiters(primaryLevel);
		}

		void paintPrimaryDelimiters(TimelineDelimiter.Level level) {
			level.listTimestamps(start, end, t -> {
				int x = navigator.getX(t);
				g.drawLine(x, timelineBaseY - 4, x, timelineBaseY + 4);
				String text = level.formatDelimiter(t);
				g.drawString(text, x + 3, timelineBaseY - 8);
			});
		}

		void paintSecondaryDelimiters(TimelineDelimiter.Level level) {
			if (level == null) return;
			level.listTimestamps(start, end, t -> {
				int x = navigator.getX(t);
				g.drawLine(x, timelineBaseY - 2, x, timelineBaseY + 2);
			});
		}
	}
}
