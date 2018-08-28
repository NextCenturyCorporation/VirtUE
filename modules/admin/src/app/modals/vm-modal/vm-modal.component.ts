import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { GenericModal } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VmModalComponent extends GenericModal {

  checked = false;

  selectedIDs: string[] = [];

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<GenericModal>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      super(router, baseUrlService, itemService, dialog, dialogRef, data);

      this.neededDatasets = ["apps", "vms"];
    }

  getColumns(): Column[] {
    return [
          {name: 'name', prettyName: 'App Name', isList: false, sortDefault: undefined, colWidth:5, formatValue: undefined},
          {name: 'os', prettyName: 'Version', isList: false, sortDefault: undefined, colWidth:3, formatValue: undefined},
          {name: 'childNamesHTML', prettyName: 'Assigned Applications', isList: true, sortDefault: undefined, colWidth:4, formatValue: this.getChildNamesHtml}
        ];
  }
  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VMS,
      neededDatasets: ["apps", "vms"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Virtual Machine Templates",
      itemName: "Vm Template",
      pluralItem: "VMs",
      domain: '/vm-templates'
    };
  }

  getNoDataMsg(): string {
    return  "No vms have been added at this time. To add a vm, click on the button \"Add Vm Template\" above.";
  }

  onPullComplete() {
    this.setItems(this.allVms.asList());
  }
}
