import { Component, OnInit, ViewChild, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';

import { DialogsComponent } from '../../../../dialogs/dialogs.component';

import { Item } from '../../../models/item.model';
import { Column } from '../../../models/column.model';
import { RowOptions } from '../../../models/rowOptions.model';
import { Mode } from '../../../enums/enums';

import { GenericTableComponent } from '../../gen-table/gen-table.component';
import { GenericFormTabComponent } from '../../gen-tab/gen-tab.component';


/**
 * #uncommented
 * @class
 * @extends
 */
export abstract class GenericMainTabComponent extends GenericFormTabComponent implements OnInit {

  /** #uncommented */
  @ViewChild('childrenTable') protected childrenTable: GenericTableComponent;

  /** #uncommented */
  // to notify user.component that a new set of childIDs have been selected
  @Output() onChildrenChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  /** #uncommented */
  @Output() onStatusChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  /**
   * #uncommented
   */
  constructor(router: Router, dialog: MatDialog) {
    super(router, dialog);
  }

  statusChange(): void {
    this.onStatusChange.emit(this.item.enabled);
  }


  /**
   * #uncommented
   * @param
   *
   * @return
   */
  init(): void {
    this.setUpChildTable();
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  update(changes: any): void {
    this.childrenTable.items = this.item.children.asList();
    if (changes.mode) {
      this.setMode(changes.mode);
      this.childrenTable.colData = this.getColumns();
      this.childrenTable.rowOptions = this.getSubMenu();
    }
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  setMode(newMode: Mode): void {
    this.mode = newMode;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   this is a checker, if the user clicks 'remove' on one of the item's children.
   Could be improved/made more clear/distinguished from the "activateModal" method.
  */
  openDialog(action: string, target: Item): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          actionType: action,
          targetObject: target
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    //  control goes here after either "Ok" or "Cancel" are clicked on the dialog
    let sub = dialogRef.componentInstance.getResponse.subscribe((shouldProceed) => {

      if (shouldProceed) {
        if (action === 'delete') {
          this.item.removeChild(target.getID());
        }
      }
    },
    () => {// on error
      sub.unsubscribe();
    },
    () => {// when finished
      sub.unsubscribe();
    });
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  // this brings up the modal to add/remove children
  // this could be refactored into a "MainTab" class, which is the same for all
  // forms, but I'm not sure that'd be necessary.
  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;

    let params = {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: this.item.getName(),
        selectedIDs: this.item.childIDs
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
      opts: this.getSubMenu(),
      coloredLabels: true,
      filters: [], // don't allow filtering on the form's child table.
      tableWidth: 9,
      noDataMsg: this.getNoDataMsg(),
      hasCheckBoxes: false
    });
  }

  /**
   *
   */
  abstract getColumns(): Column[];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  getSubMenu(): RowOptions[] {
    if (this.mode === Mode.VIEW) {
      return [
         new RowOptions("View", () => true, (i: Item) => this.viewItem(i))
      ];
    }
    else {
      return [
         new RowOptions("Remove", () => true, (i: Item) => this.openDialog('delete', i))
      ];
    }
  }

  /**
   * #uncommented
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
   * #uncommented
   * @param
   *
   * @return
   */
  // is class-specific, which modal is called is determined by main-tab subclass.
  abstract getDialogRef(params: {height: string, width: string, data: any});

}
