import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { JsondataService } from '../../shared/jsondata.service';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

export class AdUsers {
  constructor(public name: string, public username: string) { }
}

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css']
})

export class AddUserComponent implements OnInit {

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
    private jsondataService: JsondataService
  ) {
    this.adUserCtrl = new FormControl();
    this.filteredUsers = this.adUserCtrl.valueChanges
      .pipe(
        startWith(''),
        map(adUser => adUser ? this.filterUsers(adUser) : this.activeDirUsers.slice() )
        // map(adUser => adUser ? this.filterStates(adUser) : this.AdUsers.slice())
    );
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
  // Gets AD user for autocomplete field
  getJSON(src): void {
    this.jsondataService.getJSON('adUsers').subscribe(adUsers => this.activeDirUsers = adUsers);
  }
  // filters the AD list as you type
  filterUsers(username: string) {
    return this.activeDirUsers.filter(adUser =>
      adUser.username.toLowerCase().indexOf(username.toLowerCase()) === 0);
  }

  ngOnInit() {
  }

}
