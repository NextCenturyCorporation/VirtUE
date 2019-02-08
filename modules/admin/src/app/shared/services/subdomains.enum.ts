
/**
 * @enum
 * The paths on the backend which must be queried for each of those datasets
 *
 */
export enum Subdomains {
  APPS = "admin/application/",
  VM_TS = "admin/virtualMachine/template/",
  VIRTUE_TS = "admin/virtue/template/",
  USERS = "admin/user/",
  PRINTERS = "admin/printer/",
  FILE_SYSTEMS = "admin/fileSystem/",
  VIRTUES = "admin/virtues",
  VMS = "admin/vm",
  SENSORS = "admin/sensing"
}
