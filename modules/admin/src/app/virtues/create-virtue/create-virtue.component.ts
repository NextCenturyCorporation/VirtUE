import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
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
  hovering = false;
  activeClass: string = '';
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
            if (selectedVm[sel] === data[i].id) {
              this.vmList.push(data[i]);
              break;
            }
          }
        }
    });
  }
  // showMenu($event){
  //   // console.log($event);
  //   this.active = $event.type == 'mouseover' ? 'active' : '';
  // }

  removeVm(id: string, vm: VirtualMachine): void {
    this.vmList = this.vmList.filter(vm => vm.id !== id);
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
