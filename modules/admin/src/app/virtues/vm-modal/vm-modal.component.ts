import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { Application } from '../../shared/models/application.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
  selector: 'app-vm-modal',
  templateUrl: './vm-modal.component.html',
  providers: [BaseUrlService, VirtualMachineService]
})
export class VmModalComponent implements OnInit {
  @Input() vmInput: VirtualMachine;
  @Input() appInput: Application;

  form: FormGroup;
  checked = false;
  addVms = new EventEmitter();
  vmList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private route: ActivatedRoute,
    private baseUrlService: BaseUrlService,
    private vmService: VirtualMachineService,
    private dialogRef: MatDialogRef<VmModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
      this.pageVmList = data['selectedVms'];
    }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getVmList(awsServer);
    });

    if (this.pageVmList.length > 0) {
      this.selVmsList = this.pageVmList;
    }
  }

  getVmList(baseUrl: string) {
    this.vmService.getVmList(baseUrl)
      .subscribe(vms => {
        this.vmList = vms;
      });

  }

  selectVm(id: string) {
    if (this.pageVmList.length > 0) {
      for (let sel of this.pageVmList) {
        if (sel === id) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  selectAll(event) {
    if (event) {
      this.checked = true;
      for (let vm of this.vmList) {
        this.selVmsList.push(vm.id);
      }
    } else {
      this.checked = false;
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
    this.addVms.emit(this.selVmsList);
    this.clearVmList();
    this.dialogRef.close();
  }

  cancelModal() {
    this.clearVmList();
    this.dialogRef.close();
  }

}
