import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { Item } from '../../../shared/models/item.model';
import { Virtue } from '../../../shared/models/virtue.model';
import { Column } from '../../../shared/models/column.model';
import { Mode, ConfigUrls, Datasets } from '../../../shared/enums/enums';

import { VmModalComponent } from '../../../modals/vm-modal/vm-modal.component';

import { GenericMainTabComponent } from '../../../shared/abstracts/gen-tab/gen-main-tab/gen-main-tab.component';

/**
 * @class
 * This class represents the main tab for a Virtue form - [[VirtueComponent]]
 *
 * From here, the user can view/add/remove the [[Virtue]]'s attached virtual machines, view the Virtue's version number,
 * and enable/disable the Virtue.
 *
 * Note that version number increases automatically.
 *
 * @extends [[GenericMainTabComponent]]
 */
@Component({
  selector: 'app-virtue-main-tab',
  templateUrl: './virtue-main-tab.component.html',
  styleUrls: ['../../../shared/abstracts/gen-tab/gen-tab.component.css']
})
export class VirtueMainTabComponent extends GenericMainTabComponent implements OnInit {

  /** the version to be displayed. See [[updateVersion]] for details */
  private newVersion: number;

  /** re-classing parent's item object */
  protected item: Virtue;

  /**
   * see [[GenericMainTabComponent.constructor]] for parameters
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
  }

  /**
   * See [[GenericFormTabComponent.setUp]] for generic info
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
    this.newVersion = Number(this.item.version);

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
    let cols: Column[] = [
      new Column('os',          'OS',                    2, 'asc'),
      new Column('childNames',  'Assigned Applications', 4, undefined,  this.formatName, this.getChildren),
      new Column('enabled',      'Status',                2, 'asc',      this.formatStatus)
    ];
    if (this.mode === Mode.VIEW) {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc', undefined, undefined, (i: Item) => this.viewItem(i)));
    }
    else {
      cols.unshift(new Column('name', 'VM Template Name', 4, 'asc'));
    }

    return cols;
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
    this.item.version = String(this.newVersion);
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
}
