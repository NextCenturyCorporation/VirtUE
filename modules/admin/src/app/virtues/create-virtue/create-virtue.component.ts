import { Component, OnInit } from '@angular/core';
import { Routes, RouterModule, Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css'],
  providers: [ BaseUrlService, VirtuesService, VirtualMachineService ]
})
export class CreateVirtueComponent implements OnInit {
  vms: VirtualMachine;
  activeClass: string;
  baseUrl: string;
  users: User[];
  virtues: Virtue[];

  vmList = [];
  appList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private baseUrlService: BaseUrlService,
    private router: Router,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
    });

    if (this.pageVmList.length > 0) {
      this.getVmList();
    }
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
    console.log('URL: ' + url);
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
      this.router.navigate(['/users']);
    }, 500);
  }

  getVmList() {
    const selectedVm = this.pageVmList;
    this.vmService.getVmList(this.baseUrl)
      .subscribe(data => {
        if (this.vmList.length < 1) {
          console.log(this.pageVmList);
          for (let sel of selectedVm) {
            for (let vm of data) {
              if (sel === vm.id) {
                this.vmList.push(vm);
                break;
              }
            }
          }
        } else {
          this.getUpdatedVmList(this.baseUrl);
        }
      });
  }

  getAppList() {
    let vms = this.vmList;
    let apps = [];
    for (let vm of vms) {
      apps = vm.applications;
      for (let app of apps) {
        this.appList.push({
          'name': app.name,
          'version': app.version,
          'os': app.os,
          'launchCommand': app.launchCommand
        });
      }
    }
    // console.log('getAppList():' + this.appList[0].name);
    // return this.appList;
  }
  getUpdatedVmList(baseUrl: string) {
    this.vmList = [];
    this.vmService.getVmList(baseUrl)
      .subscribe(data => {
        for (let sel of this.pageVmList) {
          for (let vm of data) {
            if (sel === vm.id) {
              this.vmList.push(vm.id);
              break;
            }
          }
        }
      });
  }

  createVirtue(virtueName: string) {
    let _virtues = [];
    for (let vm of this.vmList) {
      _virtues.push(vm.id);
    }
    console.log(_virtues);
    let body = [{
      'name': virtueName,
      'version': '1.0',
      'enabled': true,
      'lastEditor': 'skim',
      'virtualMachineTemplateIds': _virtues
    }];
    // console.log('New Virtue: ');
    // console.log(newVirtue);

    this.virtuesService.createVirtue(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        return true;
      },
      error => {
        console.log(error.message);
      });
    
    this.resetRouter();
    this.router.navigate(['/virtues']);
  }


  removeVm(id: string, index: number): void {
    this.vmList = this.vmList.filter(data => {
      return data.id !== id;
    });
    this.pageVmList.splice(index, 1);
  }

  activateModal(id: string): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '800px',
      data: {
        selectedVms: this.pageVmList
      }
    });
    // console.log('VMs sent to dialog: ' + this.pageVmList);
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((data) => {
      this.selVmsList = data;
      // console.log('VMs from dialog: ' + this.selVmsList);
      if (this.pageVmList.length > 0) {
        this.pageVmList = [];
      }
      this.pageVmList = this.selVmsList;

      this.getVmList();
    });

    dialogRef.afterClosed().subscribe(() => {
      vms.unsubscribe();
    });
  }

}
