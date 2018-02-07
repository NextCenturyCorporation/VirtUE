import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { UserModel } from '../../shared/models/user.model';
import { UsersService } from '../../shared/services/users.service';
// import { JsonFilterPipe } from '../../shared/json-filter.pipe';
// import { CountFilterPipe } from '../../shared/count-filter.pipe';

import { Observable } from 'rxjs/Observable';


@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
  providers: [ UsersService ]
})
export class UserListComponent implements OnInit {

  @Output() selectedUser = new EventEmitter<UserModel>();

  saviorUsers: string;
  appUserList = [];

  // constructor( private dataService: DataService ){}
  constructor(
    private usersService: UsersService,
    public dialog: MatDialog
  ) {}

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

  getUsers(): void {
    this.usersService.listUsers().subscribe(appUsers => this.appUserList = appUsers);
    // this.appUserList = this.usersService.listUsers();
  }

  onSelected(id) {
    this.usersService.selectedUser.emit(id);
    console.log(id);
  }

  ngOnInit(){
    this.getUsers();
  }

}
