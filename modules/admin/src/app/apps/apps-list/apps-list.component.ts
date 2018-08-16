import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { MatDialog } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { Router } from '@angular/router';

import { Item } from '../../shared/models/item.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { Column } from '../../shared/models/column.model';
import { DictList } from '../../shared/models/dictionary.model';
import { RowOptions } from "../../shared/models/rowOptions.model"

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { Application } from '../../shared/models/application.model';
import { AddAppComponent } from '../add-app/add-app.component';

import { GenericListComponent } from '../../shared/abstracts/gen-list/gen-list.component';

import { ConfigUrlEnum } from '../../shared/enums/enums';

@Component({
  selector: 'apps-list',
  templateUrl: '../../shared/abstracts/gen-list/gen-list.component.html',
  styleUrls: ['../../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService  ]
})
export class AppsListComponent extends GenericListComponent {

  file: string;
  url: string;

  constructor(
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super(router, baseUrlService, itemService, dialog);

    //This defines what columns show up in the table. If supplied, formatValue(i:Item) will be called
    // to get the text for that item for that column. If not supplied, the text will be assumed to be "item.{colData.name}"
    //
    //Note: colWidths of all columns must add to exactly 12.
    //Too low will not scale to fit, and too large will cause columns to wrap, within each row.
    //See note next to a line containing "mui-col-md-12" in gen-list.component.html
    this.colData = [
      {name: 'name', prettyName: 'App Name', isList: false, sortDefault: 'asc', colWidth:5, formatValue: undefined},
      {name: 'version', prettyName: 'Version', isList: false, sortDefault: 'asc', colWidth:3, formatValue: undefined},
      {name: 'os', prettyName: 'Operating System', isList: false, sortDefault: 'desc', colWidth:4, formatValue: undefined}
    ];

    // let optionsList = new RowOptions();

    this.serviceConfigUrl = ConfigUrlEnum.APPS;

    this.prettyTitle = "Available Applications";
    this.itemName = "Application";
    this.pluralItem = "Applications";
    this.noDataMessage = "No apps appear to be available at this time. To add an application, click on the button \"Add "
                          + this.itemName +  "\" above.";
    this.domain = '/applications';

    this.neededDatasets = ["apps"];

    this.showSortingAndEditOptions = false;
  }


  //overrides parent
  getOptionsList() {
    return [
      new RowOptions("Remove", () => true, (i:Item) => this.openDialog('delete', i))
  ];
  }

  //called after all the datasets have loaded
  onPullComplete(): void {
    this.items = this.allApps.asList();
  }

  openAppsDialog(): void {
    let dialogRef = this.dialog.open(AddAppComponent, {
      width: '480px',
      data: { file: this.file, url: this.url }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
}
