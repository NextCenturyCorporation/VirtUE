import { Component, OnInit } from '@angular/core';
import { HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import { FormControl} from '@angular/forms';
import { MatDialog} from '@angular/material';
import { ActivatedRoute, Router} from '@angular/router';
import { Observable} from 'rxjs/Observable';

import { DialogsComponent} from '../../dialogs/dialogs.component';
import { VmModalComponent} from '../vm-modal/vm-modal.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { User} from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { ApplicationsService } from '../../shared/services/applications.service';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-duplicate-virtue',
  templateUrl: './duplicate-virtue.component.html',
  styleUrls: ['./duplicate-virtue.component.css'],
  providers: [ ApplicationsService, BaseUrlService, VirtuesService, VirtualMachineService ]
})
export class DuplicateVirtueComponent implements OnInit {

  virtueId: { id: string };
  virtueForm: FormControl;
  virtueEnabled: boolean;
  activeClass: string;
  baseUrl: string;
  users: User[];
  virtues: Virtue[];

  virtueData = [];
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
    this.refreshData();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req);
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  refreshData() {
    setTimeout(() => {
      this.router.navigated = false;
      this.getThisVirtue(this.baseUrl, this.virtueId.id);
    }, 1000);
  }

  getThisVirtue(baseUrl: string, id: string) {
    this.virtuesService.getVirtue(baseUrl, id).subscribe(data => {
      this.virtueData = data;
      this.pageVmList = data.virtualMachineTemplateIds;
      this.virtueEnabled = data.enabled;
      this.getVirtueVmList(data.virtualMachineTemplateIds);
      this.virtueData['name'] = 'Copy of ' + this.virtueData['name'];
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

  duplicateThisVirtue(id: string, virtueName: string, virtueVersion: string) {
    let body = {
      'name': virtueName,
      'version': virtueVersion,
      'enabled': this.virtueEnabled,
      'lastEditor': 'admin',
      'virtualMachineTemplateIds': this.pageVmList
    };

    this.virtuesService.createVirtue(this.baseUrl, JSON.stringify(body)).subscribe(
      data => {
        // console.log('Updating ' + data.name + '(' + data.id + ')');
        return true;
      },
      error => {
        console.log(error);
      });
    this.router.navigate(['/virtues']);
  }

  virtueStatus(id: string, isEnabled: boolean): void {
    if (isEnabled) {
      this.virtueEnabled = false;
    } else {
      this.virtueEnabled = true;
    }
    let body = {
      'enabled': this.virtueEnabled,
    };
    // console.log('Virtue is enabled: ' + this.virtueEnabled);
    this.virtuesService.toggleVirtueStatus(this.baseUrl, id);
    this.refreshData();
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

}
