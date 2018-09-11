
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { DictList } from './dictionary.model';

import { NetworkPermission } from './networkPerm.model';
import { FileSysPermission } from './fileSysPermission.model';
import { Printer } from './printer.model';

/**
 * #uncommented
 * @class
 * @extends
 * 
 * Represents a Virtue.
 * Children are VirtualMachine objects.
 *
 * This needs an html list (as a string) representing it's child vms' apps for
 * the virtue-list page, where both the children (vms) and children's children
 * (apps) need to be listed.
 */
export class Virtue extends Item {

  id: string;
  version: string;
  lastEditor: string;
  lastModification: string | Date;
  awsTemplateName: string;
  color: string;

  unprovisioned: boolean;

  defaultBrowser: string;

  networkWhiteList: NetworkPermission[];

  // this holds the IDs of the virtues that this virtue is allowed to paste data into.
  allowedPasteTargets: string[];

  // this virtue's r/w/e permissions for the different parts of the filesystem
  fileSysPerms: FileSysPermission[];

  allowedPrinters: Printer[];

  // convert from whatever form the virtue object is in the database.
  constructor(virtueObj) {
    super();
    if (virtueObj) {
      this.id = virtueObj.id;
      this.name = virtueObj.name;
      this.childIDs = virtueObj.virtualMachineTemplateIds;
      this.enabled = virtueObj.enabled;

      this.version = virtueObj.version;
      if (! this.version) {
        this.version = '1';
      }

      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');
      this.color = virtueObj.color;
      this.status = virtueObj.enabled ? 'enabled' : 'disabled';

      this.parentDomain = '/virtues';


      // TODO set this.{all below} to the corresponding value in virtObj, once implemented on backend.
      if (! this.networkWhiteList) {
        this.networkWhiteList = [];
      }

      if (! this.unprovisioned) {
        this.unprovisioned = true;
      }

      if (! this.defaultBrowser) {
        this.defaultBrowser = "Bowser";
      }

      if (! this.allowedPasteTargets) {
        this.allowedPasteTargets = [];
      }
      // TODO  How are these filesystems defined/found? Are they going to be dynamic?
      if (! this.fileSysPerms) {
        this.fileSysPerms = [
          new FileSysPermission("File Server\\Media "),
          new FileSysPermission("File Server\\Documents "),
          new FileSysPermission("File Server\\C Drive "),
          new FileSysPermission("File Server\\F Drive ")
        ];
      }
      // TODO  Placeholder printers.
      if (! this.allowedPrinters) {
        this.allowedPrinters = [
          new Printer("Printer 1"),
          new Printer("Printer 2"),
          new Printer("Printer 3"),
          new Printer("Printer 4"),
          new Printer("Printer 5")
        ];
      }
    }


    if (! this.color) {
      // TODO - is it better to have everything default to no color, or to something
      // like silver (#C0C0C0), to let the user know that colors are available?
      this.color = 'transparent';
    }
  }

}
