import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { Item } from '../../shared/models/item.model';
import { GenericModalComponent } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VmModalComponent extends GenericModalComponent implements OnInit {

  checked = false;

  selectedIDs: string[] = [];

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<VmModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
    ) {
      super(router, baseUrlService, itemService, dialog, dialogRef, data);

      this.neededDatasets = ["apps", "vms"];
    }

  getColumns(): Column[] {
    return [
      // arguments are: {name: str, prettyName: str, isList: bool, sortDefault: str, colWidth: num, formatValue?: func, link?: func}
      new Column('name',            'Template Name',        false, 'asc',     5, undefined, (i: Item) => this.editItem(i)),
      new Column('os',              'OS',                   false, 'asc',     3),
      new Column('childNamesHTML',  'Assigned Applications', true, undefined, 4, this.getChildNamesHtml),
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
