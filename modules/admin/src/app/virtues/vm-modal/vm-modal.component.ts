import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { VirtualMachineService } from '../../shared/services/vm.service';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';

import { MatDialogRef, MAT_DIALOG_DATA  } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: './vm-modal.component.html',
  styleUrls: ['./vm-modal.component.css'],
  providers: [ VirtualMachineService ]
})
export class VmModalComponent implements OnInit {
  @Input() vmInput: VirtualMachine;
  @Input() appInput: Application;

  form: FormGroup;
  virtueId: string;
  checked = false;
  disabled = false;
  addVms = new EventEmitter();
  vmList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private route: ActivatedRoute,
    private vmService: VirtualMachineService,
    private dialogRef: MatDialogRef<VmModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
  ) {
      this.pageVmList = data['selectedVms'];
    }

  ngOnInit() {
    this.getVmList();
    if (this.pageVmList.length > 0) {
      this.selVmsList = this.pageVmList;
    }
  }

  getVmList() {
    this.vmService.getVmList()
    .subscribe( vms => {
        this.vmList = vms;
      }
    );
  }
  selectVm(id:string) {
    if (this.pageVmList.length > 0) {
      for (let sel of this.pageVmList) {
        if (sel === id) {
          return true;
          break;
        }
      }
    } else {
      return false;
    }
  }
  selectAll(event) {
    if (event) {
      return true;
      let vms = this.vmList;
      for (let vm of vms) {
        this.selVmsList.push(vm.id);
      }
    } else {
      return false;
      this.clearVmList();
    }
  }

  cbVmList(event, id: string, index: number) {
    if (event === true) {
      this.selVmsList.push(id);
    } else {
      this.removeVm(id, index);
    }
  }

  removeVm(id: string, index: number) {
    this.selVmsList.splice(this.selVmsList.indexOf(id), 1);
  }

  clearVmList() {
    this.selVmsList = [];
    this.pageVmList = [];
  }

  onAddVms(): void {
    // if (this.pageVmList.length > 0) {
    //   this.selVmsList = this.pageVmList;
    // }
    this.addVms.emit(this.selVmsList);
    this.clearVmList();
    this.dialogRef.close();
  }

  cancelModal() {
    this.clearVmList();
    this.dialogRef.close();
  }

}
