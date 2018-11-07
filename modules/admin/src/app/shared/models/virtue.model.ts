
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
  version: number;

  /** #TODO do we need this? Can anyone else edit templates, besides the admin? Or will there be multiple, distinguishable, admins? */
  lastEditor: string;

  /** A full Date, of the last time this record was changed on the backend. */
  lastModification: Date;

  /** #uncommented */
  vmTemplates: DictList<IndexedObj>;

  /** #uncommented */
  vmTemplateIds: string[];

  // /** #TODO what is this? */
  // awsTemplateName: string;

  /** A hex string of the color to be shown on this Virtue's label, both on the workbench and the desktop app */
  color: string;

  /** #TODO what is this? #uncommented (unprovisioned) */
  unprovisioned: boolean;

  /**
   * What virtue should any links clicked within this Virtue automatically open in?
   */
  defaultBrowserVirtueId: string;
  defaultBrowserVirtue: Virtue;

  /** A list of networks this Virtue is permitted to connect to */
  networkWhiteList: NetworkPermission[];

  /** this holds the IDs of the virtues that this virtue is allowed to paste data into. */
  allowedPasteTargetIds: string[];
  /** this holds the IDs of the virtues that this virtue is allowed to paste data into. */
  allowedPasteTargets: DictList<Virtue>;

  /** this virtue's r/w/e permissions for the different parts of the filesystem */
  fileSystems: DictList<FileSystem>;
  fileSystemIds: string[];

  /** a list of printers this virtue is allowed to use. Printers are found and set up in global settings. */
  printers: DictList<Printer>;
  printerIds: string[];

  /**
   * convert from whatever form the virtue object is in the database.
   *
   * @param virtueObj a virtue record, retrieved from the backend, which we want to convert into a Virtue.
   */
  constructor(virtueObj) {
    super();
    if (virtueObj) {
      // console.log(JSON.stringify(virtueObj, null, 2));
      this.id = virtueObj.id;
      this.name = virtueObj.name;

      this.vmTemplateIds = virtueObj. vmTemplateIds;
      this.fileSystemIds = virtueObj.fileSystemIds;
      this.printerIds = virtueObj.printerIds;

      this.allowedPasteTargetIds = virtueObj.pasteTargetIds;

      this.enabled = virtueObj.enabled;

      this.version = virtueObj.version;
      this.color = virtueObj.color;

      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');



      // TODO set all below to the corresponding value in virtObj, once implemented on backend.
      if (! this.networkWhiteList) {
        this.networkWhiteList = [];
      }

      if (! this.unprovisioned) {
        this.unprovisioned = true;
      }

      if (virtueObj.defaultBrowserVirtueId) {
        this.defaultBrowserVirtueId = virtueObj.defaultBrowserVirtueId;
      }

      if (! this.allowedPasteTargets) {
        this.allowedPasteTargetIds = [];
      }

    }

    this.parentDomain = '/virtues';
    if (! this.version) {
      this.version = 1;
    }

    if (! this.color) {
      // TODO - is it better to have everything default to no color, or to something
      // like silver (#C0C0C0), to let the user know that colors are available?
      this.color = 'transparent';
    }
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
      this.fileSystems = dataset.getSubset(this.fileSystemIds) as DictList<FileSystem>;
    }
    else if (datasetName === DatasetNames.PRINTERS) {
      // console.log(this.printerIds);
      this.printers = dataset.getSubset(this.printerIds) as DictList<Printer>;
    }

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
    }
    console.log("You shouldn't be here. Expected datasetName === DatasetNames.{VMS, PRINTERS, FILE_SYSTEMS}, was", datasetName);
    return null;
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
}
