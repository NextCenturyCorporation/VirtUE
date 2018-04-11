import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { filter } from 'rxjs/operators';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';
import { Application } from '../../shared/models/application.model';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';
import { VmAppsService } from '../../shared/services/vm-apps.service';

@Component({
  selector: 'app-virtue-list',
  providers: [ VirtuesService, VirtualMachineService, VmAppsService ],
  templateUrl: './virtue-list.component.html',
  styleUrls: ['./virtue-list.component.css']
})

export class VirtueListComponent implements OnInit, OnDestroy {
  virtue: Virtue[];
  title = 'Virtues';
  virtues = [];
  vmList = [];
  appsList = [];
  virtueTotal: number;
  os: Observable<Array<VirtuesService>>;

  constructor(
    private route: ActivatedRoute,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    private appService: VmAppsService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.virtuesService.getVirtues()
    .subscribe( virtueList => {
      this.virtues = virtueList;
    });
    // this.vmService.getVmList()
    // .subscribe( data  => {
    //   this.vmList = data;
    // });
    this.appService.getAppsList()
    .subscribe( data  => {
      this.appsList = data;
    });
  }

  ngOnDestroy() {
    // this.virtuesService.unsubscribe();
    // this.vmService.unsubscribe();
    // this.appService.unsubscribe();
  }

  getAppName(id: string): void {
    const appName = this.appsList.filter(data => id === data.id);
    appName.map((_, i) => {
      return appName[i].name;
    })
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

  virtueStatus(id: string, virtue: Virtue): void {
    const virtueObj = this.virtues.filter(data => virtue.id === id);
    virtueObj.map((_, i) => {
      virtueObj[i].enabled ? virtueObj[i].enabled = false : virtueObj[i].enabled = true;
      console.log(virtueObj);
    });
    // this.virtuesService.updateVirtue(id, virtueObj);

  }

}
