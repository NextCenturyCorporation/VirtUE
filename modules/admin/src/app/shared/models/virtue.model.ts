
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { IndexedObj } from './indexedObj.model';
import { DatasetNames } from '../abstracts/gen-data-page/datasetNames.enum';
import { Subdomains } from '../services/subdomains.enum';

import { DictList } from './dictionary.model';

import { NetworkPermission } from './networkPerm.model';
import { FileSystem } from './fileSystem.model';
import { Printer } from './printer.model';

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
 * [[children]] are VirtualMachine objects.
 *
 * @extends [[Item]]
 */
export class Virtue extends Item {

  /** What iteration of edit this template is on. Automatically increases on edit. */
  version: number = 1;

  /** #TODO do we need this? Can anyone else edit templates, besides the admin? Or will there be multiple, distinguishable, admins? */
  lastEditor: string;

  /** A full Date, of the last time this record was changed on the backend. */
  lastModification: Date;

  /** #uncommented */
  vmTemplates: DictList<VirtualMachine> = new DictList<VirtualMachine>();

  /** #uncommented */
  vmTemplateIds: string[] = [];

  // /** #TODO what is this? */
  // awsTemplateName: string;

  /** A hex string of the color to be shown on this Virtue's label, both on the workbench and the desktop app */
  color: string = 'transparent';

  /** #TODO what is this? #uncommented (unprovisioned) */
  unprovisioned: boolean = true;

  /**
   * What virtue should any links clicked within this Virtue automatically open in?
   */
  defaultBrowserVirtueId: string;
  defaultBrowserVirtue: Virtue;

  /** A list of networks this Virtue is permitted to connect to */
  networkWhiteList: NetworkPermission[] = [];

  /** this holds the IDs of the virtues that this virtue is allowed to paste data into. */
  allowedPasteTargetIds: string[] = [];
  /** this holds the IDs of the virtues that this virtue is allowed to paste data into. */
  allowedPasteTargets: DictList<Virtue> = new DictList<Virtue>();

  /** this virtue's r/w/e permissions for the filesystem
   * Note that fileSystems is saved and loaded with the virtue. While everything else is saved as a list of IDs,
   * here, the list of IDs is just used to update the names of the saved fileSystems, since each virtue can set its own individual
   * permissions for a given fileSystem.
   */
  fileSystems: DictList<FileSystem> = new DictList<FileSystem>();
  fileSystemIds: string[] = [];

  /** a list of printers this virtue is allowed to use. Printers are found and set up in global settings. */
  printers: DictList<Printer> = new DictList<Printer>();
  printerIds: string[] = [];

