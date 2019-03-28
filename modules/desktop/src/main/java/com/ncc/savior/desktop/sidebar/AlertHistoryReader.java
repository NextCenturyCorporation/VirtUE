/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class AlertHistoryReader {
	private static final String CSV_FILE_PATH = "./alerts.csv";
	private static ImageIcon saviorIcon = new ImageIcon(AlertHistoryReader.class.getResource("/images/saviorLogo.png"));

	public static void displayAlerts(JFrame frame) throws IOException {
		JDialog dialog = new JDialog();
		dialog.setTitle("Alerts");
		dialog.setIconImage(saviorIcon.getImage());
		JScrollPane sp = new JScrollPane();
		sp.getVerticalScrollBar().setUnitIncrement(16);
		JPanel container = new JPanel();

		try (Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH));
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader()
	                    .withIgnoreHeaderCase()
						.withTrim());) {

			List<CSVRecord> records = csvParser.getRecords();

			if (records.size() == 0) {
				container.setLayout(new BorderLayout());
				container.setBackground(Color.WHITE);

				JLabel prompt = new JLabel("No alerts!", JLabel.CENTER);
				prompt.setVerticalAlignment(SwingConstants.CENTER);
				container.add(prompt, BorderLayout.CENTER);
			} else {
				Collections.reverse(records);

				container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

				for (CSVRecord csvRecord : records) {
					String message = csvRecord.get(0);
					String date = csvRecord.get(1);

					JPanel errorPanel = new JPanel(new BorderLayout());
					errorPanel.setMinimumSize(new Dimension(1, 80));
					errorPanel.setPreferredSize(new Dimension(1, 80));
					errorPanel.setBackground(Color.WHITE);
					errorPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
					JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>", JLabel.CENTER);
					JLabel dateLabel = new JLabel(date);
					messageLabel.setVerticalAlignment(SwingConstants.CENTER);
					dateLabel.setVerticalAlignment(SwingConstants.CENTER);

					errorPanel.add(messageLabel, BorderLayout.CENTER);
					errorPanel.add(dateLabel, BorderLayout.SOUTH);
					container.add(errorPanel);
				}
			}
		}

		sp.setViewportView(container);
		dialog.add(sp);
		addListener(dialog);
		dialog.setSize(new Dimension(400, 250));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private static void addListener(JDialog dialog) {
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent arg0) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
	}
}