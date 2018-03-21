import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { ActiveClassDirective } from '../../shared/directives/active-class.directive';

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

  constructor(
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) { }

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
    // console.log('filterValue = ' + status);
    this.filterValue = status;
    this.totalVms = this.vms.length;
  }

  updateStatus(id: string): void {
    const vm = this.vms.filter(data => data['id'] === id);
    vm.map((_, i) => {
      vm[i].enabled ? vm[i].enabled = false : vm[i].enabled = true;
      console.log(vm);
    });
    // this.appsService.update(id, app);
  }

  openDialog(id, type, action, text): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          dialogText: text,
          dialogType: type
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
  // addVM(name: string, status: string) {
  //   this.vms.push({name: name, status: status});
  // }
}
