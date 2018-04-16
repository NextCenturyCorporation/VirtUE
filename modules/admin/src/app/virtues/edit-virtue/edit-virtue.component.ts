import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { Application } from '../../shared/models/application.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';


@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css'],
  providers: [ BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class EditVirtueComponent implements OnInit {
  virtueId: { id: string };
  virtualMachine: VirtualMachine;
  activeClass: string;
  baseUrl: string;
  users: User[];
  virtues: Virtue[];

  virtueData = [];
  vmInfo = [];
  vmList = [];
  appList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private route: ActivatedRoute,
    private baseUrlService: BaseUrlService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    private location: Location,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.virtueId = {
      id: this.route.snapshot.params['id']
    };

    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
        this.getThisVirtue(awsServer);
        if (this.vmList.length > 0) {
          this.getVmList(awsServer);
        }
    });
  }

  getThisVirtue(baseUrl: string) {
    this.baseUrl = baseUrl;
    const id = this.virtueId.id;
    this.virtuesService.getVirtue(baseUrl, id).subscribe(
      data => {
        for (let vObj of data) {
          if (vObj.id === id) {
            this.virtueData = vObj;
            // this.vmList = vObj.virtualMachineTemplateIds;
            for (let vm of vObj.virtualMachineTemplateIds) {
              this.pageVmList.push(vm);
              this.vmList.push(vm);
            }
            break;
          }
        }
      });
  }

  getAllVms(baseUrl: string) {
    this.vmService.getVmList(baseUrl).subscribe(vms => {
      this.vmList = vms;
    });
  }

  getVmList(baseUrl: string) {
    // loop through the selected VM list
    let selectedVm = this.pageVmList;
    this.vmService.getVmList(baseUrl)
    .subscribe(data => {
      if (this.vmList.length < 1) {
        for (let vm of data) {
          for (let sel of selectedVm) {
            if (sel === vm.id) {
              this.vmList.push(vm);
              break;
            }
          }
        }
      } else {
        this.getUpdatedVmList(baseUrl);
      }
    });
  }

  getVmInfo(id: string, prop: string) {
    let vm = this.vmList.filter(data => id === data.id);
    if (id !== null) {
      if (prop === 'name') {
        return vm[0].name;
      } else if (prop === 'os') {
        return vm[0].os;
      }
    }
  }

  getAppList() {
    let vms = this.vmList;
    let apps = [];
    for (let vm of vms) {
      apps = vm.applications;
      // for (let app of apps) {
        // this.appList.push({
        //
        // });
      // }
    }
  }

  getUpdatedVmList(baseUrl: string) {
    this.vmList = [];
    this.vmService.getVmList(baseUrl)
      .subscribe(data => {
        for (let sel of this.pageVmList) {
          for (let vm of data) {
            if (sel === vm.id) {
              this.vmList.push(vm);
              break;
            }
          }
        }
      });
  }

  removeVm(id: string, index: number): void {
    this.vmList = this.vmList.filter(data => {
      return data.id !== id;
    });
    this.pageVmList.splice(index, 1);
  }

  // activateModal(id): void {
  //   let virtueId = id;
  //   let dialogRef = this.dialog.open(VmModalComponent, {
  //     width: '960px'
  //   });
  //
  //   dialogRef.updatePosition({ top: '5%', left: '20%' });
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     // console.log('This modal was closed');
  //   });
  // }
  activateModal(): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px',
      data: {
        selectedVms: this.pageVmList
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((data) => {
      this.selVmsList = data;
      if (this.pageVmList.length > 0) {
        this.pageVmList = [];
      }
      this.pageVmList = this.selVmsList;


      this.getVmList(this.baseUrl);
    });

    dialogRef.afterClosed().subscribe(() => {
      vms.unsubscribe();
    });
  }

  virtueStatus(virtue: Virtue): void {
    // console.log(this.virtueData['enabled']);
    this.virtueData['enabled'] ? this.virtueData['enabled'] = false : this.virtueData['enabled'] = true;
    // this.virtuesService.updateVirtue(this.virtueId.id, this.virtueData);
  }

  deleteVirtue(id): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px'
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      // console.log('This dialog was closed');
    });
  }
}
