import { Component, EventEmitter, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { User } from '../../shared/models/user.model';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css'],
  providers: [ BaseUrlService, UsersService ]
})
export class UserListComponent implements OnInit {
  @Input() user: User;

  saviorUsers: string;
  users = [];
  virtues = [];

  constructor(
    private route: ActivatedRoute,
    private location: Location,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.baseUrlService.getBaseUrl().subscribe( _url => {
      let awsServer = _url[0].aws_server;
      this.getUsers(awsServer);
      this.getVirtues(awsServer);
    });

  }

  getUsers( baseUrl: string ): void {
    this.usersService.getUsers(baseUrl).subscribe(data => {
      this.users = data;
    });
  }

  getVirtues(baseUrl: string) {
    this.virtuesService.getVirtues(baseUrl).subscribe( virtues => {
      this.virtues = virtues;
    });
  }

  getVirtueName(id: string) {
    for (let virtue of this.virtues) {
      if (id === virtue.id) {
        return virtue.name;
      }
    }
  }

  openDialog(id, type, text): void {

    const dialogRef = this.dialog.open( DialogsComponent, {
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
