import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { DialogsComponent } from '../../../../dialogs/dialogs.component';

import { Item } from '../../../models/item.model';
import { User } from '../../../models/user.model';
import { Column } from '../../../models/column.model';
import { SubMenuOptions } from '../../../models/subMenuOptions.model';
import { Mode } from '../../../abstracts/gen-form/mode.enum';

import { GenericTableComponent } from '../../gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../gen-tab/gen-tab.component';


/**
 * @class
 * This class represents a 'main' tab for the form page of Users, Virtues, and Vms.
 *
 * As a main tab, it is assumed to list:
 *  - The item's name
 *  - the item's status
 *  - the item's children
 *  - other minor info/settings that relate to *what* this item is, and how this template can be used.
 * How this item should work/be set up, should go in a 'settings' tab.
 *
 */
export abstract class GenericMainTabComponent extends GenericFormTabComponent implements OnInit {

  /**
   * A reference to the Item being viewed/edited/etc.
   * Refers to the same object as [[GenericFormComponent.item]]
   * this gets reclassed by children tabs
   */
  protected item: Item;

  /** A table for listing the item's children. */
  @ViewChild('childrenTable') protected childrenTable: GenericTableComponent<Item>;

  /** to notify the parent component that a new set of childIDs have been selected */
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  /**
   * To notify the parent component that the item's status has been toggled.
   * Only meaningful in view mode.
   */
  @Output() onStatusChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  /**
   * see [[GenericFormTabComponent.constructor]] for input notes.
   *
   * Set name on all tabs to "General Info". This can be changed in subclasses if desired.
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
    this.tabName = "General Info";
    this.item = new User(undefined);
  }

  /**
   * Called whenever the status toggle is clicked, to notify the parent component.
   * The parent components ignore it unless the page is in view mode, in which case they send
   * off a request to the backend actually toggling the status of this item.
   * This lets the toggle actually work in view mode, without having to save the item as a whole
   * as if you were in edit mode.
   */
  statusChange(): void {
    this.onStatusChange.emit(this.item.enabled);
  }

  /**
   * See [[GenericFormTabComponent.init]] for generic info
  * This sets the mode and sets up the page's children table.
  *
  * @param mode the mode to set up this tab as being in. Must be passed in.
  */
  init(mode: Mode): void {
    this.setMode(mode);

    this.setUpChildTable();
  }

  /**
   * See [[GenericFormTabComponent.update]] for generic info
   * This allows the parent component to update this tab's mode, as well as fill or re-fill
   * this tab's table with an updated item.children list.
   *
   * This re-builds some parts of the table upon mode update, to allow the columns and row options to change dynamically
   * upon mode change.
   *
   * @param changes an object, which should have an attribute `mode: Mode` if
   *                this tab's mode should be updated. The attribute is optional.
   */
  update(changes: any): void {
    this.childrenTable.populate(this.item.children.asList());
    if (changes.mode) {
      // these three lines could be replaced with
      //    this.init(changes.mode)
      // But that might be too opaque.
      this.setMode(changes.mode);
    }
  }

  /**
   * this brings up the subclass-defined-modal to add/remove children.
   *
   * Note the distinction between this and DialogsComponent;
   * This pops up to display options, or a selectable table, or something. DialogsComponent just checks
   * potentially dangerous user actions.
   */
  activateModal(): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let params = {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        name: this.item.getName(), // this currently isn't used; see [[GenericModalComponent]]
        selectedIDs: this.item.childIDs
      }
    };

    let dialogRef = this.getDialogRef(params);

    let sub = dialogRef.componentInstance.getSelections.subscribe((selectedIDs) => {
      this.onChildrenChange.emit(selectedIDs);
    },
    () => { // on error
      sub.unsubscribe();
    },
    () => { // when finished
      sub.unsubscribe();
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });

  }

  /**
   * Sets up the table of children, using sub-class-defined-functions.
   * See [[GenericTable.setUp]]()
   */
  setUpChildTable(): void {
    if (this.childrenTable === undefined) {
      return;
    }

    this.childrenTable.setUp({
      cols: this.getColumns(),
      // opts: this.getSubMenu(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      editingEnabled: () => !this.inViewMode()
    });
  }

  /**
   * Have each subclass define the format of their table.
   * @return a list of columns that should appear in this tab's child table.
   */
  abstract getColumns(): Column[];

  /**
   * See [[GenericListComponent.getSubMenu]] for more details on this sort
   * of method.
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
       new SubMenuOptions("View", () => this.inViewMode(), (i: Item) => this.viewItem(i)),
       new SubMenuOptions("Edit", () => this.inViewMode(), (i: Item) => this.editItem(i)),
       new SubMenuOptions("Remove",
                      () => !this.inViewMode(),
                      (i: Item) => this.openDialog( 'Delete ' + i.getName(),
                                                    () => {this.item.removeChild( i.getID() ); this.update({})}
                                                  )
                      )
    ];
  }

  /**
   * @returns a string to be displayed in the children table, when the table's 'items' array is undefined or empty.
   */
  abstract getNoDataMsg(): string;

  /**
   * Define the default table width, as a # of twefths-of-the-parent-space.
   * The tables don't need to take up the full screen width, they're usually pretty sparse.
   * Can be overridden if necessary though.
   */
  getTableWidth(): number {
    return 9;
  }

  /**
   * Whether or not the Items in the child table have colored labels they should be displayed with.
   * Most main tabs won't have colored labels, so just return false.
   * Overridden by [[UserMainTabComponent.hasColoredLabels]]().
   * See [[GenericTable.setUp]]()
   */
  hasColoredLabels(): boolean {
    return false;
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
