import { Component, OnInit } from '@angular/core';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { JsonFilterPipe } from '../../shared/json-filter.pipe';
import { CountFilterPipe } from '../../shared/count-filter.pipe';

@Component({
  selector: 'app-vm-list',
  providers: [ VirtualMachineService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  noListData = false;
  vmlist: string;
  vms = [];
  vmLength: number;

  constructor( private vmService: VirtualMachineService ) { }

  ngOnInit() {
    this.getVmList();
  }

  getVmList() {
    this.vmService.getVmList()
      .subscribe(vmlist => this.vms = vmlist);
  }

  // addVM(name: string, status: string) {
  //   this.vms.push({name: name, status: status});
  // }
}
