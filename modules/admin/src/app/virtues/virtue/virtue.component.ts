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
import { Mode } from '../../shared/enums/mode.enum';

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

    this.datasetName = 'allVirtues';
    this.childDatasetName = 'allVms';
  }

  //This should only stay until the data loads, if the data has a color.
  defaultColor() {
    return this.mode === Mode.CREATE ? "#cccccc": "#ffffff"
  }

  //there's probably a better way to do this
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

    // dialogRef.afterClosed().subscribe(() => {
    //   vms.unsubscribe();
    // });
  }


  finalizeItem(): boolean {
    this.item['color'] = this.settingsPane.getColor();
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
