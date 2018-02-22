import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpParams } from '@angular/common/http';

import { User } from '../../shared/models/user.model';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css'],
  providers: [ UsersService, VirtuesService ]
})

export class EditUserComponent implements OnInit {
  @Input() user: User;

  saviorUserId: {id: string};
  selectedUser: string;
  screenWidth: any;
  leftPosition: any;
  submitBtn: any;
  dialogWidth: any;
  fullImagePath: string;

  adUserCtrl: FormControl;
  filteredUsers: Observable<any[]>;
  activeDirUsers = [];
  appUser = [];
  selectedApps = [];

  constructor(
    private router: ActivatedRoute,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {
    this.usersService.getAdUsers().subscribe(
      adUsers => {
        this.activeDirUsers = adUsers;
      }
    );
    this.adUserCtrl = new FormControl();
    this.filteredUsers = this.adUserCtrl.valueChanges
      .pipe(
        startWith(''),
        map(adUser => adUser ? this.filterUsers(adUser) : this.activeDirUsers.slice() )
    );
  }

  ngOnInit() {
    this.saviorUserId = {
      id: this.router.snapshot.params['id']
    };
    this.getAdUsers();
    this.getThisUser();
  }

  getThisUser() {
    const id = this.saviorUserId.id;
    this.usersService.getUser(id)
    .subscribe( data => {
      for (let user of data) {
        if (user.id === id) {
          this.appUser = user;
          this.selectedUser = user.name;
          this.selectedApps = user.virtues;
          break;
        }
      }
    });
  }

  getAdUsers(): void {
    // Gets AD user for autocomplete field
    this.usersService.getAdUsers()
      .subscribe(adUsers => this.activeDirUsers = adUsers);
  }

  filterUsers(name: string) {
    return this.activeDirUsers.filter(adUser =>
      adUser.name.toLowerCase().indexOf(name.toLowerCase()) === 0);
  }

  activateModal(id, mode): void {

    this.dialogWidth = 600;
    this.fullImagePath = './assets/images/app-icon-white.png';

    if (mode === 'add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    const dialogRef = this.dialog.open( VirtueModalComponent, {
      width: this.dialogWidth + 'px',
      data: {
        id: id,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath
      },
      panelClass: 'virtue-modal-overlay'
    });

    this.screenWidth = (window.screen.width);
    this.leftPosition = ((window.screen.width) - this.dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: this.leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }
}
