/*******************************************************************************
 * Copyright (c) 2001, 2008 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Matthew Reeder
 *     - Added keyboard mnemonics to buttons
 *     Flemming N. Larsen
 *     - Code cleanup
 *     - Changed the F5 key press for refreshing the list of available robots
 *       into 'modifier key' + R to comply with other OSes like e.g. Mac OS
 *******************************************************************************/
package robocode.dialog;


import static robocode.ui.ShortcutUtil.MENU_SHORTCUT_KEY_MASK;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import robocode.manager.RobocodeManager;
import robocode.manager.RobotRepositoryManager;
import robocode.repository.TeamSpecification;


/**
 * @author Mathew A. Nelson (original)
 * @author Matthew Reeder (contributor)
 * @author Flemming N. Larsen (contributor)
 */
@SuppressWarnings("serial")
public class TeamCreator extends JDialog implements WizardListener {

	private JPanel teamCreatorContentPane;

	private WizardCardPanel wizardPanel;
	private WizardController wizardController;

	private RobotSelectionPanel robotSelectionPanel;
	private TeamCreatorOptionsPanel teamCreatorOptionsPanel;

	private int minRobots = 2;
	private int maxRobots = 10;

	private RobotRepositoryManager robotRepositoryManager;
	private RobocodeManager manager;

	private EventHandler eventHandler = new EventHandler();

	class EventHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Refresh")) {
				getRobotSelectionPanel().refreshRobotList();
			}
		}
	}

	public TeamCreator(RobotRepositoryManager robotRepositoryManager) {
		super(robotRepositoryManager.getManager().getWindowManager().getRobocodeFrame());
		this.robotRepositoryManager = robotRepositoryManager;
		this.manager = robotRepositoryManager.getManager();
		initialize();
	}

	protected TeamCreatorOptionsPanel getTeamCreatorOptionsPanel() {
		if (teamCreatorOptionsPanel == null) {
			teamCreatorOptionsPanel = new TeamCreatorOptionsPanel(this);
		}
		return teamCreatorOptionsPanel;
	}

	private JPanel getTeamCreatorContentPane() {
		if (teamCreatorContentPane == null) {
			teamCreatorContentPane = new JPanel();
			teamCreatorContentPane.setLayout(new BorderLayout());
			teamCreatorContentPane.add(getWizardController(), BorderLayout.SOUTH);
			teamCreatorContentPane.add(getWizardPanel(), BorderLayout.CENTER);
			getWizardPanel().getWizardController().setFinishButtonTextAndMnemonic("Create Team!", 'C', 0);
			teamCreatorContentPane.registerKeyboardAction(eventHandler, "Refresh",
					KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_SHORTCUT_KEY_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			teamCreatorContentPane.registerKeyboardAction(eventHandler, "Refresh",
					KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_SHORTCUT_KEY_MASK), JComponent.WHEN_FOCUSED);
		}
		return teamCreatorContentPane;
	}

	/**
	 * Return the Page property value.
	 *
	 * @return JPanel
	 */
	protected RobotSelectionPanel getRobotSelectionPanel() {
		if (robotSelectionPanel == null) {
			robotSelectionPanel = new RobotSelectionPanel(robotRepositoryManager, minRobots, maxRobots, false,
					"Select the robots for this team.", false, true, true, false, false, false, null);
		}
		return robotSelectionPanel;
	}

	/**
	 * Return the tabbedPane.
	 *
	 * @return JTabbedPane
	 */
	private WizardCardPanel getWizardPanel() {
		if (wizardPanel == null) {
			wizardPanel = new WizardCardPanel(this);
			wizardPanel.add(getRobotSelectionPanel(), "Select robots");
			wizardPanel.add(getTeamCreatorOptionsPanel(), "Select options");
		}
		return wizardPanel;
	}

	public void initialize() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Create a team");
		setContentPane(getTeamCreatorContentPane());
	}

	private WizardController getWizardController() {
		if (wizardController == null) {
			wizardController = getWizardPanel().getWizardController();
		}
		return wizardController;
	}

	public void cancelButtonActionPerformed() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	public void finishButtonActionPerformed() {
		try {
			int rc = createTeam();

			if (rc == 0) {
				JOptionPane.showMessageDialog(this, "Team created successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE, null);
				this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Team creation cancelled", "Cancelled",
						JOptionPane.INFORMATION_MESSAGE, null);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.toString(), "Team Creation Failed", JOptionPane.ERROR_MESSAGE, null);
		}
	}

	public int createTeam() throws IOException {
		File f = new File(robotRepositoryManager.getRobotsDirectory(),
				teamCreatorOptionsPanel.getTeamPackage().replace('.', File.separatorChar)
				+ teamCreatorOptionsPanel.getTeamNameField().getText() + ".team");

		if (f.exists()) {
			int ok = JOptionPane.showConfirmDialog(this, f + " already exists.  Are you sure you want to replace it?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION);

			if (ok == JOptionPane.NO_OPTION || ok == JOptionPane.CANCEL_OPTION) {
				return -1;
			}
		}
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}

		TeamSpecification teamSpec = new TeamSpecification();
		URL u = null;
		String w = teamCreatorOptionsPanel.getWebpageField().getText();

		if (w != null && w.length() > 0) {
			try {
				u = new URL(w);
			} catch (MalformedURLException e) {
				try {
					u = new URL("http://" + w);
					teamCreatorOptionsPanel.getWebpageField().setText(u.toString());
				} catch (MalformedURLException e2) {}
			}
		}
		teamSpec.setTeamWebpage(u);
		teamSpec.setTeamDescription(teamCreatorOptionsPanel.getDescriptionArea().getText());
		teamSpec.setTeamAuthorName(teamCreatorOptionsPanel.getAuthorField().getText());
		teamSpec.setMembers(robotSelectionPanel.getSelectedRobotsAsString());
		teamSpec.setRobocodeVersion(manager.getVersionManager().getVersion());

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			teamSpec.store(out, "Robocode robot team");
		} finally {
			if (out != null) {
				out.close();
			}
		}

		robotRepositoryManager.clearRobotList();

		return 0;
	}
}
