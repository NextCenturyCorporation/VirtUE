import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { DataRequestService } from '../../shared/services/dataRequest.service';

import { GenericDataPageComponent } from '../../shared/abstracts/gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';

import { SelectionMode } from '../../shared/abstracts/gen-table/selectionMode.enum';
import { IndexedObj } from '../../shared/models/indexedObj.model';
import { Column } from '../../shared/models/column.model';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
 * @class
 * This class represents a pop-up window, holding a table of selectable items. The user can select
 * or unselect items, and when they click 'Submit', their selections are passed back to whatever component
 * spawned this object.
 *
 * If you're looking to make a new modal, either copy the code from here (or colorModal) or take the time to make
 * "GenericModalComponent" actually define a generic modal, as opposed to a selection modal that can generically hold
 * a list of Item-subclass objects.
 * See the commented-out GenericPageComponent.activateModal() function for the unimplemented design.
 *
 */
@Component({
  selector: 'app-generic-modal',
  providers: [BaseUrlService, DataRequestService]
})
export abstract class GenericModalComponent extends GenericDataPageComponent implements OnInit {

  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent<IndexedObj>;

  /** Appears in the modals title as: 'Add/Remove {pluralItem}' */
  pluralItem: string;


  /** What the containing component watches, to get the user's selections back out of this modal. */
  getSelections = new EventEmitter();

  /**
   * Only holds the initial input selections, is not kept up-to-date.
   * Just used to build a list of initial selections to pass to the table
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
      dataRequestService: DataRequestService,
      dialog: MatDialog,

      /** injected, is a reference to the modal dialog box itself. */
      public dialogRef: MatDialogRef<GenericModalComponent>,

      /** holds the initial selections, and possibly a SelectionMode */
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(router, baseUrlService, dataRequestService, dialog);
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
   * defines the parameters for a table of selectable Items, using subclass-defined functions.
   */
  defaultTableParams() {
    return {
      cols: this.getColumns(),
      filters: [],
      tableWidth: 1,
      noDataMsg: this.getNoDataMsg(),
      editingEnabled: () => true,
      selectionOptions: {
        selectionMode: this.getSelectionMode(),
        equals: (obj1: IndexedObj, obj2: IndexedObj) => (obj1 && obj2 && (obj1.getID() !== undefined) && (obj1.getID() === obj2.getID()))
      }
    };
  }

  /**
   * Sets up the table
   *
   * If all instances of this table should have an attribute, and define it differently, then it should be set via a method in
   * [[defaultTableParams]].
   * If an instance needs a unique attribute that the other pages don't even need to see (like virtue-modal having a getColor field),
   * then that should be added in a customizeTableParams method, in that subclass. See [[VirtueModalComponent.customizeTableParams]]
   */
  setUpTable(): void {
    if (this.table === undefined) {
      return;
    }
    let params = this.defaultTableParams();

    this.customizeTableParams(params);

    this.table.setUp(params);
  }

  /**
   * Allow children to customize the parameters passed to the table. By default, do nothing.
   * @param paramsObject the object to be passed to the table. see [[GenericTableComponent.setUp]]
   */
  customizeTableParams(paramsObject) {}

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
  fillTable(newObjects: IndexedObj[]): void {
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
