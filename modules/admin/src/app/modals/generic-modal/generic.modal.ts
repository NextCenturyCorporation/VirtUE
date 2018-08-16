import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';
import { Item } from '../../shared/models/item.model';
import { Column } from '../../shared/models/column.model';

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
  providers: [BaseUrlService, ItemService]
})
export class GenericModal extends GenericListComponent {

  form: FormGroup;

  getSelections = new EventEmitter();

  selectedIDs: string[] = [];

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      public dialogRef: MatDialogRef<GenericModal>,
      @Inject(MAT_DIALOG_DATA) public data: any
    ) {
      super(router, baseUrlService, itemService, dialog);
      // console.log(data);
      if (data && data['selectedIDs']) {
        this.selectedIDs = data['selectedIDs'];
      }
      else {
        console.log("No field 'selectedIDs' in data input to modal")
        this.selectedIDs = [];
      }
      console.log(this.selectedIDs);
    }

  onPullComplete() {
    //overridden by children
  }

  isSelected(id: string) {
    return id in this.selectedIDs;
  }

  //TODO look at the html in all modals for check-boxes and selection.
  //Please.

  selectAll(event) {
    console.log(event);
    if (event) {
      for (let i of this.items) {
        this.selectedIDs.push(i.id);
      }
    } else {
      this.clearItemList();
    }
  }

  //called upon check/uncheck
  cbVmList(checked: boolean, id: string) {
    if (checked === true) {
      this.selectedIDs.push(id);
    } else {
      this.selectedIDs.splice(this.selectedIDs.indexOf(id), 1);
    }
  }

  clearItemList() {
    this.selectedIDs = [];
  }

  submit(): void {
    this.getSelections.emit(this.selectedIDs);
    this.clearItemList();
    this.dialogRef.close();
  }

  cancel() {
    this.clearItemList();
    this.dialogRef.close();
  }

}
