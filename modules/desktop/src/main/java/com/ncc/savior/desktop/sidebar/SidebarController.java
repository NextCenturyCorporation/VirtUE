package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.AuthorizationService.ILoginListener;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue.DesktopVirtueComparator;

/**
 *
 * This class provides functionality for retrieving and updating virtues
 *
 */

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
		registerAsListener();

		this.currentVirtues = new TreeMap<String, DesktopVirtue>();
	}

	public void registerAsListener() {
		authService.addLoginListener(new ILoginListener() {

			@Override
			public void onLogin() {
				startVirtuePoll();
			}

			@Override
			public void onLogout() {
				stopVirtuePoll();
			}

		});
	}

	public void init(JFrame primaryFrame) throws Exception {
		List<DesktopVirtue> initialVirtues;
		// if (authService.getUser() != null) {
		// initialVirtues = virtueService.getVirtuesForUser();
		// } else {
		initialVirtues = new ArrayList<DesktopVirtue>();
		// }
		currentVirtues = getCurrentVirtueMap(initialVirtues);

		sidebar.start(primaryFrame, initialVirtues);
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
		// if (v.getId() == null || v.getId().trim().equals("")) {
		// return v.getTemplateId();
		// }
		// return v.getTemplateId() + "-" + v.getId();
		return v.getTemplateId();
	}

	// ****************
	public void startVirtuePoll() {
		terminatePollThread = false;
		currentVirtues.clear();

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				List<DesktopVirtue> virtueList = new ArrayList<DesktopVirtue>();
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

							if (!virtues.isEmpty()) {
								virtueList.addAll(virtues);
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

	public void stopVirtuePoll() {
		terminatePollThread = true;
	}

	protected void updateVirtues(List<DesktopVirtue> virtues) throws IOException {
		Iterator<DesktopVirtue> itr = virtues.iterator();
		List<DesktopVirtue> addedVirtues = new ArrayList<DesktopVirtue>();
		Map<String, DesktopVirtue> newCurrentVirtues = new TreeMap<String, DesktopVirtue>();
		if (logger.isTraceEnabled()) {
			logger.trace("current Virtues: (" + currentVirtues.size() + ") " + currentVirtues);
		}
		while (itr.hasNext()) {
			DesktopVirtue v = itr.next();
			String key = getMapKey(v);
			if (currentVirtues.containsKey(key)) {
				DesktopVirtue old = currentVirtues.remove(key);
				String newKey = getMapKey(v);
				newCurrentVirtues.put(newKey, v);
				if (virtueChanged(old, v)) {
					reportChangedVirtue(v);
				}
			} else if (currentVirtues.containsKey(v.getTemplateId())) {
				key = v.getTemplateId();
				DesktopVirtue old = currentVirtues.remove(key);
				newCurrentVirtues.put(getMapKey(v), v);
				if (virtueChanged(old, v)) {
					reportChangedVirtue(v);
				}
			} else {
				addedVirtues.add(v);
				newCurrentVirtues.put(getMapKey(v), v);
			}
		}

		if (!addedVirtues.isEmpty()) {
			reportAddedVirtues(addedVirtues);
		} else {
			reportNoAddedVirtues();
		}

		Iterator<DesktopVirtue> removeItr = currentVirtues.values().iterator();
		while (removeItr.hasNext()) {
			DesktopVirtue v = removeItr.next();
			reportRemovedVirtue(v);
			removeItr.remove();
		}

		currentVirtues = newCurrentVirtues;
	}

	private boolean virtueChanged(DesktopVirtue old, DesktopVirtue v) {
		return !old.equals(v);
	}

	// TODO this still has bugs and should be completely rethought
	protected void detectChangesAndReport2(List<DesktopVirtue> currentVirtues, List<DesktopVirtue> virtues)
			throws IOException {
		DesktopVirtueComparator comparator = new DesktopVirtue.DesktopVirtueComparator();
		List<DesktopVirtue> addedVirtues = new ArrayList<DesktopVirtue>();
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
				addedVirtues.add(nv);
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
			addedVirtues.add(v);
			nindex++;
		}
		reportAddedVirtues(addedVirtues);
	}

	protected void reportRemovedVirtue(DesktopVirtue virtue) {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("removing virtue " + virtue);
			}
			changeHandler.removeVirtue(virtue);
		} catch (Exception e) {
			logger.error("Error sending remove virtue event", e);
		}
	}

	protected void reportNoAddedVirtues() {
		try {
			if (logger.isTraceEnabled()) {
				logger.debug("no virtues to be added");
			}
			changeHandler.addNoVirtues();
		} catch (Exception e) {
			logger.error("Error sending remove virtue event", e);
		}
	}

	protected void reportAddedVirtues(List<DesktopVirtue> virtues) throws IOException {
		try {
			if (logger.isTraceEnabled()) {
				logger.debug("adding virtues " + virtues);
			}
			changeHandler.addVirtues(virtues);
		} catch (Exception e) {
			logger.error("Error sending remove virtue event", e);
		}
	}

	protected void reportChangedVirtue(DesktopVirtue virtue) {
		try {
			if (logger.isTraceEnabled()) {
				logger.debug("changing virtue " + virtue);
			}
			changeHandler.changeVirtue(virtue);
		} catch (Exception e) {
			logger.error("Error sending change virtue event", e);
		}
	}

	public static interface VirtueChangeHandler {

		void removeVirtue(DesktopVirtue virtue);

		void changeVirtue(DesktopVirtue virtue);

		void addVirtues(List<DesktopVirtue> virtues) throws IOException, InterruptedException, ExecutionException;

		void addNoVirtues();

	}

	public void setVirtueChangeHandler(VirtueChangeHandler vch) {
		this.changeHandler = vch;
	}

}
