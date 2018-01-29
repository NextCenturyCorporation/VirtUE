import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';

import { JsondataService } from '../../shared/jsondata.service';
import { JsonFilterPipe } from '../../shared/json-filter.pipe';
import { CountFilterPipe } from '../../shared/count-filter.pipe';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {

  saviorUsers: string;
  appUserList=[];

  // constructor( private dataService: DataService ){}
  constructor(
    private jsondataService: JsondataService,
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

  getJSON(src): void {
    this.jsondataService.getJSON(src).subscribe(appUsers => this.appUserList = appUsers);
  }

  ngOnInit(){
    this.getJSON('appUsers');
  }

}
