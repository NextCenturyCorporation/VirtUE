/**
 * @enum
 * The mode a form page can be in. See [[GenericFormComponent]].
 */
export enum Mode {
  CREATE = "Create",
  EDIT = "Edit",
  DUPLICATE = "Duplicate",
  VIEW = "View"
}

/**
 * @enum
 * The paths on the backend which must be queried for each of those datasets
 */
export enum ConfigUrls {
  APPS = "admin/application/",
  VMS = "admin/virtualMachine/template/",
  VIRTUES = "admin/virtue/template/",
  USERS = "admin/user/"
}

/**
 * @enum
 * The available protocols which a virtue can be permitted to use with a given [[NetworkPermission]]
 * in  [[VirtueSettingsTabComponent]]
 */
export enum Protocols {
  TCPIP = "TCP/IP",
  UDP = "UDP",
  ICMP = "ICMP"
}

/**
 * the names of the main four datasets, to provide an encapsulated interface for
 * pages requesting the population of datasets.
 */
export enum Datasets {
  APPS = "allApps",
  VMS = "allVms",
  VIRTUES = "allVirtues",
  USERS = "allUsers"
}
