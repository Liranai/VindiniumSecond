
package vindinium;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import lombok.Getter;

@Getter
public class DebugWindow extends JFrame {

	private static final long serialVersionUID = -2942201604598924346L;
	private StatePanel statePanel;

	public DebugWindow () {
		super("MainFrame");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setPreferredSize(new Dimension(960, 1080));

		statePanel = new StatePanel();

		add(statePanel, BorderLayout.CENTER);
		pack();
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public void repaint (State state) {
		statePanel.drawState(state);
	}
}
