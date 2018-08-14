import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { DictList } from '../../shared/models/dictionary.model';

import { Mode, ConfigUrlEnum } from '../../shared/enums/enums';

import { VirtueSettingsComponent } from '../virtue-settings/virtue-settings.component';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-virtue',
  templateUrl: './virtue.component.html',
  // styleUrls: ['./edit-virtue.component.css'],
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

    this.serviceConfigUrl = ConfigUrlEnum.VIRTUES;

    this.updateFuncQueue = [this.pullApps, this.pullVms, this.pullVirtues];
    this.neededDatasets = ["apps", "vms", "virtues"];

    //set up empty (except for a default color), will get filled in ngOnInit if
    //mode is not 'create'
    this.item = new Virtue({color: this.defaultColor()});

    this.datasetName = 'allVirtues';
    this.childDatasetName = 'allVms';
  }

  //This should only stay until the data loads, if the data has a color.
  defaultColor() {
    return this.mode === Mode.CREATE ? "#cccccc": "#ffffff"
  }

  //TODO decide if this is sufficent, or if it could be designed better
  updateUnconnectedFields() {
    this.settingsPane.setColor((this.item as Virtue).color);
  }

  activateModal(): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px',
      data: {
        selectedVms: this.item.childIDs
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((dialogvmIDs) => {
      this.updateChildList(dialogvmIDs);
    });

    //TODO look at unsubscriptions, everywhere things are subscribed to.
    //Apparently angular has a bug where subscriptions aren't always automatically
    //destroyed when their containing component is destroyed.
    //May be the cause of the possible memory-leak like thing in firefox.
    // dialogRef.afterClosed().subscribe(() => {
    //   vms.unsubscribe();
    // });
  }


  //create and fill the fields the backend expects to see, record any
  //uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {

    //TODO perform checks here, so none of the below changes happen if the item
    //isn't valid

    this.item['color'] = this.settingsPane.getColor();
    console.log("color:", this.item['color']);
    if (! this.item['version']) {
      this.item['version'] = '1.0';
    }
    console.log("version:", this.item['version'], "(see what it looks like to type html code here - injection vector?)");

    //The following are required:
    // this.item.name,     can't be empty
    // this.item.version,  will be valid
    // this.item.enabled,  should either be true or false
    // this.item.color,    should be ok? make sure it has a default in the settings pane
    this.item['virtualMachineTemplateIds'] = this.item.childIDs;

    //TODO update the update date. Maybe? That might be done on the backend
    // this.item['lastModification'] = new Date().something

    this.item.children = undefined;
    this.item.childIDs = undefined;
    return true;
  }

  // deleteVirtue(id): void {
  //   let dialogRef = this.dialog.open(DialogsComponent, {
  //     width: '450px'
  //   });
  //
  //   dialogRef.updatePosition({ top: '15%', left: '36%' });
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     // console.log('This dialog was closed');
  //   });
  // }
}
