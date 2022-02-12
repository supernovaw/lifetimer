package supernovaw.lifetimer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

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
		initMenuBar();

		Timeline tl = new Timeline();
		add(tl);
		tl.setBounds(100, 100, 1000, 200);
	}

	private void initMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu groupDay = menuBar.add(new JMenu("Day"));
		JMenu groupCurrent = menuBar.add(new JMenu("Current"));
		JMenu groupPast = menuBar.add(new JMenu("Past"));
		JMenu groupEdit = menuBar.add(new JMenu("Edit"));
		groupDay.setMnemonic(KeyEvent.VK_D);
		groupCurrent.setMnemonic(KeyEvent.VK_C);
		groupPast.setMnemonic(KeyEvent.VK_S);
		groupEdit.setMnemonic(KeyEvent.VK_E);

		JMenuItem itemDayCreate = groupDay.add(new JMenuItem("Create"));
		JMenuItem itemDayEdit = groupDay.add(new JMenuItem("Edit"));
		JMenuItem itemDayFinish = groupDay.add(new JMenuItem("Finish"));
		JMenuItem itemDayContinue = groupDay.add(new JMenuItem("Continue"));
		JMenuItem itemDayRemove = groupDay.add(new JMenuItem("Remove"));
		itemDayCreate.setMnemonic(KeyEvent.VK_A);
		itemDayEdit.setMnemonic(KeyEvent.VK_E);
		itemDayFinish.setMnemonic(KeyEvent.VK_F);
		itemDayContinue.setMnemonic(KeyEvent.VK_C);
		itemDayRemove.setMnemonic(KeyEvent.VK_R);

		JMenuItem itemCurrentStart = groupCurrent.add(new JMenuItem("Start activity"));
		JMenuItem itemCurrentInterrupt = groupCurrent.add(new JMenuItem("Interrupt"));
		JMenuItem itemCurrentFinish = groupCurrent.add(new JMenuItem("Finish"));
		JMenuItem itemCurrentEdit = groupCurrent.add(new JMenuItem("Edit"));
		itemCurrentStart.setMnemonic(KeyEvent.VK_A);
		itemCurrentInterrupt.setMnemonic(KeyEvent.VK_R);
		itemCurrentFinish.setMnemonic(KeyEvent.VK_F);
		itemCurrentEdit.setMnemonic(KeyEvent.VK_E);

		JMenuItem itemPastStart = groupPast.add(new JMenuItem("Start activity"));
		JMenuItem itemPastInterrupt = groupPast.add(new JMenuItem("Interrupt"));
		JMenuItem itemPastInsertActivity = groupPast.add(new JMenuItem("Insert activity"));
		JMenuItem itemPastInsertInterruption = groupPast.add(new JMenuItem("Insert interruption"));
		JMenuItem itemPastMove = groupPast.add(new JMenuItem("Move"));
		JMenuItem itemPastEdit = groupPast.add(new JMenuItem("Edit"));
		itemPastStart.setMnemonic(KeyEvent.VK_A);
		itemPastInterrupt.setMnemonic(KeyEvent.VK_R);
		itemPastInsertActivity.setMnemonic(KeyEvent.VK_S);
		itemPastInsertInterruption.setMnemonic(KeyEvent.VK_T);
		itemPastMove.setMnemonic(KeyEvent.VK_V);
		itemPastEdit.setMnemonic(KeyEvent.VK_E);

		JMenuItem itemEditCurrentConfiguration = groupEdit.add(new JMenuItem("Current configuration"));
		JMenuItem itemEditConfigurationsList = groupEdit.add(new JMenuItem("Configurations list"));
		itemEditCurrentConfiguration.setMnemonic(KeyEvent.VK_C);
		itemEditConfigurationsList.setMnemonic(KeyEvent.VK_S);
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
