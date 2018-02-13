import { Component, OnInit } from '@angular/core';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { MatDialogRef } from '@angular/material';
import { FormGroup, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-vm-modal',
  templateUrl: './vm-modal.component.html',
  styleUrls: ['./vm-modal.component.css'],
  providers: [ VirtualMachineService ]
})
export class VmModalComponent implements OnInit {

  vmList = [];

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
        data => {this.vmList = data}
      );
  }

  saveVMList() {
    this.dialogRef.close();
  }

}
