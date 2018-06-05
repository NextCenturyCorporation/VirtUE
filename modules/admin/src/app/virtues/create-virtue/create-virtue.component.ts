import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { VmModalComponent } from '../vm-modal/vm-modal.component';

import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
// import { VirtualMachine } from '../../shared/models/vm.model';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class CreateVirtueComponent implements OnInit {
  // vms: VirtualMachine;
  virtueForm: FormControl;
  activeClass: string;
  baseUrl: string;
  users: User[];
  virtues: Virtue[];

  vmList = [];
  appsList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private baseUrlService: BaseUrlService,
    private router: Router,
    private appsService: ApplicationsService,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) {
    this.virtueForm = new FormControl();
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getAppsList(awsServer);
    });

    if (this.pageVmList.length > 0) {
      this.getVmList();
    }
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
    console.log('URL: ' + url);
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  getVmList() {
    const selectedVm = this.pageVmList;
    this.vmService.getVmList(this.baseUrl)
      .subscribe(data => {
        if (this.vmList.length < 1) {
          for (let sel of selectedVm) {
            for (let vm of data) {
              if (sel === vm.id) {
                this.vmList.push(vm);
                break;
              }
            }
          }
        } else {
          this.getUpdatedVmList(this.baseUrl);
        }
      });
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
              this.vmList.push(vm.id);
              break;
            }
          }
        }
      });
  }

  createVirtue(virtueName: string) {
    console.log(virtueName);
    let virtueVms = [];
    for (let vm of this.pageVmList) {
      virtueVms.push(vm);
    }
    console.log(virtueVms);
    let body = {
      'name': virtueName,
      'version': '1.0',
      'enabled': true,
      'virtualMachineTemplateIds': virtueVms
    };
    // console.log('New Virtue: ');
    console.log(body);

    this.virtuesService.createVirtue(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        return data;
      },
      error => {
        console.log(error.message);
      });

    this.resetRouter();
    this.router.navigate(['/virtues']);
  }


  removeVm(id: string, index: number): void {
    this.vmList = this.vmList.filter(data => {
      return data.id !== id;
    });
    this.pageVmList.splice(index, 1);
  }

  activateModal() {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '800px',
      data: {
        selectedVms: this.pageVmList
      }
    });
    // console.log('VMs sent to dialog: ' + this.pageVmList);
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const vms = dialogRef.componentInstance.addVms.subscribe((data) => {
      this.selVmsList = data;
      // console.log('VMs from dialog: ' + this.selVmsList);
      if (this.pageVmList.length > 0) {
        this.pageVmList = [];
      }
      this.pageVmList = this.selVmsList;

      this.getVmList();
    });

    dialogRef.afterClosed().subscribe(() => {
      vms.unsubscribe();
    });
  }

}
