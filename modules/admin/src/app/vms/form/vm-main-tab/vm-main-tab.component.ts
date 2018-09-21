import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { VirtualMachine } from '../../../shared/models/vm.model';
import { Column } from '../../../shared/models/column.model';
import { Mode } from '../../../shared/abstracts/gen-form/mode.enum';
import { ConfigUrls } from '../../../shared/services/config-urls.enum';
import { Datasets } from '../../../shared/abstracts/gen-data-page/datasets.enum';

import { AppsModalComponent } from '../../../modals/apps-modal/apps-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

import { OSSet } from '../../os.set';

/**
 * This class represents the main tab for a VirtualMachine template form - [[VmComponent]]
 *
 * From here, the user can view/add/remove the [[VirtualMachine]]'s attached applications, view the VM template's version number,
 * change the template's OS, and enable/disable the template.
 *
 * Note that version number increases automatically.
 *
 * @extends [[GenericMainTabComponent]]
 */
@Component({
  selector: 'app-vm-main-tab',
  templateUrl: './vm-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ OSSet ]
})
export class VmMainTabComponent extends GenericMainTabComponent implements OnInit {

  /** the version to be displayed. See [[updateVersion]] for details */
  private newVersion: number;

  /** re-classing parent's item object */
  protected item: VirtualMachine;

  /**
   * see [[GenericMainTabComponent.constructor]] for inherited parameters
   */
  constructor(
      /** the available operating systems that this VM can be set as. */
      protected osOptions: OSSet,
      router: Router, dialog: MatDialog) {
    super(router, dialog);
  }

  /**
   * See [[GenericFormTabComponent.setUp]] for generic info
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
   * Overrides parent, [[GenericFormTabComponent.setMode]]
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
      new Column('name',    'Application Name', 5, 'asc'),
      new Column('version', 'Version',          3, 'asc'),
      new Column('os',      'Operating System', 4, 'desc')
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
