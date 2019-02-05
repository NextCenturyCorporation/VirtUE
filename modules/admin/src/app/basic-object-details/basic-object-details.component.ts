import { Component, OnInit, ViewChild } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';

import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';

import { IndexedObj } from '../shared/models/indexedObj.model';
import { Column } from '../shared/models/column.model';
import { SubMenuOptions } from '../shared/models/subMenuOptions.model';
import { DictList } from '../shared/models/dictionary.model';

import { GenericDataPageComponent } from '../shared/abstracts/gen-data-page/gen-data-page.component';
import { SimpleTableComponent } from '../shared/abstracts/gen-table/simple-table/simple-table.component';

import { RouterService } from '../shared/services/router.service';
import { BaseUrlService } from '../shared/services/baseUrl.service';
import { DataRequestService } from '../shared/services/dataRequest.service';

import { DatasetNames } from '../shared/abstracts/gen-data-page/datasetNames.enum';
import { Mode } from '../shared/abstracts/gen-form/mode.enum';

/**
* @class
 */
@Component({
  selector: 'app-basic-object-details',
  templateUrl: './basic-object-details.component.html',
  styleUrls: ['../shared/abstracts/item-list/item-list.component.css']
})
export class BasicObjectDetailsComponent extends GenericDataPageComponent implements OnInit {

  @ViewChild(SimpleTableComponent) table: SimpleTableComponent;

  /** a string to appear as the list's title - preferably a full description */
  prettyTitle: string;

  neededDataset: DatasetNames;

  objectBeingExamined: any;

  objectID: string;

  /**
   * see [[GenericPageComponent.constructor]] for notes on parameters
   */
  constructor(
    routerService: RouterService,
    baseUrlService: BaseUrlService,
    dataRequestService: DataRequestService,
    dialog: MatDialog,
    private activatedRoute: ActivatedRoute
  ) {
    super(routerService, baseUrlService, dataRequestService, dialog);


    this.prettyTitle = "";

    let datasetName = this.getRouteParam("dataset").toUpperCase();
    if (datasetName in DatasetNames) {
      this.neededDataset = DatasetNames[datasetName];
    }
    // re-call this since the original call gets made in super(), before the activatedRoute object has been instantialized
    this.neededDatasets = this.getNeededDatasets();
    this.objectID = this.getRouteParam("id");
  }

  /**
   * Called automatically on page render.
   */
  ngOnInit(): void {
    this.cmnDataComponentSetup();
    console.log(this['table']);
  }


  /**
   * called after all the datasets have loaded. Pass the app list to the table.
   */
  onPullComplete(): void {
    if ((this.neededDataset === undefined) || !(this.datasets[this.neededDataset].has(this.objectID))) {
      return;
    }
    let objects = this.datasets[this.neededDataset];
    this.objectBeingExamined = objects.get(this.objectID);


    let attributeList = [];

    for (let attribute in this.objectBeingExamined) {
      if (this.objectBeingExamined.hasOwnProperty(attribute)) {
        attributeList.push( {[attribute]: this.objectBeingExamined[attribute]} );
      }
    }

    this.table.setItems(attributeList);

    if ( 'getName' in this.objectBeingExamined) {
      this.prettyTitle = this.objectBeingExamined.getName();
    }
    else if ('name' in this.objectBeingExamined) {
      this.prettyTitle = this.objectBeingExamined['name'];
    }
    else {
      this.prettyTitle = this.neededDataset + " object: " + this.objectID;
    }

    this.routerService.submitPageTitle(this.prettyTitle);
  }

  /**
   * @returns a string to be displayed in the table, when the table's 'items' array is undefined or empty.
   */
  getNoDataMsg(): string {
    return "";
  }

  getRouteParam(paramName: string): string {
    return this.activatedRoute.snapshot.params[paramName];
  }

  getNeededDatasets() {
    return [this.neededDataset]
  }

}
