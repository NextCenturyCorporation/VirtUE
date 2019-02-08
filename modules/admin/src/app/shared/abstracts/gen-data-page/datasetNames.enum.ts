
/**
 * @enum
 * the names of the datasets we want to be able to load from, to provide an encapsulated interface for
 * pages requesting the population of datasets.
 * For the basic-object-details page to work, the enum's value.toUpperCase() must === the enum's name.
 */
export enum DatasetNames {
  APPS = "apps",
  VM_TS = "vm_ts",
  VIRTUE_TS = "virtue_ts",
  USERS = "users",
  PRINTERS = "printers",
  FILE_SYSTEMS = "file_systems",
  VIRTUES = "virtues",
  VMS = "vms",
  SENSORS = "sensors"
}
