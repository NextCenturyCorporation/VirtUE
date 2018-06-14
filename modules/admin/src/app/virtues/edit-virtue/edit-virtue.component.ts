import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import {  HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { VmModalComponent } from '../vm-modal/vm-modal.component';


@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class EditVirtueComponent implements OnInit {
  virtueId: { id: string };
  virtueForm: FormControl;
  virtueEnabled: boolean;
  activeClass: string;
  baseUrl: string;
  virtue: any;
  errorMsg: any;
  users: User[];
  virtues: Virtue[];

  virtueData = [];
  vmInfo = [];
  vmList = [];
  appsList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    private location: Location,
    public dialog: MatDialog
  ) {
      this.virtueForm = new FormControl();
    }

  ngOnInit() {
    this.virtueId = {
      id: this.activatedRoute.snapshot.params['id']
    };

    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };

    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getThisVirtue(awsServer, this.virtueId.id);
      this.getAppsList(awsServer);
      if (this.pageVmList.length > 0) {
        this.getVirtueVmList(this.pageVmList);
      }
    });
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  getThisVirtue(baseUrl: string, id: string) {
    this.virtuesService.getVirtue(baseUrl, id).subscribe(data => {
      this.virtueData = data;
      this.pageVmList = data.virtualMachineTemplateIds;
      this.virtueEnabled = data.enabled;
      this.getVirtueVmList(data.virtualMachineTemplateIds);
    });
  }

  getVirtueVmList(virtueVms: any) {
    // loop through the selected VM list
    let selectedVm = this.pageVmList;
    for (let id of selectedVm) {
      this.vmService.getVM(this.baseUrl, id).subscribe(
        data => {
          this.vmList.push(data);
        },
        error => {
          console.log(error.message);
        }
      );
    }
  }

  getVmInfo(id: string, prop: string) {
    let vm = this.vmList.filter(data => id === data.id);
    if (id !== null) {
      if (prop === 'name') {
        return vm[0].name;
      } else if (prop === 'os') {
        return vm[0].os;
      }
    }
  }

  getAppsList(baseUrl: string) {
    this.appsService.getAppsList(baseUrl).subscribe(data => {
      this.appsList = data;
    });
  }

  getAppName(id: string) {
    const app = this.appsList.filter(data =>  id === data.id);
    if (id !== null) {
      return app[0].name;
    }
  }

  getUpdatedVmList(baseUrl: string) {
    this.vmList = [];
    this.vmService.getVmList(baseUrl)
      .subscribe(data => {
        for (let sel of this.pageVmList) {
          for (let vm of data) {
            if (sel === vm.id) {
              this.vmList.push(vm);
              break;
            }
          }
        }
      });
  }

  removeVm(id: string, index: number): void {
    this.vmList = this.vmList.filter(data => {
      return data.id !== id;
    });
    // console.log(this.vmList);

    this.pageVmList.splice(index, 1);
  }

  activateModal(): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '750px',
      data: {
        selectedVms: this.pageVmList
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((data) => {
      this.selVmsList = data;
      if (this.pageVmList.length > 0) {
        this.pageVmList = [];
      }
      this.pageVmList = this.selVmsList;
      this.getVirtueVmList(this.pageVmList);
    });

    // dialogRef.afterClosed().subscribe(() => {
    //   vms.unsubscribe();
    // });
  }

  updateThisVirtue(id: string, virtueName: string, virtueVersion: string) {
    let body = {
      'name': virtueName,
      'version': virtueVersion,
      'enabled': this.virtueEnabled,
      'virtualMachineTemplateIds': this.pageVmList
    };

    this.virtuesService.updateVirtue(this.baseUrl, id, JSON.stringify(body)).subscribe(
      data => {
        // console.log('Updating ' + data.name + '(' + data.id + ')');
        return true;
      },
      error => {
        console.log(error);
      });
    this.resetRouter();
    this.router.navigate(['/virtues']);
  }

  virtueStatus(id: string, isEnabled: boolean): void {
    if (isEnabled) {
      this.virtueEnabled = false;
    } else {
      this.virtueEnabled = true;
    }
  }

  // deleteVirtue(id): void {
  //   let dialogRef = this.dialog.open(DialogsComponent, {
  //     width: '450px'
  //   });
  //
  //   dialogRef.updatePosition({ top: '15%', left: '36%' });
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     // console.log('This dialog was closed');
  //   });
  // }
}
