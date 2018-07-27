import { HttpEvent, HttpHandler, HttpRequest } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Observable } from 'rxjs/Observable';

import { User } from '../../shared/models/user.model';
import { Virtue } from '../../shared/models/virtue.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  providers: [ ApplicationsService, BaseUrlService, UsersService, VirtuesService ]
})

export class UserComponent implements OnInit {

  baseUrl: string;
  submitBtn: any;
  fullImagePath: string;

  user: User;
  userData: string;

  mode: string;
  actionName: string;

  parentDomain: string;

  allVirtues = [];
  allApps = [];
  adUserCtrl: FormControl;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {
    this.setMode();
    this.user = new User('');

    console.log(this.user);

     //maybe? originally this was only called in addUser's constructor, but in Virtues it was called in create, edit, and duplicate.
     //I don't know what it does, but it wouldn't persist once you leave the creation screen anyway. So let's make it every time.
    this.adUserCtrl = new FormControl();

    this.parentDomain = "/users";
    // override the route reuse strategy
    this.router.routeReuseStrategy.shouldReuseRoute = function() {
      return false;
    };
  }

  ngOnInit() {
    if (this.mode === "e" || this.mode === "d") {//if "d" or "e"
      this.user.username = this.activatedRoute.snapshot.params['id'];
    }

    this.baseUrlService.getBaseUrl().subscribe(data => {
      let url = data[0].aws_server;
      this.setBaseUrl(url);
      this.getAllVirtues();
      this.getAllApps();
    });

    if (this.mode === "e" || this.mode === "d") {//if "d" or "e"
      this.refreshData();
    }
  }

  //I'm not sure this gets used anywhere.
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.log(req);
    return next.handle(req);
  }

  //This checks the current routing info (the end of the current url)
  //and uses that to set what mode (create/edit/duplicate) the page
  // ought to be in.
  // Create new user: 'c', Edit user: 'e', Duplicate user: 'd'
  setMode() {
    // console.log(this.router.routerState.snapshot.url);
    let url = this.router.routerState.snapshot.url;
    if (url[0] === '/') {
      url = url.substr(1);
    }
    this.mode = 'c';

    //Parse url, making sure it's set up the expected way.
    let urlValid = true;

    let route = url.split('/');
    if (route[0] !== 'users') {
      //something about the routing system has changed.
      urlValid = false;
    }
    if (route[1] === 'create') {
        this.mode = 'c';
        this.actionName = "Create";
    } else if (route[1] === 'edit') {
        this.mode = 'e';
        this.actionName = "Edit";
    } else if (route[1] === 'duplicate') {
        this.mode = 'd';
        this.actionName = "Duplicate";
    } else {
        //something about the routing system has changed.
        urlValid = false;
    }
    if (!urlValid) {
      if (this.router.routerState.snapshot.url === "/users") {
        // apparently any time an error happens on this page, the system
        // quits and returns to /virtues, and then for some reason re-calls the
        // constructor for CreateEditVirtueComponent. Which leads here and then
        // breaks because the URL is wrong. Strange.
        return false;
      }
      console.log("ERROR: Can't decipher URL; Something about \
the routing system has changed. Returning to virtues page.\n       Expects something like \
/users/create, /users/duplicate/username, or /users/edit/username,\
 but got: \n       " + this.router.routerState.snapshot.url);
      this.router.navigate(['/users']);
      return false;
    }
    return true;
  }

  resetRouter() {
    setTimeout(() => {
      this.router.navigated = false;
    }, 1000);
  }

  refreshData() {
    setTimeout(() => {
      this.getUserData(this.user.username);
    }, 400);
  }

  setBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserData(username: string) {
    this.usersService.getUser(this.baseUrl, username).subscribe(uData => {
      this.userData = uData;
      this.user.roles = uData.authorities;
      this.user.enabled = uData.enabled;
      this.updateVirtueList(uData.virtueTemplateIds);
    });
  }

  selectedRole(role: string) {
    if (this.user.roles.length > 0) {
      for (let sel of this.user.roles) {
        if (sel === role) {
          return true;
        }
      }
    } else {
      return false;
    }
  }

  getAllVirtues() {
    if (this.baseUrl !== null) {
      this.virtuesService.getVirtues(this.baseUrl).subscribe(virts => {
        this.allVirtues = virts;
      });
    }
  }

  getAllApps() {
    if (this.baseUrl !== null) {
      this.appsService.getAppsList(this.baseUrl).subscribe(apps => {
        this.allApps = apps;
      });
    }
  }

  getVirtueName(id: string) {
    let userVirtue = [];
    if (id !== null) {
      userVirtue = this.allVirtues.filter(virt => virt.id === id)
        .map(virtue => virtue.name);
      return userVirtue;
    }
  }

  generateAppsListHTML(virtue: any) {
    let appsString = virtue.applicationIds.toString();
    let appList: any;
    let appInfo: any;
    let appNames: string = '';
    let i: number = 0;
    appList = appsString.split(',');
    for (let id of appList) {
      // console.log("here")
      i++;
      appInfo = this.allApps.filter(data => data.id === id)
        .map(app => app.name);
      appNames = appNames + `<li>${appInfo.toString()}</li>`;
    }
    return appNames;
  }

  updateVirtueList(newVirtueIDs: any) {
    this.user.virtueIDs = newVirtueIDs;
    this.user.virtues = new Array<Virtue>();
      for (let vID of newVirtueIDs) {
        for (let virtue of this.allVirtues) {
          if (vID === virtue.id) {
            this.user.virtues.push(virtue);
            break;
          }
        }
      }
  }

  removeVirtue(id: string, index: number): void {
    this.user.virtues = this.user.virtues.filter(virt => {
      return virt.id !== id;
    });
    this.user.virtueIDs.splice(index, 1);
  }

  activateModal(mode: string): void {
    let dialogHeight = 600;
    let dialogWidth = 800;
    // let fullImagePath = './assets/images/app-icon-white.png';

    if (mode === 'add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    let dialogRef = this.dialog.open( VirtueModalComponent, {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: this.user.username,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath,
        userVirtueIDs: this.user.virtueIDs
      },
      panelClass: 'virtue-modal-overlay'
    });

    let virtueList = dialogRef.componentInstance.addVirtues.subscribe((selectedVirtues) => {
      this.updateVirtueList(selectedVirtues);
    });
    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }

  updateThisUser(roleUser: boolean, roleAdmin: boolean) {
    let roles = [];
    if (roleUser) {
      roles.push('ROLE_USER');
    }
    if (roleAdmin) {
      roles.push('ROLE_ADMIN');
    }

    if (!this.user.username) {
      return confirm("You need to enter a username.");
    }

    let body = {
      'username': this.user.username,
      'authorities': roles,
      'virtueTemplateIds': this.user.virtueIDs,
      'enabled': this.user.enabled
    };
    // console.log(body);
    this.usersService.updateUser(this.baseUrl, this.user.username, JSON.stringify(body)).subscribe(
      error => {
        console.log(error);
      });
    this.resetRouter();
    this.router.navigate(['/users']);
  }

  toggleUserStatus() {
    this.user.enabled = !this.user.enabled;
  }
}
