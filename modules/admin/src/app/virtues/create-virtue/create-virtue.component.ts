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
  providers: [ VirtuesService, VirtualMachineService ]
})
export class CreateVirtueComponent implements OnInit {
  users: Users[];
  virtues: Virtue[];
  vms = VirtualMachine;
  hovering = false;
  activeClass: string;
  vmList = [];
  appList = [];
  selVmsList = [];
  // selVmsList = [
  //   'bd3d540b-d375-4b2f-a7b7-e14159fcb60b',
  //   'be0ca662-5203-4ef4-876a-9999cb30308e'
  // ];

  constructor(
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    if (this.selVmsList.length > 0) {
      this.getVmList();
    }
  }

  getVmList() {
    // loop through the selected VM list
    const selectedVm = this.selVmsList;
    this.vmService.getVmList()
      .subscribe(
        data => {
        for (let sel of selectedVm) {
          for (let vm of data) {
            if (sel === vm.id) {
              this.vmList.push(vm);
              break;
            }
          }
        }
        // console.log(this.vmList);
    });
  }

  createVirtue(virtueName: string) {
    this.getAppList();
    const dt = new Date();
    const vms = this.vmList;
    for (let vm of vms){
      console.log('VMs: ');
      console.log(vm);
    }
    console.log( `Virtue Name: ${virtueName} | Create Date: ${ dt.getTime() } `);
    let user = [{ 'username': 'admin','authorities':['ROLE_USER','ROLE_ADMIN'] }]
    let newVirtue = [ {
      // 'id': 'TEST',
      'name': virtueName,
      'version':'1.0',
      'vmTemplates': this.vmList,
      'users': user,
      'enabled': true,
      'lastModification': dt.getTime(),
      'lastEditor': 'skim',
      'applications': this.appList
    } ];
    // console.log( `Virtue Name: ${virtueName} | Create Date: ${ dt } `);
    console.log('New Virtue: ');
    console.log(newVirtue);
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
          'id':app.id,
          'name':app.name,
          'version':app.version,
          'os':app.os,
          'launchCommand':app.launchCommand
        });
      }
    }
    console.log('getAppList():'+this.appList[0].name);
    // return this.appList;
  }

  removeVm(id: string, vm: VirtualMachine): void {
    this.vmList = this.vmList.filter(data => vm.id !== id);
    // console.log(this.vmList);
  }

  activateModal(id: string): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px'
    });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((data) => {
      this.selVmsList = data;
      this.getVmList();
    });

    dialogRef.afterClosed().subscribe(() => {
      vms.unsubscribe();
    });
  }

}
