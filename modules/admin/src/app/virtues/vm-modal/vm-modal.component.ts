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
  selVmList = [];
  storedVmList = [];


  constructor(
    private route: ActivatedRoute,
    private vmService: VirtualMachineService,
    private dialogRef: MatDialogRef<VmModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
  ) {
    if (this.data['selectedList'].length > 0) {
        this.selVmList = this.data['selectedList'];
        console.log('VM List shared with modal: ' + this.selVmList);
      }
    }

  ngOnInit() {
    this.getVmList();
  }

  getVmList() {
    this.vmService.getVmList()
    .subscribe( data => {
      this.vmList = data;
      // console.log(data);
      for (let vm of data) {
        let storedVm = {
          id: vm.id,
          name: vm.name,
          os: vm.os,
          templatePath: vm.templatePath,
          applications: vm.applications,
          checked: this.checked
        }
        if (this.selVmList.length > 0) {
          for (let sel of this.selVmList) {
            if (storedVm['id'] === sel) {
              storedVm['checked'] = true;
              break;
            } else {
              storedVm['checked'] = false;
            }
          }
        }
        this.storedVmList.push(storedVm);
      }
      this.selVmList = [];
    });
  }

  selectAll(event) {
    if (event) {
      this.checked = true;
      let vms = this.vmList;
      for (let vm of vms) {
        this.selVmList.push(vm.id);
      }
    } else {
      this.checked = false;
      this.clearVmList();
    }
    // console.log(this.selVmList);
  }

  cbVmList(event, sel) {
    if (event) {
      this.selVmList.push(sel);
    } else {
      this.removeVm(sel);
    }
  }

  removeVm(sel) {
    this.selVmList.splice(sel, 1);
  }

  clearVmList() {
    this.selVmList = [];
    // console.log(this.selVmList);
  }

  onAddVms(): void {
    console.log(this.selVmList);
    this.addVms.emit(this.selVmList);
    this.dialogRef.close();
  }

  cancelModal() {
    this.clearVmList();
    this.dialogRef.close();
  }

}
