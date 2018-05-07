import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { HttpClient, HttpEvent, HttpHeaders, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';

import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { Application } from '../../shared/models/application.model';
import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { VirtualMachine } from '../../shared/models/vm.model';

import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';


@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css'],
  providers: [ BaseUrlService, VirtuesService, VirtualMachineService ]
})

export class EditVirtueComponent implements OnInit {
  virtueId: { id: string };
  virtueForm: FormControl;
  virtueEnabled: boolean;
  virtualMachine: VirtualMachine;
  activeClass: string;
  baseUrl: string;
  users: User[];
  virtues: Virtue[];

  virtueData = [];
  vmInfo = [];
  vmList = [];
  appList = [];
  selVmsList = [];
  pageVmList = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
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

    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getThisVirtue(awsServer, this.virtueId.id);
      this.getBaseUrl(awsServer);
      if (this.pageVmList.length > 0) {
        this.getVirtueVmList(this.pageVmList);
      }
    });
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 500);
  }

  getThisVirtue(baseUrl: string, id: string) {
    this.virtuesService.getVirtue(baseUrl, id).subscribe(data => {
      this.virtueData = data;
      this.pageVmList = data.virtualMachineTemplateIds;
      this.virtueEnabled = data.enabled;
      this.getVirtueVmList(data.virtualMachineTemplateIds);
    });
  }

  getAllVms() {
    this.vmService.getVmList(this.baseUrl).subscribe(vms => {
      this.vmList = vms;
    });
  }

  getVirtueVmList(virtueVms: any[]) {
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
    // this.vmService.getVmList(this.baseUrl)
    // .subscribe(data => {
    //   if (this.vmList.length < 1) {
    //     for (let vm of data) {
    //       for (let sel of selectedVm) {
    //         if (sel === vm.id) {
    //           this.vmList.push(vm);
    //           break;
    //         }
    //       }
    //     }
    //   } else {
    //     this.getUpdatedVmList(this.baseUrl);
    //   }
    // });
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

  getAppList() {
    let vms = this.vmList;
    let apps = [];
    for (let vm of vms) {
      apps = vm.applications;
      // for (let app of apps) {
        // this.appList.push({
        //
        // });
      // }
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
    this.pageVmList.splice(index, 1);
  }

  // activateModal(id): void {
  //   let virtueId = id;
  //   let dialogRef = this.dialog.open(VmModalComponent, {
  //     width: '960px'
  //   });
  //
  //   dialogRef.updatePosition({ top: '5%', left: '20%' });
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     // console.log('This modal was closed');
  //   });
  // }
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
    console.log('updating virtue #' + id);
    console.log(body);
    this.virtuesService.updateVirtue(this.baseUrl, id, JSON.stringify(body));
    this.resetRouter();
    // this.router.navigate(['/virtues']);
  }

  virtueStatus(isEnabled: boolean): void {
    if (isEnabled) {
      this.virtueEnabled = false;
    } else {
      this.virtueEnabled = true;
    }
    console.log(this.virtueEnabled);
  }

  deleteVirtue(id): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px'
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      // console.log('This dialog was closed');
    });
  }
}
