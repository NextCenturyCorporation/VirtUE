import { Component, EventEmitter, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { User } from '../../shared/models/user.model';
import { UsersService } from '../../shared/services/users.service';

import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
  providers: [ UsersService ]
})
export class UserListComponent implements OnInit {
  @Input() user: User;

  saviorUsers: string;
  appUserList = [];

  constructor(
    private route: ActivatedRoute,
    private usersService: UsersService,
    private location: Location,
    public dialog: MatDialog
  ) {}

  ngOnInit(){
    this.getUsers();
  }

  getUsers(): void {
    this.usersService.getUsers()
      .subscribe(appUsers => this.appUserList = appUsers);
  }

  onSelected(id) {

  }

  openDialog(id,type,text): void {

    let dialogRef = this.dialog.open( DialogsComponent, {
      width: '450px',
      data:  {
          dialogText: text,
          dialogType: type
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog to delete {{data.dialogText}} was closed');
    });
  }

}
