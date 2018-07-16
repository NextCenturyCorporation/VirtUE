import { Component, Input, OnInit} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';

import { VmAppsModalComponent } from '../vm-apps-modal/vm-apps-modal.component';

import { VirtualMachine } from '../../shared/models/vm.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

@Component({
  selector: 'app-vm-duplicate',
  templateUrl: './vm-duplicate.component.html',
  providers: [ ApplicationsService, BaseUrlService, VirtualMachineService ]
})
export class VmDuplicateComponent implements OnInit {
  @Input() vm: VirtualMachine;

  vmForm: FormControl;
  vmId: { id: string };
  baseUrl: string;
  os: string;
  osValue: string;
  selectedOS: string;
  securityTag: string;
  securityLevel: string;

  vmData = [];
  appList = [];
  selectedApps = [];
  selAppList = [];
  pageAppList = [];
  osList = [
    { 'name': 'Debian', 'os': 'LINUX' },
    { 'name': 'Windows', 'os': 'WINDOWS' }
  ];
  securityOptions = [
    { 'level': 'default', 'name': 'Default' },
    { 'level': 'email', 'name': 'Email' },
    { 'level': 'power', 'name': 'Power User' },
    { 'level': 'admin', 'name': 'Administrator' }
  ];

  constructor(
    private activatedRoute: ActivatedRoute,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private router: Router,
    private vmService: VirtualMachineService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.vmId = {
      id: this.activatedRoute.snapshot.params['id']
    };
    this.baseUrlService.getBaseUrl().subscribe(res => {
      let awsServer = res[0].aws_server;
      this.getBaseUrl(awsServer);
      this.getThisVm(awsServer, this.vmId.id);
    });
  }

  getBaseUrl(url: string) {
    this.baseUrl = url;
  }

  getThisVm(baseUrl: string, id: string) {
    this.baseUrl = baseUrl;
    this.vmService.getVM(baseUrl, id).subscribe(
      data => {
        this.vmData = data;
        this.vmData['name'] = 'Copy of ' + this.vmData['name'];
        this.selectedOS = data.os;
        this.pageAppList = data.applicationIds;
        // this.getAppList(data.applicationIds);
        this.getAppList();
        this.securityLevel = data.securityTag;
      }
    );
  }

  getAppList() {
    const selectedApps = this.pageAppList;
    this.appsService.getAppsList(this.baseUrl)
      .subscribe(apps => {
        if (selectedApps.length < 1) {
          for (let sel of selectedApps) {
            for (let app of apps) {
              if (sel === app.id) {
                this.appList.push(app);
                break;
              }
            }
          }
        } else {
          this.getUpdatedAppList(this.baseUrl);
        }
      });
  }

  getUpdatedAppList(baseUrl: string) {
    this.appList = [];
    this.appsService.getAppsList(baseUrl)
      .subscribe(apps => {
        for (let sel of this.pageAppList) {
          for (let app of apps) {
            if (sel === app.id) {
              this.appList.push(app);
              break;
            }
          }
        }
      });
  }

  removeApp(id: string, index: number): void {
    this.appList = this.appList.filter(data => {
      return data.id !== id;
    });
    this.pageAppList.splice(index, 1);
  }

  activateModal(): void {

    let dialogRef = this.dialog.open(VmAppsModalComponent, {
      width: '750px',
      data: {
        selectedApps: this.pageAppList
      }
    });
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    const apps = dialogRef.componentInstance.addApps.subscribe((data) => {
      this.selAppList = data;
      if (this.pageAppList.length > 0) {
        this.pageAppList = [];
      }
      this.pageAppList = this.selAppList;

      this.getAppList();
      // this.getAppList(this.pageAppList);
    });

    dialogRef.afterClosed().subscribe(() => {
      apps.unsubscribe();
    });
  }

  duplicateVirtualMachine(vmName: string, vmOs: string, vmSecurityTag: string) {
    let body = {
      'name': vmName,
      'os': vmOs,
      'loginUser': 'system',
      'enabled': true,
      'applicationIds': this.pageAppList,
      'securityTag': vmSecurityTag
    };
    this.vmService.createVM(this.baseUrl, JSON.stringify(body));
    this.router.navigate(['/virtual-machines']);
  }

  cancel() {
    this.router.navigate(['/virtual-machines']);
  }

}
