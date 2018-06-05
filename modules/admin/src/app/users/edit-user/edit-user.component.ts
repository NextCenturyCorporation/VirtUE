import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { User } from '../../shared/models/user.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { Observable } from 'rxjs/Observable';
import { startWith } from 'rxjs/operators/startWith';
import { map } from 'rxjs/operators/map';

import { MatDialog } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  providers: [ BaseUrlService, UsersService, VirtuesService ]
})

export class EditUserComponent implements OnInit {
  @Input() user: User;

  baseUrl: string;
  userToEdit: {id: string};
  submitBtn: any;
  fullImagePath: string;
  userData = [];
  adUserCtrl: FormControl;

  constructor(
    private router: ActivatedRoute,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    // private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.userToEdit = {
      id: this.router.snapshot.params['id']
    };

    this.baseUrlService.getBaseUrl().subscribe(data => {
      let url = data[0].aws_server;
      this.getBaseUrl(url);
      this.getUserToEdit(url, this.userToEdit.id);
    });

  }

  getBaseUrl( url: string ) {
    this.baseUrl = url;
  }

  getUserToEdit(baseUrl: string, username: string) {
    this.usersService.getUser(baseUrl, username).subscribe(data => {
      this.userData = data;
    });
  }

  // displayUser(userData: any) {
  //   this.userData = userData[0];
  // }

  activateModal(id, mode): void {
    let dialogHeight = 600;
    let dialogWidth = 800;
    // let fullImagePath = './assets/images/app-icon-white.png';

    if (mode === 'add') {
      this.submitBtn = 'Add Virtues';
    } else {
      this.submitBtn = 'Update List';
    }

    const dialogRef = this.dialog.open( VirtueModalComponent, {
      height: dialogHeight + 'px',
      width: dialogWidth + 'px',
      data: {
        id: id,
        dialogMode: mode,
        dialogButton: this.submitBtn,
        appIcon: this.fullImagePath
      },
      panelClass: 'virtue-modal-overlay'
    });

    let leftPosition = ((window.screen.width) - dialogWidth) / 2;

    dialogRef.updatePosition({ top: '5%', left: leftPosition + 'px' });
    // dialogRef.afterClosed().subscribe();
  }
}
