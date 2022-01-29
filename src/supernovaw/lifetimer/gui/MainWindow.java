package supernovaw.lifetimer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainWindow extends JFrame {
	public static int screenRefreshRate;

	public MainWindow() {
		DisplayMode screenInfo = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		screenRefreshRate = screenInfo.getRefreshRate();
		setSize(screenInfo.getWidth() * 3 / 5, screenInfo.getHeight() * 2 / 3);
		setLocationRelativeTo(null);
		addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				exitProgram();
			}
		});
		setLayout(null);

		Timeline tl = new Timeline();
		add(tl);
		tl.setBounds(100, 100, 1000, 200);
	}

	public void exitProgram() {
		System.exit(0);
	}

	public static double easeFunction(double x) {
		if (x <= 0 || x >= 1) return x;
		return Math.sin(x * Math.PI / 2);
	}

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
