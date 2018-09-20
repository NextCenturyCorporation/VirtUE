import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';
import { Item } from '../../shared/models/item.model';
import { Column } from '../../shared/models/column.model';
import { SubMenuOptions } from '../../shared/models/subMenuOptions.model';

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

/*
This inherits from GeneralListComponent, so that it can display the available
items in the same way, with the same filtering/sorting capabilities as the user
can on the list page.
 */
@Component({
  selector: 'app-generic-modal',
  templateUrl: './generic.modal.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [BaseUrlService, ItemService]
})
export abstract class GenericModalComponent extends GenericListComponent implements OnInit {

  getSelections = new EventEmitter();

  // only holds the initial input selections, just passed to table once
  // table loads
  initialSelections: string[] = [];

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      public dialogRef: MatDialogRef<GenericModalComponent>,
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
   * this gives the childIDs the item was loaded with, and is only used to build
   * the table - any changes will be made to this.table.selectedIDs.
   *
   * @return a list of item IDs that should be initialized as 'selected' when the table builds.
   */
  getSelectedIDs(): string[] {
    return this.initialSelections;
  }

  getTableFilters(): {text: string, value: string}[] {
    return [];
  }

  hasCheckbox() {
    return true;
  }

  /**
   * @return an empty list - no need for a submenu on Items in a modal at the moment
   */
  getSubMenu(): SubMenuOptions[] {
    return [];
  }


  submit(): void {
    this.getSelections.emit(this.table.selectedIDs);
    this.table.clearSelections();
    this.dialogRef.close();
  }

  cancel() {
    this.table.clearSelections();
    this.dialogRef.close();
  }

}
