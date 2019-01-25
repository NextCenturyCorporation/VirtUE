import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';

import { IndexedObj } from '../../../shared/models/indexedObj.model';
import { Item } from '../../../shared/models/item.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Application } from '../../../shared/models/application.model';

import {
  Column,
  TextColumn,
  ListColumn,
  SORT_DIR
} from '../../../shared/models/column.model';

import { RouterService } from '../../../shared/services/router.service';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';

import { DatasetNames } from '../../../shared/abstracts/gen-data-page/datasetNames.enum';

import { AppsModalComponent } from '../../../modals/apps-modal/apps-modal.component';

import { ItemFormMainTabComponent } from '../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-main-tab/item-form-main-tab.component';

import { OSSet } from '../../os.set';

/**
 * This class represents the main tab for a VirtualMachine template form - [[VmComponent]]
 *
 * From here, the user can view/add/remove the [[VirtualMachine]]'s attached applications, view the VM template's version number,
 * change the template's OS, and enable/disable the template.
 *
 * Note that version number increases automatically.
 *
 * @extends [[ItemFormMainTabComponent]]
 */
@Component({
  selector: 'app-vm-main-tab',
  templateUrl: './vm-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-form-tab/item-form-tab/item-form-tab.component.css']
})
export class VmMainTabComponent extends ItemFormMainTabComponent implements OnInit {

  /** the version to be displayed. See [[updateVersion]] for details */
  private newVersion: number;

  /** re-classing parent's item object
   * Must be public to be used in template html file in production mode.
   */
  public item: VirtualMachine;

  /**
   * see [[ItemFormMainTabComponent.constructor]] for inherited parameters
   */
  constructor(
      routerService: RouterService,
      dialog: MatDialog,
      /** the available operating systems that this VM can be set as.
       * Must be public to be used in template html file in production mode.
       */
      public osOptions: OSSet
      ) {
    super(routerService, dialog);
    this.childDatasetName = DatasetNames.APPS;
  }

  /**
   * See [[ItemFormTabComponent.setUp]] for generic info
   * @param
   *
   * @return
   */
  setUp(item: Item): void {
    if ( !(item instanceof VirtualMachine) ) {
      // TODO throw error
      console.log("item passed to vm-main-tab which was not a VirtualMachine: ", item);
      return;
    }
    this.item = item as VirtualMachine;

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
   * @return what columns should show up in the VM's's application children table
   */
  getColumns(): Column[] {
    return [
      new TextColumn('Application Name', 5, (a: Application) => a.getName(), SORT_DIR.ASC, (i: Item) => this.viewItem(i),
                                                                                                () => this.getSubMenu()),
      new TextColumn('Version',          3, (a: Application) => String(a.version), SORT_DIR.ASC),
      new TextColumn('Operating System', 4, (a: Application) => a.os, SORT_DIR.DESC)
    ];
  }

  /**
   * @return a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return 'No applications have been added yet. To add a template, click on the button "Add/Remove Application Packages" above.';
  }

  /**
   * Pull in whatever [[version]] the item should be saved as.
   *
   * Eventually, add something to check for name uniqueness?
   * @return true always at the moment
   */
  collectData(): boolean {
    this.item.version = this.newVersion;
    return true;
  }


  /**
   * Loads an AppsModalComponent
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
    return this.dialog.open( AppsModalComponent, params);
  }
}
