import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ItemService } from '../../shared/services/item.service';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { DictList } from '../../shared/models/dictionary.model';

import { ConfigUrlEnum } from '../../shared/enums/enums';
import { OSSet } from '../../shared/sets/os.set';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-vm',
  templateUrl: './vm.component.html',
  providers: [ BaseUrlService, ItemService, OSSet ]
})
export class VmComponent extends GenericFormComponent {


  constructor(
    protected osOptions: OSSet,
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    itemService: ItemService,
    dialog: MatDialog
  ) {
    super('/vm-templates', activatedRoute, router, baseUrlService, itemService, dialog);

    this.item = new VirtualMachine(undefined);

    this.updateFuncQueue = [this.pullApps, this.pullVms];
    this.neededDatasets = ["apps", "vms"];

    this.serviceConfigUrl = ConfigUrlEnum.VMS;

    this.datasetName = 'allVms';
    this.childDatasetName = 'allApps';
  }


  activateModal(): void {
    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.item.childIDs
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((dialogAppsList) => {
      this.updateChildList(dialogAppsList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  //create and fill the fields the backend expects to see, record any
  //uncollected inputs, and check that the item is valid to be saved
  finalizeItem(): boolean {

    //TODO perform checks here, so none of the below changes happen if the item
    //isn't valid

    //The following are required:
    // 'id'           should be ok as-is. May be empty if creating new.
    // 'name'         can't be empty
    // 'os'           must be set
    this.item['loginUser'] = 'system'; //TODO does this still exist on the backend?

    //TODO check if necessary, and what the string should be (admin vs administrator)
    // this.item['lastEditor'] = 'administrator';

    // 'enabled'      must be either true or false
    this.item['applicationIds'] = this.item.childIDs;  //may be empty


    //TODO update the update date. Maybe? That might be done on the backend
    // this.item['lastModification'] = new Date().something

    this.item.children = undefined;
    this.item.childIDs = undefined;
    return true;
  }
}
