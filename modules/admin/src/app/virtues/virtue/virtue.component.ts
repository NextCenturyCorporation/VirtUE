import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Location } from '@angular/common';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { UsersService } from '../../shared/services/users.service';

import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { Item } from '../../shared/models/item.model';
import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Virtue } from '../../shared/models/virtue.model';
import { DictList } from '../../shared/models/dictionary.model';

import { VirtueSettingsComponent } from '../virtue-settings/virtue-settings.component';

import { GenericFormComponent } from '../../shared/abstracts/gen-form/gen-form.component';


@Component({
  selector: 'app-virtue',
  templateUrl: './virtue.component.html',
  // styleUrls: ['./edit-virtue.component.css'],
  providers: [ BaseUrlService, ApplicationsService, VirtualMachineService, VirtuesService, UsersService ]
})

export class VirtueComponent extends GenericFormComponent {
  @ViewChild(VirtueSettingsComponent) settingsPane: VirtueSettingsComponent;

  errorMsg: any;

  constructor(
    activatedRoute: ActivatedRoute,
    router: Router,
    baseUrlService: BaseUrlService,
    usersService: UsersService,
    virtuesService: VirtuesService,
    vmService: VirtualMachineService,
    appsService: ApplicationsService,
    dialog: MatDialog
  ) {
    super('/virtues', activatedRoute, router, baseUrlService, usersService, virtuesService, vmService, appsService, dialog);

    this.updateFuncQueue = [this.pullApps, this.pullVms, this.pullVirtues];

    //set up empty (except for a default color), will get filled in ngOnInit if
    //mode is not 'create'
    this.item = new Virtue({color: this.defaultColor()});

    this.serviceCreateFunc = this.virtuesService.createVirtue;
    this.serviceUpdateFunc = this.virtuesService.updateVirtue;
  }

  //This should only stay until the data loads, if the data has a color.
  defaultColor() {
    return this.mode === "c" ? "#cccccc": "#ffffff"
  }

  //if nothing is passed in, we just want to populate item.children
  updateChildList( newVmIDs? : string[] ) {
    this.item.children = new DictList<Virtue>();

    if (newVmIDs instanceof Array) {
      this.item.childIDs = newVmIDs;
    }

    for (let vmID of this.item.childIDs) {
      this.item.children.add(vmID, this.allVms.get(vmID));
    }
  }

  pullItemData(id: string) {
    this.item = this.allVirtues.get(id);
    this.updateChildList();
    //I don't like this cast but I can't find a better place to set the color
    this.settingsPane.setColor((this.item as Virtue).color);
    this.resetRouter();
    // this.virtuesService.getVirtue(this.baseUrl, id).subscribe(vData => {
    //   if (! vData.color) {
    //     vData.color = vData.enabled ? "#BB00AA" : "#00DD33"
    //   }
    //   if (! vData.virtualMachineTemplateIds) {
    //     vData.virtualMachineTemplateIds = [];
    //   }
    //   if (! vData.version) {
    //     vData.version = '1.0';
    //   }
    //   this.itemData = vData;
    //   this.item = new Virtue(vData);
    //   this.settingsPane.setColor(vData.color);
    //   this.updateChildList(vData.virtualMachineTemplateIds);
    // });
  }

  getAppsList() {
    this.appsService.getAppsList(this.baseUrl).subscribe(data => {
      this.appsList = data;
    });
  }

  getAppName(id: string) {
    const app = this.appsList.filter(data =>  id === data.id);
    if (id !== null) {
      return app[0].name;
    }
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

    // dialogRef.afterClosed().subscribe(() => {
    //   vms.unsubscribe();
    // });
  }

  finalizeItem() {
    this.item['color'] = this.settingsPane.getColor();
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
