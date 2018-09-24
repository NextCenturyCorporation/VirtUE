import { Component, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { GenericDataPageComponent } from '../../shared/abstracts/gen-data-page/gen-data-page.component';
import { GenericTableComponent } from '../../shared/abstracts/gen-table/gen-table.component';
import { SELECTION_MODE } from '../../shared/abstracts/gen-table/selectionMode.enum';
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
  templateUrl: './generic.modal.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [BaseUrlService, ItemService]
})
export abstract class GenericModalComponent extends GenericDataPageComponent implements OnInit {


  /** The table itself */
  @ViewChild(GenericTableComponent) table: GenericTableComponent;


  /** a string to appear as the list's title - preferably a full description */
  prettyTitle: string;


  /** What the containing component watches, to get the user's selections back out of this modal. */
  getSelections = new EventEmitter();

  /**
   * Only holds the initial input selections, is not kept up-to-date.
   * Saved temporarily, and passed to table once table loads
   */
  initialSelections: string[] = [];

  /**
   * #uncommented
   */
  mode: SELECTION_MODE;

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

      /** holds the initial selections */
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(router, baseUrlService, itemService, dialog);
      if (data && data['selectedIDs']) {
        this.initialSelections = data['selectedIDs'];
      }
      else {
        console.log("No field 'selectedIDs' in data input to modal");
        this.initialSelections = [];
      }

      this.mode = SELECTION_MODE.MULTI;

      // TODO should we not allow addition of disabled items?
      // if so, note that select-all button will not act how user expects.
      // Could be changed to only add/remove enabled items, but still then the user couldn't
      // remove disabled ones through that menu.
  }

  /**
   * #uncommented
   */
  setMode(mode: SELECTION_MODE) {
    this.mode = mode;
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    this.fillTable();
  }

  /**
   * Sets up the table, according to parameters defined in this class' child classes.
   */
  fillTable(): void {
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
        selectionMode: this.mode,
        getObjectID: (obj: {getID(): string}) => obj.getID(), // just expect something with a getID method
        selectedIDs: this.getSelectedIDs()
      }
    });
  }

  /**
   * Populates the table with the input list of items.
   * Abstracts away table from subclasses
   *
   * @param newItems the list of items to be displayed in the table.
   */
  setItems(newItems: any[]): void {
    this.table.populate(newItems);
  }

  /**
   * This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
   * to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
   *
   * Note: colWidths of all columns must add to exactly 12.
   * Too low will not scale to fit, and too large will cause columns to wrap, within each row.
   *
   * @return a list of columns to be displayed within the table of Items.
   */
  abstract getColumns(): Column[];

  /**
   * @returns a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  abstract getNoDataMsg(): string;

  /**
   * @return whether or not the items being listed have colored labels. True for, and only for, all tables that list virtues.
   * overridden by virtue-list, virtues-modal, main-user-tab, vm-usage-tab, and virtue-settings
   */
  hasColoredLabels(): boolean {
    return false;
  }

  /**
   * this gives the childIDs the item was loaded with, and is only used to build
   * the table - any changes will be made to this.table.selectedIDs.
   *
   * @return a list of item IDs that should be initialized as 'selected' when the table builds.
   */
  getSelectedIDs(): string[] {
    return this.initialSelections;
  }

  /**
   * Notifies the component which created and is waiting on this modal that selections have been made,
   * then clears and closes the modal.
   */
  submit(): void {
    this.getSelections.emit(this.table.selectedIDs);
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
