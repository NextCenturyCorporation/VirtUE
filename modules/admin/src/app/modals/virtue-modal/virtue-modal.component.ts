import { Component, ElementRef, EventEmitter, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { Column } from '../../shared/models/column.model';
import { GenericModal } from '../generic-modal/generic.modal';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

@Component({
  selector: 'app-virtue-modal',
  templateUrl: '../generic-modal/generic.modal.html',
  styleUrls: ['../generic-modal/generic.modal.css'],
  providers: [ BaseUrlService, ItemService ]
})
export class VirtueModalComponent extends GenericModal {

  constructor(
      router: Router,
      baseUrlService: BaseUrlService,
      itemService: ItemService,
      dialog: MatDialog,
      dialogRef: MatDialogRef<VirtueModalComponent>,
      @Inject( MAT_DIALOG_DATA ) data: any
  ) {
    super(router, baseUrlService, itemService, dialog, dialogRef, data);
    this.neededDatasets = ["apps", "vms", "virtues"];
  }

  getColumns(): Column[] {
    return [
      {name: 'name',            prettyName: 'Template Name',      isList: false,  sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'childNamesHTML',  prettyName: 'Virtual Machines',   isList: true,   sortDefault: undefined, colWidth:3, formatValue: this.getChildNamesHtml},
      {name: 'apps',            prettyName: 'Applications',       isList: true,   sortDefault: undefined, colWidth:3, formatValue: this.getGrandchildrenHtmlList},
      {name: 'modDate',         prettyName: 'Modification Date',  isList: false,  sortDefault: 'desc', colWidth:2, formatValue: undefined},
      {name: 'status',          prettyName: 'Status',             isList: false,  sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];
  }

  hasColoredLabels() {
    return true;
  }

  onPullComplete() {
    this.setItems(this.allVirtues.asList());
  }

  getPageOptions(): {
      serviceConfigUrl: ConfigUrlEnum,
      neededDatasets: string[]} {
    return {
      serviceConfigUrl: ConfigUrlEnum.VIRTUES,
      neededDatasets: ["apps", "vms", "virtues"]
    };
  }

  getListOptions(): {
      prettyTitle: string,
      itemName: string,
      pluralItem: string,
      domain: string} {
    return {
      prettyTitle: "Virtue Templates",
      itemName: "Virtue Template",
      pluralItem: "Virtues",
      domain: '/virtues'
    };
  }

  getNoDataMsg(): string {
    return "No virtues have been added at this time. To add a virtue, click on the button \"Add Virtue Template\" above.";
  }
}
