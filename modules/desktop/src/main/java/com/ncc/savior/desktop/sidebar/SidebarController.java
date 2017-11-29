package com.ncc.savior.desktop.sidebar;

import java.util.List;

import com.ncc.savior.desktop.virtues.VirtueDto;
import com.ncc.savior.desktop.virtues.VirtueDto.VirtueDtoComparator;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.VirtueState;

import javafx.stage.Stage;

public class SidebarController {

	private VirtueService virtueService;
	private Sidebar sidebar;
	private Thread virtuePollThread;
	private boolean terminatePollThread = false;
	private List<VirtueDto> currentVirtues;
	private long pollPeriodMillis = 500;
	private VirtueChangeHandler changeHandler;

	public SidebarController(VirtueService virtueService, Sidebar sidebar) {
		this.sidebar = sidebar;
		this.changeHandler = sidebar;
		this.virtueService = virtueService;
	}

	public void init(Stage primaryStage) throws Exception {
		List<VirtueDto> initialVirtues = virtueService.getVirtuesForUser();
		currentVirtues = initialVirtues;
		sidebar.start(primaryStage, initialVirtues);

		startVirtuePoll();
	}

	private void startVirtuePoll() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				while (!terminatePollThread) {
					List<VirtueDto> virtues = virtueService.getVirtuesForUser();
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

	protected void detectChangesAndReport(List<VirtueDto> currentVirtues, List<VirtueDto> virtues) {
		VirtueDtoComparator comparator = new VirtueDto.VirtueDtoComparator();
		currentVirtues.sort(comparator);
		virtues.sort(comparator);
		int cindex = 0;
		int nindex = 0;

		if (currentVirtues.isEmpty()) {
			for (VirtueDto virtue : virtues) {
				reportAddedVirtue(virtue);
			}
			return;
		}

		if (virtues.isEmpty()) {
			for (VirtueDto virtue : currentVirtues) {
				reportRemovedVirtue(virtue);
			}
			return;
		}

		while ((cindex < currentVirtues.size() && nindex < virtues.size())) {
			VirtueDto cv = currentVirtues.get(cindex);
			VirtueDto nv = virtues.get(nindex);
			int compare = cv.getId().compareTo(nv.getId());
			if (0 == compare) {
				if (!cv.getName().equals(nv.getName()) || detectStatusChange(cv, nv)) {
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

	private boolean detectStatusChange(VirtueDto cv, VirtueDto nv) {
		VirtueState cs = cv.getStatus();
		VirtueState ns = nv.getStatus();
		if (cs == null) {
			if (ns == null) {
				return false;
			}else {
			return !ns.equals(cs);
			}
		}else {
			return !cs.equals(ns);
		}

	}

	protected void reportRemovedVirtue(VirtueDto virtue) {
		changeHandler.removeVirtue(virtue);
	}

	protected void reportAddedVirtue(VirtueDto virtue) {
		changeHandler.addVirtue(virtue);
	}

	protected void reportChangedVirtue(VirtueDto virtue) {
		changeHandler.changeVirtue(virtue);
	}

	public static interface VirtueChangeHandler {

		void removeVirtue(VirtueDto virtue);

		void changeVirtue(VirtueDto virtue);

		void addVirtue(VirtueDto virtue);

	}

	public void setVirtueChangeHandler(VirtueChangeHandler vch) {
		this.changeHandler = vch;
	}

}
