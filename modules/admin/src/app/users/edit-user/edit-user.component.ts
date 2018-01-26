import { Component, OnInit, Input } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { SaviorUser } from '../savior-user';
import { JsondataService } from '../../data/jsondata.service';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { MatDialog, MatDialogRef } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.css']
})

export class EditUserComponent implements OnInit {
  @Input() appUser : SaviorUser;

  // appUser: string;
  saviorUserId: number;
  screenWidth: any;
  leftPosition: any;
  submitBtn: any;
  dialogWidth: any;
  fullImagePath: string;

  adUserCtrl: FormControl;
  filteredUsers: Observables<any[]>;
  activeDirUsers: AdUsers [] = [
    { name:'Anthony Wong', username:'awong' },
    { name:'Binoy Ravindran', username:'bravindran' },
    { name:'Chris Long', username:'clong' },
    { name:'Kara Cartwright', username:'kcartwright' },
    { name:'Kyle Drumm', username:'kdrumm' },
    { name:'Mike Day', username:'mday' },
    { name:'Patrick Dwyer', username:'pdwyer' },
    { name:'Pierre Olivier', username:'polivier' },
    { name:'Ruslan Nikolaev', username:'rnikolaev' },
    { name:'Sophie Kim', username:'skim' },
    { name:'Wole Omitowoju', username:'womitowoju' },
  ];

  constructor(
    public dialog: MatDialog,
    private jsondataService: JsondataService,
    private route: ActivatedRoute
  ) {
    this.adUserCtrl = new FormControl();
    this.filteredUsers = this.adUserCtrl.valueChanges
      .pipe(
        startWith(''),
        map(adUser => adUser ? this.filterUsers(adUser) : this.activeDirUsers.slice() )
        // map(adUser => adUser ? this.filterStates(adUser) : this.AdUsers.slice())
    );
    this.route.params.subscribe(userId => this.saviorUserId = userId.id));
    console.log('Savior User: '+this.saviorUserId);
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

  filterUsers(username: string) {
    return this.activeDirUsers.filter(adUser =>
      adUser.username.toLowerCase().indexOf(username.toLowerCase()) === 0);
  }

  getUser(): void {
    const id = this.saviorUserId;
    this.jsondataService.getDataById(id, 'appUsers')
      .subscribe(saviorUser => this.appUser = saviorUser).match(this.appUser.app_user_id===id);
  }

  ngOnInit() {
    this.getUser();
  }

}
