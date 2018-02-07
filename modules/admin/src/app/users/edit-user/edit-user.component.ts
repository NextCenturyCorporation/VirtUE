import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpParams } from '@angular/common/http';

import { UserModel } from '../../shared/models/user.model';
import { UsersService } from '../../shared/services/users.service';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css'],
  providers: [ UsersService ]
})

export class EditUserComponent implements OnInit {
  @Input() appUserById : UserModel;

  appUser: string;
  saviorUserId: {id:number};
  screenWidth: any;
  leftPosition: any;
  submitBtn: any;
  dialogWidth: any;
  fullImagePath: string;

  adUserCtrl: FormControl;
  filteredUsers: Observable<any[]>;
  activeDirUsers=[];

  constructor(
    public dialog: MatDialog,
    private usersService: UsersService,
    private router: ActivatedRoute,
  ) {
    this.adUserCtrl = new FormControl();
    this.usersService.getAdUsers().subscribe(adUsers => this.activeDirUsers = adUsers);
    this.filteredUsers = this.adUserCtrl.valueChanges
      .pipe(
        startWith(''),
        map(adUser => adUser ? this.filterUsers(adUser) : this.activeDirUsers.slice() )
    );
    console.log(this.filteredUsers);
  }

  ngOnInit() {
    this.saviorUserId = {
      id: this.router.snapshot.params['id']
    };
    // this.getAdUsers();
    this.getAppUser(this.saviorUserId.id);
  }

  activateModal(id,mode): void {

    this.dialogWidth = 600;
    this.fullImagePath = './assets/images/app-icon-white.png';

    if (mode=='add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    let dialogRef = this.dialog.open( VirtueModalComponent, {
      width: this.dialogWidth+'px',
      data: {
        id: id,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath
      },
      panelClass: 'virtue-modal-overlay'
    });

    this.screenWidth = (window.screen.width);
    this.leftPosition = ((window.screen.width)-this.dialogWidth)/2;
    // console.log(this.screenWidth);
    // console.log(this.leftPosition);

    dialogRef.updatePosition({ top: '5%', left: this.leftPosition+'px' });
    // dialogRef.afterClosed().subscribe();
  }

  getAppUser(appUserId) {
    console.log(appUserId);
    // this.usersService.getUser(id).subscribe(appUser => this.appUser = appUser);
    this.usersService.getUser(appUserId).subscribe(appUser => this.appUser = appUser);
  }

  getAdUsers(): void {
    // Gets AD user for autocomplete field
    this.usersService.getAdUsers().subscribe(adUsers => this.activeDirUsers = adUsers);
  }

  filterUsers(username: string) {
    return this.activeDirUsers.filter(adUser =>
      adUser.username.toLowerCase().indexOf(username.toLowerCase()) === 0);
  }

}