  /**
   * convert from whatever form the virtue object is in the database.
   *
   * Note that there doesn't currently appear to be any good way to compare if two objects have the same
   * attributes, without simply iterating through them all and checking. 'instanceof' appears to only work
   * for items created from the same prototype.
   *
   * The signature is equivalent to 'any', but
   *
   * @param virtueObj a virtue record, retrieved from the backend, which we want to convert into a Virtue.
   */
  constructor(virtueObj) {
    super();
    this.parentDomain = '/virtues';
    if ('name' in virtueObj) {
      this.name = virtueObj.name;
    }

    if ('id' in virtueObj) {
      if (virtueObj.id === "1f4e0394-5b60-4d0a-b632-721834b09945") {
        console.log(virtueObj.fileSystems);
      }
      this.id = virtueObj.id;
    }

    if ('enabled' in virtueObj) {
      this.enabled = virtueObj.enabled;
    }

    if ('vmTemplateIds' in virtueObj && virtueObj.vmTemplateIds) {
      this.vmTemplateIds = virtueObj.vmTemplateIds;
    }

    if ('fileSystemIds' in virtueObj && virtueObj.fileSystemIds) {
      this.fileSystemIds = virtueObj.fileSystemIds;
    }

    // `Array.isArray` apparently doesn't work on some legacy browsers (i.e., IE) - we should be fine.
    // https://stackoverflow.com/a/20989617/3015812
    if ('fileSystems' in virtueObj && virtueObj.fileSystems && Array.isArray(virtueObj.fileSystems)) {
      for (let fs of virtueObj.fileSystems) {
        this.fileSystems.add(fs.id, fs);
      }
    }

    if ('printerIds' in virtueObj && virtueObj.printerIds) {
      this.printerIds = virtueObj.printerIds;
    }

    if ('allowedPasteTargetIds' in virtueObj && virtueObj.allowedPasteTargetIds) {
      this.allowedPasteTargetIds = virtueObj.allowedPasteTargetIds;
    }

    if ('lastEditor' in virtueObj) {
      this.lastEditor = virtueObj.lastEditor;
    }

    if ('lastModification' in virtueObj) {
      this.lastModification = virtueObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');
    }

    if ('networkWhiteList' in virtueObj && virtueObj.networkWhiteList) {
      this.networkWhiteList = virtueObj.networkWhiteList;
    }

    if ('unprovisioned' in virtueObj) {
      this.unprovisioned = virtueObj.unprovisioned;
    }

    if ('defaultBrowserVirtueId' in virtueObj) {
      this.defaultBrowserVirtueId = virtueObj.defaultBrowserVirtueId;
    }

    if ('version' in virtueObj) {
      this.version = virtueObj.version;
    }

    if ('color' in virtueObj) {
      this.color = virtueObj.color;
    }

    if (this.id === "1f4e0394-5b60-4d0a-b632-721834b09945") {
      console.log(this);
    }
  }

  /**
   * @return the VIRTUES subdomain
   */
  getSubdomain(): string {
    return Subdomains.VIRTUES;
  }

  /**
   * #uncommented
   */
  buildAttribute( datasetName: DatasetNames, dataset: DictList<IndexedObj> ): void {
    if (datasetName === DatasetNames.VIRTUES) {
      this.defaultBrowserVirtue = dataset.get(this.defaultBrowserVirtueId) as Virtue;
      this.allowedPasteTargets = dataset.getSubset(this.allowedPasteTargetIds) as DictList<Virtue>;
    }
    if (datasetName === DatasetNames.VMS) {
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


  /** @override [[Item.getRelatedDict]] */
  getRelatedDict(datasetName: DatasetNames): DictList<IndexedObj> {
    switch (datasetName) {
      case DatasetNames.VMS:
        return this.vmTemplates;
      case DatasetNames.PRINTERS:
        return this.printers;
      case DatasetNames.FILE_SYSTEMS:
        return this.fileSystems;
      default:
        console.log("You shouldn't be here. Expected datasetName === DatasetNames.{VMS, PRINTERS}, was", datasetName);
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
      case DatasetNames.VMS:
        return this.vmTemplateIds;
      case DatasetNames.PRINTERS:
        return this.printerIds;
      case DatasetNames.FILE_SYSTEMS:
        return this.fileSystemIds;
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.VIRTUES, was", datasetName);
    return [];
  }

  protected getInBackendFormat() {


    let virtue = {
        name: this.name,
        id: this.id,
        enabled: this.enabled,
        vmTemplateIds: this.vmTemplateIds,
        fileSystemIds: this.fileSystemIds,
        fileSystems: this.fileSystems.asList(),
        printerIds: this.printerIds,
        allowedPasteTargetIds: this.allowedPasteTargetIds,
        lastEditor: this.lastEditor,
        lastModification: this.lastModification,
        networkWhiteList: this.networkWhiteList,
        unprovisioned: this.unprovisioned,
        version: this.version,
        defaultBrowserVirtueId: this.defaultBrowserVirtueId,
        color: this.color

    };

    // // just to clear a little memory.
    // this.virtueTemplates = undefined;
    // this.roles = [];
    return virtue;
  }
}
