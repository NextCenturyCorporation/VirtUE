import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { GenericModal } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class AppsModalComponent extends GenericModal {

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<GenericModal>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    super(router, baseUrlService, itemService, dialog, dialogRef, data);

    //must add up to 11 here, to leave space for checkbox
    this.colData = [
      {name: 'name', prettyName: 'App Name', isList: false, sortDefault: 'asc', colWidth:4, formatValue: undefined},
      {name: 'version', prettyName: 'Version', isList: false, sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'os', prettyName: 'Operating System', isList: false, sortDefault: 'desc', colWidth:4, formatValue: undefined}
    ];
    this.neededDatasets = ["apps"];
  }

  onPullComplete() {
    this.items = this.allApps.asList();
  }

}
