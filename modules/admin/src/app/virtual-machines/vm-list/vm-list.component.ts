import { Component, OnInit } from '@angular/core';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-list',
  providers: [ VirtualMachineService ],
  templateUrl: './vm-list.component.html',
  styleUrls: ['./vm-list.component.css']
})
export class VmListComponent implements OnInit {

  vms = [];
  filterValue = '*';
  noListData = false;

  vmlist: string;
  totalVms: number;

  constructor( private vmService: VirtualMachineService ) { }

  ngOnInit() {
    this.getVmList();
  }

  getVmList() {
    this.vmService.getVmList()
      .subscribe(vmlist => {
        this.vms = vmlist;
        this.totalVms = vmlist.length;
      });
  }

  listFilter(status: any) {
    console.log('filterValue = ' + status);
    this.filterValue = status;
    this.totalVms = this.vms.length;
  }

  // addVM(name: string, status: string) {
  //   this.vms.push({name: name, status: status});
  // }
}
