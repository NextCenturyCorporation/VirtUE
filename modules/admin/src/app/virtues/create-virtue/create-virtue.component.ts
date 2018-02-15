import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { VirtualMachine } from '../../shared/models/vm.model';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css'],
  providers: [ VirtuesService, VirtualMachineService ]
})
export class CreateVirtueComponent implements OnInit {
  vms = VirtualMachine;
  // test = [
  //   {
  //     id: 'T1E-2S-3T4',
  //     name: 'Alpha ',
  //     os: 'LINUX',
  //     templatePath: 'TESTVM',
  //     applications: [{
  //       id: 'TEST-1-APP',
  //       name: 'TEST APP',
  //       version: '1.0',
  //       os: 'LINUX',
  //       launchCommand: 'TESTAPP'
  //     }]
  //   },
  //   {
  //     id: 'T1E-2S-3T4',
  //     name: 'Beta',
  //     os: 'LINUX',
  //     templatePath: 'TESTVM',
  //     applications: [{
  //       id: 'TEST-1-APP',
  //       name: 'TEST APP',
  //       version: '1.0',
  //       os: 'LINUX',
  //       launchCommand: 'TESTAPP'
  //     }]
  //   }
  // ];

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
    if (this.selVmsList.length > 0){
    this.getVmList();
    }
  }

  getVmList() {
    // loop through the selected VM list
    const selectedVm = this.selVmsList;
    this.vmService.getVmList()
    .subscribe(
      data => {
        for (var sel in selectedVm) {
          for (var i in data) {
            if (selectedVm.length > 0 && data[i].id === selectedVm[sel]){
              this.vmList.push(data[i].applications);
            }
          }
        }
      }
    );
  }

  activateModal(id: string): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '960px',
      data:  {
          vms: id
        }
    });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('This modal was closed');
    });
  }

  onSave() {

  }

}
