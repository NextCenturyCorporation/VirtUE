import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { Application } from '../../shared/models/application.model';
import { VmAppsService } from '../../shared/services/vm-apps.service';

@Component({
  selector: 'app-vm-apps-list',
  providers: [ VmAppsService ],
  templateUrl: './vm-apps-list.component.html',
  styleUrls: ['./vm-apps-list.component.css']
})
export class VmAppsListComponent implements OnInit {

  title = 'Applications';
  filterValue = '*';
  apps = [];

  totalApps: number;
  appsfilter: string;

  constructor(
    private route: ActivatedRoute,
    private appsService: VmAppsService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.appsService.getAppsList()
    .subscribe( appsList => {
      this.apps = appsList;
      this.totalApps = appsList.length;
    });
  }

  listFilter(status: any) {
    console.log('filterValue = ' + status);
    this.filterValue = status;
    this.totalApps = this.apps.length;
  }

}
