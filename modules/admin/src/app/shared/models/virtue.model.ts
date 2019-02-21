
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';

import { DictList } from './dictionary.model';

import { NetworkPermission } from './networkPerm.model';
import { FileSystem } from './fileSystem.model';
import { Printer } from './printer.model';

export class ClipboardPermission {
  source: string;
  dest: string;
  permission: ClipboardPermissionOption;

  constructor( clip? ) {
    if (clip) {
      this.source = clip.sourceGroupId;
      this.dest = clip.destinationGroupId;
      this.permission = clip.permission;
    }
  }
}

export enum ClipboardPermissionOption {
  ALLOW = "ALLOW",
  DENY = "DENY",
  ASK = "ASK"
}

/**
 * @class
 * Represents a template for a Virtue.
 * A virtue is a collection of virtual machines that can generally interact with each other, but
 *  not (by default) with any vm in a different Virtue. Each Virtue has a plethora of settings that can be defined,
 *  which allow the admin to decide what small set of ways this collection of virtual machines is allowed to interact
 *  with the outside world.
 *
 * Virtue instances also have software sensors built in, which can be monitored to detect possible intrusions or attacks.
 *
 * Virtues are supposed to represent a role, a single cohesive bundle of activities. So there might be a vm that has MS office on it,
 * and another that has LibreOffice and gedit on it. Those two together might make an "office" Virtue.
 * A standard office worker might have:
 *  - that office virtue
 *  - an internet virtue, with browsers, that's open to the world but closed to all other virtues
 *  - an intranet virtue with corporate email and other internal services
 *
 * This is a template. When saved to the backend, an image will be built and stored there, which in turn is what is used to create
 * virtue instances whenever a virtue-desktop user clicks on an application under that Virtue's umbrella.
 *
 * @extends [[Item]]
 */
export class Virtue extends Item {

  /** What iteration of edit this template is on. Automatically increases on edit. */
  version: number = 1;

  /** #TODO do we need this? Can anyone else edit templates, besides the admin? Or will there be multiple, distinguishable, admins? */
  lastEditor: string;

  private vmTemplates: DictList<VirtualMachine> = new DictList<VirtualMachine>();

  vmTemplateIds: string[] = [];

  // /** #TODO what is this? */
  // awsTemplateName: string;

  /** A hex string of the color to be shown on this Virtue's label, both on the workbench and the desktop app */
  color: string = 'transparent';

  /** What virtue should any links clicked within this Virtue automatically open in */
  defaultBrowserVirtueId: string;
  private defaultBrowserVirtue: Virtue;

  /** A list of networks this Virtue is permitted to connect to */
  networkSecurityPermWhitelist: NetworkPermission[] = [];
  private newSecurityPermissions: NetworkPermission[] = [];
  private revokedSecurityPermissions: NetworkPermission[] = [];

  /** this holds the IDs and permissions regarding the virtues that this virtue is allowed to paste data into. */
  clipboardPermissions: ClipboardPermission[] = [];

  /** this virtue's r/w/e permissions for the filesystem
   * Note that fileSystems is saved and loaded with the virtue. While everything else is saved as a list of IDs,
   * here, the list of IDs is just used to update the names of the saved fileSystems, since each virtue can set its own individual
   * permissions for a given fileSystem.
   */
  private fileSystems: DictList<FileSystem> = new DictList<FileSystem>();
  fileSystemIds: string[] = [];

  /** a list of printers this virtue is allowed to use. Printers are found and set up in global settings. */
  private printers: DictList<Printer> = new DictList<Printer>();
  printerIds: string[] = [];

  /**
   * convert from whatever form the virtue object is in the database.
   *
   * Note that there doesn't currently appear to be any good way to compare if two objects have the same
   * attributes, without simply iterating through them all and checking. 'instanceof' appears to only work
   * for items created from the same prototype.
   *
   * TODO should this be durable, or fail-fast?
   *
   * @param virtueObj a virtue record, retrieved from the backend, which we want to convert into a Virtue.
   */
  constructor(virtueObj?) {
    super();
    this.parentDomain = '/virtues';

    if (virtueObj) {
      this.name = virtueObj.name;
      this.id = virtueObj.id;
      this.enabled = virtueObj.enabled;
      this.vmTemplateIds = virtueObj.vmTemplateIds;
      this.printerIds = virtueObj.printerIds;
      this.fileSystemIds = virtueObj.fileSystemIds;

      this.lastEditor = virtueObj.lastEditor;

      this.modificationDate = virtueObj.lastModification;
      this.readableModificationDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');

      this.defaultBrowserVirtueId = virtueObj.defaultBrowserVirtueId;
      this.version = virtueObj.version;
      this.color = virtueObj.color;
    }

  }

