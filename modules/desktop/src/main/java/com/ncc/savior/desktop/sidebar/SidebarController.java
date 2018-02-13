package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue.DesktopVirtueComparator;

import javafx.stage.Stage;

public class SidebarController {
	private static final Logger logger = LoggerFactory.getLogger(SidebarController.class);
	private VirtueService virtueService;
	private Sidebar sidebar;
	private Thread virtuePollThread;
	private boolean terminatePollThread = false;
	private List<DesktopVirtue> currentVirtues;
	private long pollPeriodMillis = 500;
	private VirtueChangeHandler changeHandler;
	private AuthorizationService authService;

	public SidebarController(VirtueService virtueService, Sidebar sidebar, AuthorizationService authService) {
		this.sidebar = sidebar;
		this.changeHandler = sidebar;
		this.virtueService = virtueService;
		this.authService = authService;
	}

	public void init(Stage primaryStage) throws Exception {
		List<DesktopVirtue> initialVirtues;
		if (authService.getUser() != null) {
			initialVirtues = virtueService.getVirtuesForUser();
		} else {
			initialVirtues = new ArrayList<DesktopVirtue>();
		}
		currentVirtues = initialVirtues;
		sidebar.start(primaryStage, initialVirtues);
		startVirtuePoll();
	}

	private void startVirtuePoll() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (!terminatePollThread) {
					try {
						if (authService.getUser() != null) {
							List<DesktopVirtue> virtues;
							try {

								virtues = virtueService.getVirtuesForUser();
							} catch (IOException e1) {
								// TODO do something with connection errors.
								virtues = new ArrayList<DesktopVirtue>(0);
							}
							detectChangesAndReport(currentVirtues, virtues);
							currentVirtues = virtues;
						}

						Thread.sleep(pollPeriodMillis);
					} catch (Throwable t) {
						logger.error("error in virtue polling thread", t);
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

		while ((cindex < currentVirtues.size() && nindex < virtues.size())) {
			DesktopVirtue cv = currentVirtues.get(cindex);
			DesktopVirtue nv = virtues.get(nindex);
			int compare = comparator.compare(cv, nv);
			boolean updatedId = false;
			if (compare != 0) {
				if (cv.getId() != null && nv.getId() == null) {
					cv.setId(nv.getId());
					updatedId = true;
					compare = 1;
				} else if (cv.getId() == null && nv.getId() != null) {
					cv.setId(nv.getId());
					updatedId = true;
					compare = 0;
				}
			}
			if (0 == compare) {
				if (!cv.getName().equals(nv.getName()) || updatedId) {
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

		// when one of the lists has been fulled processed, we must process the rest on
		// the other list.
		while (cindex < currentVirtues.size()) {
			DesktopVirtue v = currentVirtues.get(cindex);
			reportRemovedVirtue(v);
			cindex++;
		}
		while (nindex < virtues.size()) {
			DesktopVirtue v = virtues.get(nindex);
			reportAddedVirtue(v);
			nindex++;
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
