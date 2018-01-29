import { Component, OnInit, Input } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { SaviorUser } from '../savior-user';
import { JsondataService } from '../../shared/jsondata.service';

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
  filteredUsers: Observable<any[]>;
  activeDirUsers=[];

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
    // this.route.params.subscribe(userId => this.saviorUserId = userId.id));
    console.log('Savior User: ' + this.saviorUserId);
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

  filterUsers(username: string) {
    return this.activeDirUsers.filter(adUser =>
      adUser.username.toLowerCase().indexOf(username.toLowerCase()) === 0);
  }

  ngOnInit() {}

}