  getDatasetName(): string {
    return DatasetNames.VIRTUE_TS;
  }

  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {
    if (datasetName === DatasetNames.VIRTUE_TS) {
      this.defaultBrowserVirtue = dataset.get(this.defaultBrowserVirtueId) as Virtue;
    }
    if (datasetName === DatasetNames.VM_TS) {
      this.vmTemplates = dataset.getSubset(this.vmTemplateIds) as DictList<VirtualMachine>;
    }
    else if (datasetName === DatasetNames.FILE_SYSTEMS) {
      let fileSystemDefaults = dataset.getSubset(this.fileSystemIds) as DictList<FileSystem>;
      this.updateFileSystems(fileSystemDefaults);
    }
    else if (datasetName === DatasetNames.PRINTERS) {
      this.printers = dataset.getSubset(this.printerIds) as DictList<Printer>;
    }

  }

  updateFileSystems(selectedFileSystemDefaultPerms: DictList<FileSystem>) {
    if (this.fileSystems === undefined) {
      this.fileSystems = new DictList<FileSystem>();
    }

    for (let newFileSystem of selectedFileSystemDefaultPerms.asList()) {
      if ( this.fileSystems.has(newFileSystem.getID())) {
        this.fileSystems.get(newFileSystem.getID()).name = newFileSystem.name;
      }
      else {
        this.fileSystems.add(newFileSystem.getID(), newFileSystem);
      }
    }

    this.fileSystems.trimTo(this.fileSystemIds);
  }


  getVms(): IndexedObj[] {
    return this.getChildren(DatasetNames.VM_TS);
  }

  getVmApps(): IndexedObj[] {
    return this.getGrandChildren(DatasetNames.VM_TS, DatasetNames.APPS);
  }


  removeUnspecifiedChild(childObj: IndexedObj) {
    if (childObj instanceof VirtualMachine) {
      this.removeVm(childObj);
    }
    else if (childObj instanceof Printer) {
      this.removePrinter(childObj);
    }
    else if (childObj instanceof FileSystem) {
      this.removeFileSystem(childObj);
    }
    else {
      console.log("The given object doesn't appear to be a Vm, Printer, or FileSystem.");
    }
  }

  removeVm(vm: VirtualMachine) {
    this.removeChild(vm.getID(), DatasetNames.VM_TS);
  }

  removePrinter(printer: Printer) {
    this.removeChild(printer.getID(), DatasetNames.PRINTERS);
  }

  removeFileSystem(fileSys: FileSystem) {
    this.removeChild(fileSys.getID(), DatasetNames.FILE_SYSTEMS);
  }


  /** @override [[Item.getRelatedDict]] */
  getRelatedDict(datasetName: DatasetNames): DictList<IndexedObj> {
    switch (datasetName) {
      case DatasetNames.VM_TS:
        return this.vmTemplates;
      case DatasetNames.PRINTERS:
        return this.printers;
      case DatasetNames.FILE_SYSTEMS:
        return this.fileSystems;
      default:
        console.log("You shouldn't be here. Requested dataset: ", datasetName);
        return null;
    }
  }

  /**
   * Currently Users only have one type of children, so just return that.
   *
   * @override [[Item.getRelatedIDList]]
   */
  getRelatedIDList(datasetName: DatasetNames): string[] {
    switch (datasetName) {
      case DatasetNames.VM_TS:
        return this.vmTemplateIds;
      case DatasetNames.PRINTERS:
        return this.printerIds;
      case DatasetNames.FILE_SYSTEMS:
        return this.fileSystemIds;
    }
    console.log("You shouldn't be here. Requested dataset: ", datasetName);
    return [];
  }

  protected getInBackendFormat() {

    let virtue = {
        name: this.name,
        id: this.id,
        enabled: this.enabled,
        vmTemplateIds: this.vmTemplateIds,
        fileSystemIds: this.fileSystemIds,
        printerIds: this.printerIds,
        lastEditor: this.lastEditor,
        lastModification: this.modificationDate,
        networkSecurityPermWhitelist: this.networkSecurityPermWhitelist,
        version: this.version,
        defaultBrowserVirtueId: this.defaultBrowserVirtueId,
        color: this.color

    };
    return virtue;
  }

  getVmTemplates() {
    return this.vmTemplates;
  }

  getDefaultBrowserVirtue() {
    return this.defaultBrowserVirtue;
  }

  getNewSecurityPermissions() {
    return this.newSecurityPermissions;
  }

  getRevokedSecurityPermissions() {
    return this.revokedSecurityPermissions;
  }

  getFileSystems() {
    return this.fileSystems;
  }

  getPrinters() {
    return this.printers;
  }
}
