import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';

import { VirtualMachineService } from '../../shared/services/vm.service';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';

import { MatDialogRef } from '@angular/material';

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
  checked = false;
  indeterminate = false;

  selectedVms: string;
  vmList = [];
  appList = [];

  constructor(
    private vmService: VirtualMachineService,
    private dialogRef: MatDialogRef<VmModalComponent>
  ) {}

  ngOnInit() {
    this.getVmList();
  }

  getVmList() {
    this.vmService.getVmList()
      .subscribe(
        data => {
          this.vmList = data;
        }
      );
  }

  addVms(id: string): void {
    id = id.trim();
    if (!id) {
      this.appList.push(VirtualMachine);
    }
  }

  cancelModal() {
    this.dialogRef.close();
  }
  saveVMList() {
    this.dialogRef.close();
  }

}
