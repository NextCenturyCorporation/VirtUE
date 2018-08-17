import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { MatDialog } from '@angular/material';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { DialogsComponent } from '../../dialogs/dialogs.component';
import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrlEnum } from '../../shared/enums/enums';


@Component({
  selector: 'virtue-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})

export class VirtueListComponent extends GenericListComponent {

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);

    this.serviceConfigUrl = ConfigUrlEnum.VIRTUES;

    this.neededDatasets = ["apps", "vms", "virtues"];

    this.prettyTitle = "Virtue Templates";
    this.itemName = "Virtue Template";
    this.pluralItem = "Virtues";
    this.noDataMessage = "No virtues have been added at this time. To add a \
virtue, click on the button \"Add " + this.itemName +  "\" above.";
    this.domain = '/virtues'
  }

  getColumns(): Column[] {
    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    return [
      {name: 'name',            prettyName: 'Template Name',      isList: false,  sortDefault: 'asc', colWidth:2, formatValue: undefined, link:(i:Item) => this.editItem(i)},
      {name: 'childNamesHTML',  prettyName: 'Virtual Machines',   isList: true,   sortDefault: undefined, colWidth:2, formatValue: this.getChildNamesHtml},
      {name: 'apps',            prettyName: 'Applications',       isList: true,   sortDefault: undefined, colWidth:2, formatValue: this.getGrandchildrenHtmlList},
      {name: 'lastEditor',      prettyName: 'Last Editor',        isList: false,  sortDefault: 'asc', colWidth:2, formatValue: undefined},
      {name: 'version',         prettyName: 'Version',            isList: false,  sortDefault: 'asc', colWidth:1, formatValue: undefined},
      {name: 'modDate',         prettyName: 'Modification Date',  isList: false,  sortDefault: 'desc', colWidth:2, formatValue: undefined},
      {name: 'status',          prettyName: 'Status',             isList: false,  sortDefault: 'asc', colWidth:1, formatValue: this.formatStatus}
    ];
  }
  //called after all the datasets have loaded
  onPullComplete(): void {
    this.setItems(this.allVirtues.asList());
  }

  //overrides parent
  hasColoredLabels() {
    return true;
  }
}
