import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { GenericDataPageComponent } from '../../shared/abstracts/gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { SelectionMode } from '../../shared/abstracts/gen-table/selectionMode.enum';
import { Item } from '../../shared/models/item.model';
import { Column } from '../../shared/models/column.model';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
 * @class
 * This class represents a pop-up window, holding a table of selectable items. The user can select
 * or unselect items, and when they click 'Submit', their selections are passed back to whatever component
 * spawned this object.
 *
 * @extends [[GenericListComponent]] so that it can display the available items in the same way,
 * with the same filtering/sorting capabilities available to the user on any of the list pages.
 */
@Component({
  selector: 'app-generic-modal',
  providers: [BaseUrlService, ItemService]
})
export abstract class GenericModalComponent extends GenericDataPageComponent implements OnInit {


  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent<Item>;


  /** a string to appear as the list's title - preferably a full description */
  prettyTitle: string;


  /** What the containing component watches, to get the user's selections back out of this modal. */
  getSelections = new EventEmitter();

  /**
   * Only holds the initial input selections, is not kept up-to-date.
   * Just used to build a list of initial selections to pass ot the table
   */
  selectedIDs: string[] = [];

  /**
   * The standard SelectionMode to set up this modal's table in. Most modals want multiple selection.
   */
  defaultSelectionMode: SelectionMode = SelectionMode.MULTI;

  /**
   * see [[GenericPageComponent.constructor]] for notes on inherited parameters
   *
   * @param dialogRef injected, is a reference to the modal dialog box itself.
   * @param data is defined in calling component, holds the initial selections
   */
  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,

      /** injected, is a reference to the modal dialog box itself. */
      public dialogRef: MatDialogRef<GenericModalComponent>,

      /** holds the initial selections, and possibly a SelectionMode */
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(router, baseUrlService, itemService, dialog);
      if (data && data['selectedIDs']) {
        this.selectedIDs = data['selectedIDs'];
      }
      else {
        console.log("No field 'selectedIDs' in data input to modal");
        this.selectedIDs = [];
      }

      if (data && data['selectionMode']) {
        this.defaultSelectionMode = data.selectionMode;
      }

      // TODO should we not allow addition of disabled items?
      // if so, note that select-all button will not act how user expects.
      // Could be changed to only add/remove enabled items, but still then the user couldn't
      // remove disabled ones through that menu.
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.setUpTable();
  }

  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  setUpTable(): void {
    if (this.table === undefined) {
      return;
    }
    this.table.setUp({
      cols: this.getColumns(),
      coloredLabels: this.hasColoredLabels(),
      filters: [],
      tableWidth: 12,
      noDataMsg: this.getNoDataMsg(),
      selectionOptions: {
        selectionMode: this.getSelectionMode(),
        equals: (obj1: Item, obj2: Item) => {return obj1 && obj2 && (obj1.getID() !== undefined) && (obj1.getID() === obj2.getID())}
      }
    });
  }

  /**
   * Sets the page's selection mode: can be {OFF, SINGLE, MULTI}. Default is MULTI.
   * Subclasses should override this function, if they need a different mode.
   * @return the selection mode to set this page in
   */
  getSelectionMode() {
    return this.defaultSelectionMode;
  }

  /**
   * Populates the table with the input list of objects.
   * Abstracts away table from subclasses
   *
   * To make this whole class truly generic, we need to make this take its list of options from its callers.
   * Also define the above equals in subclasses, and make this simply take in a list of objects to show, and selections.
   * Simply pass those selections up to the table.
   *
   *
   * @param newObjects the list of objects to be displayed in the table.
   */
  fillTable(newObjects: Item[]): void {
    this.table.populate(newObjects);
    let selected = [];
    for (let ID of this.selectedIDs) {
      for (let item of newObjects) {
        if (item.getID() === ID) {
          selected.push(item);
        }
      }
    }


    this.table.setSelections(selected);
  }

  /**
   * This defines what columns show up in the table. See notes on [[Column]] types.
   *
   * @return a list of columns to be displayed within the table.
   */
  abstract getColumns(): Column[];

  /**
   * @returns a string to be displayed in the table, when the table's 'elements' array is undefined or empty.
   */
  abstract getNoDataMsg(): string;

  /**
   * @return whether or not the elements being listed have colored labels. True for, and only for, all tables that list virtues.
   * overridden to that effect by virtue-list, virtues-modal, main-user-tab, vm-usage-tab, and virtue-settings
   */
  hasColoredLabels(): boolean {
    return false;
  }

  /**
   * Notifies the component which created and is waiting on this modal that selections have been made,
   * then clears and closes the modal.
   */
  submit(): void {
    this.selectedIDs = [];

    for (let i of this.table.getSelections()) {
      this.selectedIDs.push(i.getID());
    }

    this.getSelections.emit(this.selectedIDs);
    this.table.clear();
    this.dialogRef.close();
  }

  /**
   * Clears and closes the modal.
   */
  cancel() {
    this.table.clear();
    this.dialogRef.close();
  }

}
