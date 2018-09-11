import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';
import { Item } from '../../shared/models/item.model';
import { Column } from '../../shared/models/column.model';
import { RowOptions } from '../../shared/models/rowOptions.model';

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/**
 * This inherits from GeneralListComponent, so that it can display the available
 * items in the same way, with the same filtering/sorting capabilities as the user
 * can on the list page.

 * #uncommented
 * @class
 * @extends
 */
@Component({
  selector: 'app-generic-modal',
  templateUrl: './generic.modal.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [BaseUrlService, ItemService]
})
export abstract class GenericModalComponent extends GenericListComponent implements OnInit {
  /**
   * What the containing component watches, to get the user's selections back out of this modal.
   */
  getSelections = new EventEmitter();

  /**
   * Only holds the initial input selections, is not kept up-to-date.
   * Saved temporarily, and passed to table once table loads
   */
  initialSelections: string[] = [];

  /**
   * @param router injected, see parent
   * @param baseUrlService injected, see parent
   * @param itemService injected, see parent
   * @param dialog injected, see parent
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


      // TODO should we not allow addition of disabled items?
      // if so, note that select-all button will not act how user expects.
      // Could be changed to only add/remove enabled items, but still then the user couldn't
      // remove disabled ones through that menu.
  }

  /**
   * @return an empty list, because filters won't be very useful here until they can do more than filter on status
   */
  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  /**
   * @return true - all modals at the moment are for selection of a set of Items.
   */
  hasCheckbox() {
    return true;
  }

  /**
   * @return an empty list - no need for a submenu on Items in a modal at the moment
   */
  getSubMenu(): RowOptions[] {
    return [];
  }

  /**
   * Notifies the component which created and is waiting on this modal that selections have been made,
   * then clears and closes the modal.
   */
  submit(): void {
    this.getSelections.emit(this.table.selectedIDs);
    this.table.clearSelections();
    this.dialogRef.close();
  }

  /**
   * Clears and closes the modal.
   */
  cancel() {
    this.table.clearSelections();
    this.dialogRef.close();
  }

}
