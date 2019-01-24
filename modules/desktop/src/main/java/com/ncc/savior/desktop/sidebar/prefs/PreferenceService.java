package com.ncc.savior.desktop.sidebar.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.AuthorizationService.ILoginListener;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.util.JavaUtil;

/**
 * Stores and manages preferences for the Savior Desktop application. All
 * preferences should be stores in the {@link DesktopPreference} enum.
 * Preferences are internally determined to be either collections or single
 * instances. Collections are groups of related preferences such as default
 * applications. The application can give any string key for a default
 * application and use that string for the element.
 *
 *
 */
public class PreferenceService {
	private static final Logger logger = LoggerFactory.getLogger(PreferenceService.class);
	private static final String PREFERENCE_DESKTOP_ROOT = "com/ncc/savior/desktop/";
	private String username;
	Map<DesktopPreference, DesktopPreferenceDetails> allPrefs;

	public PreferenceService(AuthorizationService authService) {
		allPrefs = new HashMap<DesktopPreference, DesktopPreferenceDetails>();
		allPrefs.put(DesktopPreference.FAVORITES, new DesktopPreferenceDetails("Favorite Applications",
				"Applications that have been favorited by the user", "/favorites", true));
		allPrefs.put(DesktopPreference.LAST_VIEW,
				new DesktopPreferenceDetails("Last View", "The last view set by the user.", "/lastView", false));
		allPrefs.put(DesktopPreference.LAST_SORT, new DesktopPreferenceDetails("Last Sort",
				"The last virtue/application sorting mechanism set by user", "/lastSort", false));
		allPrefs.put(DesktopPreference.DEFAULT_APPS, new DesktopPreferenceDetails("Default Applicaion - ",
				"IDs for virtue and application to use for default application.", "/defaultApps", true, true));

		authService.addLoginListener(new ILoginListener() {

			@Override
			public void onLogout() {
				username = null;
			}

			@Override
			public void onLogin(DesktopUser user) {
				try {
					username = authService.getUser().getUsername();
				} catch (InvalidUserLoginException e) {
					username = null;
				}
			}
		});
		try {
			this.username = authService.getUser().getUsername();
		} catch (InvalidUserLoginException e) {
			this.username = null;
		}
	}

	/**
	 * Clears all preferences from the internally determined base node for savior
	 * desktop for the current user.
	 *
	 * @throws BackingStoreException
	 */
	public void clearAllPreferences() throws BackingStoreException {
		verifyUsername();
		Preferences base = getBaseNode();
		recursiveClear(base);
	}

	/**
	 * Clears a specific preference. If the {@link DesktopPreference} given is a
	 * collection node, the element field will determine which element to clear. If
	 * not a collection node, element should be null.
	 *
	 * @param node
	 * @param element
	 * @throws BackingStoreException
	 */
	public void clearPreference(DesktopPreference node, String element) throws BackingStoreException {
		Preferences pref = getPreferenceNode(node, element);
		recursiveClear(pref);
	}

	/**
	 * Returns the {@link Preferences} node for the given node and if a collection
	 * preference, the collection element. If it is not a collection preference,
	 * collection element should be null.
	 *
	 * @param node
	 * @param collectionElement
	 * @return
	 */
	public Preferences getPreferenceNode(DesktopPreference node, String collectionElement) {
		verifyUsername();
		DesktopPreferenceDetails nodeDesc = allPrefs.get(node);
		if (nodeDesc.isNodeCollection() && collectionElement != null) {
			return Preferences.userRoot()
					.node(PREFERENCE_DESKTOP_ROOT + username + nodeDesc.getRoot() + "/" + collectionElement);
		} else {
			return Preferences.userRoot().node(PREFERENCE_DESKTOP_ROOT + username + nodeDesc.getRoot());
		}
	}

	/**
	 * Get preference node for a non-collection preference.
	 *
	 * @param node
	 * @return
	 */
	public Preferences getPreferenceNode(DesktopPreference node) {
		return getPreferenceNode(node, null);
	}

	/**
	 * Stores a new value for a preference. If it is not a collection preference,
	 * collection element should be null.
	 *
	 * @param node
	 * @param collectionElement
	 * @param key
	 * @param value
	 */
	public void put(DesktopPreference node, String collectionElement, String key, String value) {
		logger.trace("Put Pref " + node + " el=" + collectionElement + " key=" + key + " val=" + value);
		Preferences pref = getPreferenceNode(node, collectionElement);
		pref.put(key, value);
	}

	/**
	 * Returns the description or {@link DesktopPreferenceDetails} for a preference
	 * which contains more user field descriptions and names of a preference.
	 *
	 * @param node
	 * @return
	 */
	public DesktopPreferenceDetails getDescription(DesktopPreference node) {
		return allPrefs.get(node);
	}

	/**
	 * Returns all the child elements for a collection preference.
	 *
	 * @param node
	 * @return
	 * @throws BackingStoreException
	 */
	public String[] getCollectionElements(DesktopPreference node) throws BackingStoreException {
		return getPreferenceNode(node).childrenNames();
	}

	/**
	 * Get all the from the preference node matching the node and element.
	 *
	 * @param node
	 * @param element
	 * @return
	 */
	public List<DesktopPreferenceData> getPreferenceData(DesktopPreference node, String element) {
		Preferences pref = getPreferenceNode(node, element);
		return getData(pref);
	}

	/**
	 * Returns all {@link DesktopPreferenceData} from the applications base node.
	 *
	 * @return
	 * @throws BackingStoreException
	 */
	public List<DesktopPreferenceData> getAllActualPreferences() throws BackingStoreException {
		verifyUsername();
		Preferences base = getBaseNode();
		List<DesktopPreferenceData> list = new ArrayList<DesktopPreferenceData>();
		travelNode("", base, list);
		return list;
	}

	private void verifyUsername() {
		if (!JavaUtil.isNotEmpty(username)) {
			// TODO fix generic exception
			throw new RuntimeException("No username");
		}
	}

	private Preferences getBaseNode() {
		Preferences userBasePreference = Preferences.userRoot().node(PREFERENCE_DESKTOP_ROOT + username);
		return userBasePreference;
	}

	private List<DesktopPreferenceData> getData(Preferences userBasePreference) {
		try {
			String relativeNodePath = "";
			List<DesktopPreferenceData> vector = new ArrayList<DesktopPreferenceData>();
			travelNode(relativeNodePath, userBasePreference, vector);
			return vector;
		} catch (BackingStoreException e) {
			logger.error("failed to get data", e);
			throw new RuntimeException("Fix this exception", e);
		}
	}

	private void travelNode(String relativeNodePath, Preferences node, List<DesktopPreferenceData> list)
			throws BackingStoreException {
		String[] names = node.childrenNames();
		for (String name : names) {
			if (node.nodeExists(name)) {
				String newRelativeNodePath = relativeNodePath + "/" + name;
				Preferences newNode = node.node(name);
				travelNode(newRelativeNodePath, newNode, list);
			}
		}
		for (String key : node.keys()) {
			String value = node.get(key, null);
			// logger.debug(node + " " + key + ": " + value);
			if (JavaUtil.isNotEmpty(value)) {
				DesktopPreferenceData pdto = new DesktopPreferenceData(relativeNodePath, key, value);
				list.add(pdto);
			}
		}
	}

	private void recursiveClear(Preferences basePreference) throws BackingStoreException {
		for (String name : basePreference.childrenNames()) {
			recursiveClear(basePreference.node(name));
		}
		for (String key : basePreference.keys()) {
			basePreference.put(key, "");
		}
	}
}
