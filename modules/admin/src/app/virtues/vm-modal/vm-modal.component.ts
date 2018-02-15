import { Component, EventEmitter, Inject, Input, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
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
  @Input() vmInput : VirtualMachine;
  @Input() appInput : Application;

  form: FormGroup;
  virtueId: string;
  checked = false;
  addVms = new EventEmitter();
  vmList = [];
  selVmList = [];

  constructor(
    private route: ActivatedRoute,
    private vmService: VirtualMachineService,
    private dialogRef: MatDialogRef<VmModalComponent>,
    @Inject( MAT_DIALOG_DATA ) public data: any
  ) {  }

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
  selectAll(event) {
    if(event){
      this.checked = true;
    } else {
      tthis.checked = false;
    }
  }
  cbVmList(event, sel) {
    if(event){
      this.selVmList.push(sel);
    } else {
      this.selVmList.remove(sel);
    }
    console.log(this.selVmList);
  }
  onAddVms(): void {
    if (!id){
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
