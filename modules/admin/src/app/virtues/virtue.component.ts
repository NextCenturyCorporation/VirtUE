import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog, MatSlideToggleModule } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../shared/services/baseUrl.service';
import { ItemService } from '../shared/services/item.service';

import { VmModalComponent } from '../modals/vm-modal/vm-modal.component';

import { Item } from '../shared/models/item.model';
import { Application } from '../shared/models/application.model';
import { VirtualMachine } from '../shared/models/vm.model';
import { Virtue } from '../shared/models/virtue.model';
import { DictList } from '../shared/models/dictionary.model';
import { Column } from '../shared/models/column.model';
import { RowOptions } from '../shared/models/rowOptions.model';

import { Mode, ConfigUrlEnum } from '../shared/enums/enums';

import { VirtueSettingsComponent } from './virtue-settings/virtue-settings.component';

import { GenericFormComponent } from '../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-virtue',
  templateUrl: './virtue.component.html',
  styleUrls: ['../shared/abstracts/gen-list/gen-list.component.css'],
  providers: [ BaseUrlService, ItemService ]
})

export class VirtueComponent extends GenericFormComponent {
  @ViewChild(VirtueSettingsComponent) settingsPane: VirtueSettingsComponent;

  constructor(
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/virtues', activatedRoute, router, baseUrlService, itemService, dialog);


    // set up empty (except for a default color), will get filled in ngOnInit if
    // mode is not 'create'
    this.item = new Virtue({color: this.defaultColor()});

    this.datasetName = 'allVirtues';
    this.childDatasetName = 'allVms';

    this.childDomain = "/vm-templates";
  }

  // This should only stay until the data loads, if the data has a color.
  defaultColor() {
    return this.mode === Mode.CREATE ? "#cccccc" : "transparent";
  }

  // TODO decide if this is sufficent, or if it could be designed better
  updateUnconnectedFields() {
    this.settingsPane.setColor((this.item as Virtue).color);
  }


  getColumns(): Column[] {
    return [
      new Column('name',            'Template Name',        false, 'asc',     4, undefined, (i: Item) => this.editItem(i)),
      // new Column('name',            'Template Name',        false, 'asc',     4),
      new Column('os',              'OS',                   false, 'asc',     2),
      new Column('childNamesHTML',  'Assigned Applications', true, undefined, 4, this.getChildNamesHtml),
      new Column('status',          'Status',               false, 'asc',     2, this.formatStatus)
    ];
  }

  getNoDataMsg(): string {
    return "No virtue templates have been created yet. To add a template, click on the button \"Add Virtue\" above.";
  }

  getPageOptions(): {
    serviceConfigUrl: ConfigUrlEnum,
    neededDatasets: string[]} {
      return {
        serviceConfigUrl: ConfigUrlEnum.VIRTUES,
        neededDatasets: ["apps", "vms", "virtues"]
      };
    }

  // overrides default
  // ensure it takes the full width of its half-page area
  getTableWidth(): number {
    return 12;
  }

  getModal(
    params: {width: string, height: string, data: {id: string, selectedIDs: string[] }}
  ): any {
    return this.dialog.open( VmModalComponent, params);
  }

  // create and fill the fields the backend expects to see, record any
  // uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {

    // TODO perform checks here, so none of the below changes happen if the item
    // isn't valid

    this.item['color'] = this.settingsPane.getColor();
    console.log("color:", this.item['color']);
    if (! this.item['version']) {
      this.item['version'] = '1.0';
    }
    console.log("version:", this.item['version'], "(see what it looks like to type html code here - injection vector?)");

    // The following are required:
    //  this.item.name,     can't be empty
    //  this.item.version,  will be valid
    //  this.item.enabled,  should either be true or false
    //  this.item.color,    should be ok? make sure it has a default in the settings pane
    this.item['virtualMachineTemplateIds'] = this.item.childIDs;

    // TODO update the update date. Maybe? That might be done on the backend
    //  this.item['lastModification'] = new Date().something

    // note that children is set to undefined for a brief instant before the
    // page navigates away, during which time an exception would occur on the
    // table - that chunk of html has now been wrapped in a check, to not check
    // children's list size if children is undefined
    this.item.children = undefined;
    this.item.childIDs = undefined;
    return true;
  }

}
