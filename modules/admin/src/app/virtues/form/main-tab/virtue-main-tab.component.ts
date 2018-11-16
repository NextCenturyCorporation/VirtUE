import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { Printer } from '../../../shared/models/printer.model';
import { FileSystem } from '../../../shared/models/fileSystem.model';
import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { VirtualMachine } from '../../../shared/models/vm.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';

import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { ItemFormMainTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-main-tab/item-form-main-tab.component';

/**
 * @class
 * This class represents the main tab for a Virtue form - [[VirtueComponent]]
 *
 * From here, the user can view/add/remove the [[Virtue]]'s attached virtual machines, view the Virtue's version number,
 * and enable/disable the Virtue.
 *
 * Note that version number increases automatically.
 *
 * @extends [[ItemFormMainTabComponent]]
 */
@Component({
  selector: 'app-virtue-main-tab',
  templateUrl: './virtue-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class VirtueMainTabComponent extends ItemFormMainTabComponent implements OnInit {

  /** the version to be displayed. See [[updateVersion]] for details */
  private newVersion: number;

  /** re-classing parent's item object */
  protected item: Virtue;

  /**
   * see [[ItemFormMainTabComponent.constructor]] for parameters
   */
  constructor(
      router: Router,
      dialog: MatDialog) {
    super(router, dialog);
    this.childDatasetName = DatasetNames.VMS;
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
   *
   * @param item a reference to the Item being displayed by this tab's parent form.
   */
  setUp(item: Item): void {
    if ( !(item instanceof Virtue) ) {
      // TODO throw error
      console.log("item passed to virtue-main-tab which was not a Virtue: ", item);
      return;
    }
    this.item = item as Virtue;

    this.updateVersion();
  }

  /**
   * Overrides parent, [[ItemFormTabComponent.setMode]]
   *
   * @param newMode the Mode to set the page as.
   */
  setMode(newMode: Mode): void {
    this.mode = newMode;

    if (this.item) {
      this.updateVersion();
    }
  }

  /**
   * Updates what value gets listed as the current version.
   * In edit mode, the version is what version it'll be saved as; The current version + 1.
   * Otherwise, it should just show the current version.
   */
  updateVersion(): void {
    this.newVersion = this.item.version;

    // if (this.mode === Mode.EDIT || this.mode === Mode.DUPLICATE) {
    if (this.mode === Mode.EDIT) {
      this.newVersion++;
    }
  }

  /**
   * @return what columns should show up in the virtue's VM children table
   *         The first column, the VM's name, should be clickable if and only if the page is in view mode.
   */
  getColumns(): Column[] {
    return [
      new TextColumn('VM Template Name', 4, (vm: VirtualMachine) => vm.getName(), SORT_DIR.ASC, (i: Item) => this.viewItem(i),
                                                                                                () => this.getSubMenu()),
      new ListColumn('Assigned Apps', 4, (v: VirtualMachine) => this.getApps(v),  this.formatName),
      new TextColumn('OS', 2, (vm: VirtualMachine) => String(vm.version), SORT_DIR.ASC),
      new TextColumn('Version', 1, (vm: VirtualMachine) => String(vm.version), SORT_DIR.ASC),
      new TextColumn('Status',  1, this.formatStatus, SORT_DIR.ASC)
    ];
  }

  /**
   * @return a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "No virtual machine templates have been given to this Virtue yet. \
To add a virtual machine template, click on the button \"Add VM\" above.";
  }

  /**
   * Pull in whatever [[version]] the item should be saved as.
   *
   * Eventually, add something to check name? I know names aren't IDs for virtues, but if two virtues
   * in a list have the same name, how can they be easily distinguished?
   * @return true always at the moment
   */
  collectData(): boolean {
    this.item.version = this.newVersion;
    return true;
  }

  /**
   * Loads an VmModalComponent
   * @param parameters to be passed into the modal
   */
  getDialogRef(params: {
                          /** the height of the modal, in pixels */
                          height: string,
                          /** the width of the modal, in pixels */
                          width: string,
                          /** some type of data object to be passed into the modal - a container */
                          data: any
                        }) {
    return this.dialog.open( VmModalComponent, params);
  }


  /**
   * Removes childItem from this.item.vmTemplates and its id from this.item.vmTemplateIds.
   * Remember this.item is a Virtue here, and childItem can be a VirtualMachine, Printer, or .
   *
   * @param childItem the Item to be removed from this.[[item]]'s child lists.
   * @override parent [[ItemFormMainTabComponent.removeChildObject]]()
   */
  removeChildObject(childItem: IndexedObj): void {
    if (childItem instanceof VirtualMachine) {
      this.item.removeChild(childItem.getID(), DatasetNames.VMS);
    }
    else if (childItem instanceof Printer) {
      this.item.removeChild(childItem.getID(), DatasetNames.PRINTERS);
    }
    else if (childItem instanceof FileSystem) {
      this.item.removeChild(childItem.getID(), DatasetNames.FILE_SYSTEMS);
    }
    else {
      console.log("The given object doesn't appear to be a VM.");
    }
  }
}
