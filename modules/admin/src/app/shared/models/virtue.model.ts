
import { DatePipe } from '@angular/common';

import { Item } from './item.model';
import { Application } from './application.model';
import { VirtualMachine } from './vm.model';

import { DictList } from './dictionary.model';

import { NetworkPermission } from './networkPerm.model';
import { FileSysPermission } from './fileSysPermission.model';
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

  // /** #TODO what is this? */
  // awsTemplateName: string;

  /** A hex string of the color to be shown on this Virtue's label, both on the workbench and the desktop app */
  color: string;

  /** #TODO what is this? #uncommented (unprovisioned) */
  unprovisioned: boolean;

  /**
   * What browser should any links clicked within this Virtue automatically open in?
   */
  defaultBrowser: string;

  /** A list of networks this Virtue is permitted to connect to */
  networkWhiteList: NetworkPermission[];

  /** this holds the IDs of the virtues that this virtue is allowed to paste data into. */
  allowedPasteTargets: string[];

  /** this virtue's r/w/e permissions for the different parts of the filesystem */
  fileSysPerms: FileSysPermission[];

  /** a list of printers this virtue is allowed to use. Printers are found and set up in global settings. */
  allowedPrinters: Printer[];

  /**
   * convert from whatever form the virtue object is in the database.
   *
   * @param virtueObj a virtue record, retrieved from the backend, which we want to convert into a Virtue.
   */
  constructor(virtueObj) {
    super();
    if (virtueObj) {
      this.id = virtueObj.id;
      this.name = virtueObj.name;
      this.childIDs = virtueObj.virtualMachineTemplateIds;
      this.enabled = virtueObj.enabled;

      this.version = virtueObj.version;
      if (! this.version) {
        this.version = 1;
      }

      this.lastEditor = virtueObj.lastEditor;
      this.lastModification = virtueObj.lastModification;
      this.modDate = new DatePipe('en-US').transform(virtueObj.lastModification, 'short');
      this.color = virtueObj.color;

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
      // TODO  Placeholder filesystems/servers - these will be defined in and pulled from the global settings.
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
