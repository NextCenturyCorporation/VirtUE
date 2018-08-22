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

/*
This inherits from GeneralListComponent, so that it can display the available
items in the same way, with the same filtering/sorting capabilities as the user
can on the list page.
 */

@Component({
  selector: 'generic-modal',
  templateUrl: './generic.modal.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [BaseUrlService, ItemService]
})
export abstract class GenericModal extends GenericListComponent {

  // form: FormGroup;

  getSelections = new EventEmitter();

  //only holds the initial input selections, just passed to table once
  //table loads
  initialSelections: string[] = [];

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      public dialogRef: MatDialogRef<GenericModal>,
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(router, baseUrlService, itemService, dialog);
      if (data && data['selectedIDs']) {
        this.initialSelections = data['selectedIDs'];
      }
      else {
        console.log("No field 'selectedIDs' in data input to modal")
        this.initialSelections = [];
      }

  }

  //remember this component is abstract and so can't fit the onInit interface
  //but all concrete children inherit this function.
  ngOnInit() {
    this.cmnComponentSetup();
    this.fillTable();

    //TODO should we not allow addition of disabled?
    // if so, see note in notes - select-all button will not act how user expects.
    //Could be changed to only add/remove enabled items, but still then the user couldn't
    //remove disabled ones through that menu.
    // this.table.filterList("enabled");
  }

  // this gives the childIDs the item was loaded with, and is only used to build
  // the table - any changes will be made to this.table.selectedIDs.
  getSelectedIDs() {
    console.log("initialSelections", this.initialSelections);
    return this.initialSelections;
  }

  getTableFilters(): {text:string, value:string}[] {
    return [];
  }

  hasCheckbox() {
    return true;
  }

  getOptionsList(): RowOptions[] {
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
