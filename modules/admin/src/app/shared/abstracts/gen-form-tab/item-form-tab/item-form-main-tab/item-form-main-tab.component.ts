import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { DialogsComponent } from '../../../../../dialogs/dialogs.component';

import { IndexedObj } from '../../../../models/indexedObj.model';
import { Item } from '../../../../models/item.model';
import { User } from '../../../../models/user.model'; // just for temporary initialization, to prevent error while loading
import { Column } from '../../../../models/column.model';
import { SubMenuOptions } from '../../../../models/subMenuOptions.model';
import { Mode } from '../../../../abstracts/gen-form/mode.enum';

import { GenericTableComponent } from '../../../gen-table/gen-table.component';
import { ItemFormTabComponent } from '../item-form-tab.component';

import { DatasetNames } from '../../../gen-data-page/datasetNames.enum';

/**
 * @class
 * This class represents a 'main' tab for the form page of Users, Virtues, and Vms.
 *
 * As a main tab, it is assumed to list:
 *  - The item's name
 *  - the item's status
 *  - the item's direct children
 *  - other minor info/settings that relate to *what* this item is, and how this template can be used.
 *
 * Everything on how this item should work/be set up, or its connections to other entities, should go in a 'settings' tab, which
 * should be a direct subclass of ItemFormTabComponent.
 *
 */
export abstract class ItemFormMainTabComponent extends ItemFormTabComponent implements OnInit {

  /**
   * A reference to the Item being viewed/edited/etc.
   * Refers to the same object as [[ItemFormComponent.item]]
   * this gets reclassed by children tabs
   */
  protected item: Item;

  childDatasetName: DatasetNames;

  /** A table for listing the item's children. */
  @ViewChild('childrenTable') protected childrenTable: GenericTableComponent<Item>;

  /** to notify the parent item form that one of this.item's lists of child ids has been changed */
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  /**
   * To notify the parent component that the item's status has been toggled.
   * Only meaningful in view mode.
   *
   * it should just emit the new value.
   * It just needs to emit something to let the form know that the user toggled the status.
   * Item's `enabled` field should only be mutable from the view page.
   */
  @Output() onStatusChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  /**
   * see [[ItemFormTabComponent.constructor]] for input notes.
   *
   * Set name on all tabs to "General Info". This can be changed in subclasses if desired.
   */
  constructor(
    router: Router,
    dialog: MatDialog) {
      super(router, dialog);
      this.tabName = "General Info";
      this.item = new User({enabled: false}); // just until data from the backend gets set up.
  }

  /**
   * Note that for the foreseeable future, the status toggle will only be active in view mode.
   *
   * Called whenever the status toggle is clicked, to notify the parent component.
   * The parent components ignore it unless the page is in view mode, in which case they send
   * off a request to the backend actually toggling the status of this item.
   */
  statusChange(): void {
    this.onStatusChange.emit(!this.item.enabled);
  }

  /**
   * See [[ItemFormTabComponent.init]] for generic info
  * This sets the mode and sets up the page's children table.
  *
  * @param mode the mode to set up this tab as being in. Must be passed in.
  */
  init(mode: Mode): void {
    this.setMode(mode);

    this.setUpChildTable();
  }

  /**
   * See [[ItemFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well as fill or re-fill
   * this tab's table with an updated item.children list.
   *
   * This re-builds some parts of the table upon mode update, to allow the columns and row options to change dynamically
   * upon mode change.
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated. The attribute is optional.
   */
  update(changes?: any): void {
    if (changes && changes.mode) {
      this.setMode(changes.mode);
    }
    this.childrenTable.populate(this.item.getRelatedDict(this.childDatasetName).asList());
  }

  /**
   * this brings up the subclass-defined-modal to add/remove children.
   *
   * Note the distinction between this and DialogsComponent;
   * This pops up to display options, or a selectable table, or something. DialogsComponent just checks
   * potentially dangerous user actions.
   */
  activateModal(): void {

    let params = {
      height: '70%',
      width: '70%',
      data: {
        selectedIDs: this.item.getRelatedIDList(this.childDatasetName)
      }
    };

    let dialogRef = this.getDialogRef(params);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedItems) => {
      this.onChildrenChange.emit(selectedItems);
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });

    dialogRef.updatePosition({ top: '5%' });

  }

  /**
   * defines the child table using sub-class-defined-functions.
   */
  defaultChildTableParams() {
    return {
      cols: this.getColumns(),
      filters: [], // don't enable filtering by status on the form's child table.
      tableWidth: 0.75,
      noDataMsg: this.getNoDataMsg(),
      elementIsDisabled: (i: Item) => !i.enabled,
      disableLinks: () => !this.inViewMode(),
      editingEnabled: () => !this.inViewMode()
    };
  }

  /**
   * Sets up the table listing this item's children
   * See [[GenericTableComponent.setUp]]()
   * overridden by [[UserMainTabComponent]]
   */
  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    let params = this.defaultChildTableParams();

    this.customizeTableParams(params);

    this.childrenTable.setUp(params);
  }

  /**
   * Allow children to customize the parameters passed to the table. By default, do nothing.
   * @param paramsObject the object to be passed to the table. see [[GenericTableComponent.setUp]]
   */
  customizeTableParams(paramsObject) {}

  /**
   * Have each subclass define the format of their table.
   * @return a list of columns that should appear in this tab's child table.
   */
  abstract getColumns(): Column[];

  /**
   * See [[TextColumn]] for more details on the submenu.
   *
   * A set of row options, to be displayed on each item in the child table.
   * Generally, we want them to be linkable and viewable only in view mode,
   * and removable only in edit mode.
   *
   * see [[GenericPageComponent.openDialog]] for notes on that openDialog call.
   *
   * @return a list of row options; 'View', 'Edit', and 'Remove'.
   */
  getSubMenu(): SubMenuOptions[] {
    return [
       new SubMenuOptions("View", () => this.inViewMode(), (childItem: Item) => this.viewItem(childItem)),
       new SubMenuOptions("Edit", () => this.inViewMode(), (childItem: Item) => this.editItem(childItem)),
       new SubMenuOptions("Remove",
                      () => !this.inViewMode(),
                      (childItem: Item) => this.openDialog( 'Delete ' + childItem.getName(),
                                                            () => {
                                                              this.item.removeUnspecifiedChild(childItem);
                                                              this.update();
                                                            }
                                                          )
                      )
    ];
  }

  /**
   * @returns a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  abstract getNoDataMsg(): string;

  /**
   * Define the default table width, as a fraction of the screen space.
   * The tables don't need to take up the full screen width, they're usually pretty sparse.
   * Can be overridden if necessary though.
   */
  getTableWidth(): number {
    return 0.75;
  }

  /**
   * Defined by subclasses, so each can load their own type of modal.
   * @param parameters to be passed into the modal
   */
  abstract getDialogRef(params: {
                          /** the height of the modal, in pixels */
                          height: string,
                          /** the width of the modal, in pixels */
                          width: string,
                          /** some type of data object to be passed into the modal - a container */
                          data: any
                        });
}
