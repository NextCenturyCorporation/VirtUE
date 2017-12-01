package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue.DesktopVirtueComparator;

import javafx.stage.Stage;

public class SidebarController {

	private VirtueService virtueService;
	private Sidebar sidebar;
	private Thread virtuePollThread;
	private boolean terminatePollThread = false;
	private List<DesktopVirtue> currentVirtues;
	private long pollPeriodMillis = 500;
	private VirtueChangeHandler changeHandler;

	public SidebarController(VirtueService virtueService, Sidebar sidebar) {
		this.sidebar = sidebar;
		this.changeHandler = sidebar;
		this.virtueService = virtueService;
	}

	public void init(Stage primaryStage) throws Exception {
		List<DesktopVirtue> initialVirtues = virtueService.getVirtuesForUser();
		currentVirtues = initialVirtues;
		sidebar.start(primaryStage, initialVirtues);

		startVirtuePoll();
	}

	private void startVirtuePoll() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (!terminatePollThread) {
					List<DesktopVirtue> virtues;
					try {
						virtues = virtueService.getVirtuesForUser();
					} catch (IOException e1) {
						// TODO do something with connection errors.
						virtues = new ArrayList<DesktopVirtue>(0);
					}
					detectChangesAndReport(currentVirtues, virtues);
					currentVirtues = virtues;
					try {
						Thread.sleep(pollPeriodMillis);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		virtuePollThread = new Thread(runnable, "VirtuePollThread");
		virtuePollThread.setDaemon(true);
		virtuePollThread.start();
	}

	protected void detectChangesAndReport(List<DesktopVirtue> currentVirtues, List<DesktopVirtue> virtues) {
		DesktopVirtueComparator comparator = new DesktopVirtue.DesktopVirtueComparator();
		currentVirtues.sort(comparator);
		virtues.sort(comparator);
		int cindex = 0;
		int nindex = 0;

		if (currentVirtues.isEmpty()) {
			for (DesktopVirtue virtue : virtues) {
				reportAddedVirtue(virtue);
			}
			return;
		}

		if (virtues.isEmpty()) {
			for (DesktopVirtue virtue : currentVirtues) {
				reportRemovedVirtue(virtue);
			}
			return;
		}

		while ((cindex < currentVirtues.size() && nindex < virtues.size())) {
			DesktopVirtue cv = currentVirtues.get(cindex);
			DesktopVirtue nv = virtues.get(nindex);
			int compare;
			if (cv.getId() != null) {
				// if current virtue doesn't have an ID, a new one with an ID could override it.
				compare = cv.getTemplateId().compareTo(nv.getTemplateId());
				if (compare == 0) {
					// Alter current such that subsequence virtues matching the template will be
					// added.
					cv.setId(nv.getId());
				}
			} else {
				compare = nv.getId() == null ? 0 : -1;
			}
			if (0 == compare) {
				if (!cv.getName().equals(nv.getName())) {
					reportChangedVirtue(nv);
				}
				cindex++;
				nindex++;
			} else if (0 < compare) {
				reportAddedVirtue(nv);
				nindex++;
			} else if (0 > compare) {
				reportRemovedVirtue(cv);
				cindex++;
			}
		}
	}

	// private boolean detectStatusChange(DesktopVirtue cv, DesktopVirtue nv) {
	// VirtueState cs = cv.getStatus();
	// VirtueState ns = nv.getStatus();
	// if (cs == null) {
	// if (ns == null) {
	// return false;
	// } else {
	// return !ns.equals(cs);
	// }
	// } else {
	// return !cs.equals(ns);
	// }
	//
	// }

	protected void reportRemovedVirtue(DesktopVirtue virtue) {
		changeHandler.removeVirtue(virtue);
	}

	protected void reportAddedVirtue(DesktopVirtue virtue) {
		changeHandler.addVirtue(virtue);
	}

	protected void reportChangedVirtue(DesktopVirtue virtue) {
		changeHandler.changeVirtue(virtue);
	}

	public static interface VirtueChangeHandler {

		void removeVirtue(DesktopVirtue virtue);

		void changeVirtue(DesktopVirtue virtue);

		void addVirtue(DesktopVirtue virtue);

	}

	public void setVirtueChangeHandler(VirtueChangeHandler vch) {
		this.changeHandler = vch;
	}

}
