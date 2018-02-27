import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { Users } from '../../shared/models/users.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css'],
  providers: [VirtuesService, VirtualMachineService]
})
export class CreateVirtueComponent implements OnInit {
  users: Users[];
  virtues: Virtue[];
  vms: VirtualMachine;
  hovering = false;
  activeClass: string;
  vmList = [];
  appList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    if (this.pageVmList.length > 0) {
      this.getVmList();
      // console.log('page VM list @ ngOnInit(): ' + this.pageVmList);
    }
  }

  getVmList() {
    // loop through the selected VM list
    const selectedVm = this.pageVmList;
    // console.log('page VM list @ getVmList(): ' + this.pageVmList);
    this.vmService.getVmList()
      .subscribe(data => {
        if (this.vmList.length < 1) {
          for (let sel of selectedVm) {
            for (let vm of data) {
              if (sel === vm.id) {
                this.vmList.push(vm);
                break;
              }
            }
          }
        } else {
          this.getUpdatedVmList();
        }
      });
  }

  getUpdatedVmList() {
    this.vmList = [];
    this.vmService.getVmList()
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

  createVirtue(virtueName: string) {
    this.getAppList();
    let user = [{ 'username': 'admin', 'authorities': ['ROLE_USER', 'ROLE_ADMIN'] }];
    let newVirtue = [{
      // 'id': 'TEST',
      'name': virtueName,
      'version': '1.0',
      'vmTemplates': this.vmList,
      'users': user,
      'enabled': true,
      'lastEditor': 'skim',
      'applications': this.appList
    }];
    // console.log('New Virtue: ');
    // console.log(newVirtue);

    // this.virtuesService.createVirtue({newVirtue} as Virtue)
    // .subscribe(data => {
    //   this.virtues.push(data);
    // });
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

  removeVm(id: string, index: number): void {
    this.vmList = this.vmList.filter(data => {
      return data.id !== id;
    });
    this.pageVmList.splice(index, 1);
  }

  activateModal(id: string): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px',
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
