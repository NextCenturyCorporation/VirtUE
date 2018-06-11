package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue.DesktopVirtueComparator;

public class SidebarController {
	private static final Logger logger = LoggerFactory.getLogger(SidebarController.class);
	private VirtueService virtueService;
	private Sidebar sidebar;
	private Thread virtuePollThread;
	private boolean terminatePollThread = false;
	protected Map<String, DesktopVirtue> currentVirtues;
	private long pollPeriodMillis = 2500;
	private VirtueChangeHandler changeHandler;
	private AuthorizationService authService;

	public SidebarController(VirtueService virtueService, Sidebar sidebar, AuthorizationService authService) {
		this.sidebar = sidebar;
		this.changeHandler = sidebar;
		this.virtueService = virtueService;
		this.authService = authService;
		this.currentVirtues = new TreeMap<String, DesktopVirtue>();
	}

	public void init(JFrame primaryStage) throws Exception {
		List<DesktopVirtue> initialVirtues;
		// if (authService.getUser() != null) {
		// initialVirtues = virtueService.getVirtuesForUser();
		// } else {
		initialVirtues = new ArrayList<DesktopVirtue>();
		// }
		currentVirtues = getCurrentVirtueMap(initialVirtues);

		sidebar.start(primaryStage, initialVirtues);
		startVirtuePoll();

		// sidebar.setStartState();
	}

	private Map<String, DesktopVirtue> getCurrentVirtueMap(List<DesktopVirtue> initialVirtues) {
		Map<String, DesktopVirtue> map = new TreeMap<String, DesktopVirtue>();
		for (DesktopVirtue v : initialVirtues) {
			String key = getMapKey(v);
			map.put(key, v);
		}
		return map;
	}

	private String getMapKey(DesktopVirtue v) {
		if (v.getId() == null || v.getId().trim().equals("")) {
			return v.getTemplateId();
		}
		return v.getTemplateId() + "-" + v.getId();
	}

	// ****************
	public void startVirtuePoll() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				String previousUser = null;
				while (!terminatePollThread) {
					try {
						DesktopUser currentUser = authService.getUser();
						// DesktopUser currentUser = new DesktopUser("dummy", "");
						if (currentUser != null) {
							List<DesktopVirtue> virtues;
							try {

								virtues = virtueService.getVirtuesForUser();
							} catch (IOException e1) {
								// TODO do something with connection errors.
								virtues = new ArrayList<DesktopVirtue>(0);
							}
							if (!currentUser.getUsername().equals(previousUser)) {
								currentVirtues.clear();
								previousUser = currentUser.getUsername();
							}
							updateVirtues(virtues);

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

	protected void updateVirtues(List<DesktopVirtue> virtues) throws IOException {
		Iterator<DesktopVirtue> itr = virtues.iterator();
		Map<String, DesktopVirtue> newCurrentVirtues = new TreeMap<String, DesktopVirtue>();
		while (itr.hasNext()) {
			DesktopVirtue v = itr.next();
			String key = getMapKey(v);
			if (currentVirtues.containsKey(key)) {
				currentVirtues.remove(key);
				newCurrentVirtues.put(getMapKey(v), v);
				reportChangedVirtue(v);
			} else if (currentVirtues.containsKey(v.getTemplateId())) {
				currentVirtues.remove(v.getTemplateId());
				newCurrentVirtues.put(getMapKey(v), v);
				reportChangedVirtue(v);
			} else {
				reportAddedVirtue(v);
				newCurrentVirtues.put(getMapKey(v), v);
			}
		}

		Iterator<DesktopVirtue> removeItr = currentVirtues.values().iterator();
		while (removeItr.hasNext()) {
			DesktopVirtue v = removeItr.next();
			reportRemovedVirtue(v);
			removeItr.remove();
		}

		currentVirtues = newCurrentVirtues;
	}

	// TODO this still has bugs and should be completely rethought
	protected void detectChangesAndReport2(List<DesktopVirtue> currentVirtues, List<DesktopVirtue> virtues)
			throws IOException {
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
				if (!cv.getName().equals(nv.getName()) || updatedId
						|| !cv.getVirtueState().equals(nv.getVirtueState())) {
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

	protected void reportAddedVirtue(DesktopVirtue virtue) throws IOException {
		changeHandler.addVirtue(virtue);
	}

	protected void reportChangedVirtue(DesktopVirtue virtue) {
		changeHandler.changeVirtue(virtue);
	}

	public static interface VirtueChangeHandler {

		void removeVirtue(DesktopVirtue virtue);

		void changeVirtue(DesktopVirtue virtue);

		void addVirtue(DesktopVirtue virtue) throws IOException;

	}

	public void setVirtueChangeHandler(VirtueChangeHandler vch) {
		this.changeHandler = vch;
	}

}
