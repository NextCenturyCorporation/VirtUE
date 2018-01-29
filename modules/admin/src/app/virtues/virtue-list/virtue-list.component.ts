import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { JsondataService } from '../../shared/jsondata.service';
import { JsonFilterPipe } from '../../shared/json-filter.pipe';
import { CountFilterPipe } from '../../shared/count-filter.pipe';

@Component({
  selector: 'app-virtue-list',
  providers: [ JsondataService ],
  templateUrl: './virtue-list.component.html',
  styleUrls: ['./virtue-list.component.css']
})
export class VirtueListComponent implements OnInit {

  virtues = [];
  virtueTotal : number;

  constructor(
    private jsondataService: JsondataService,
    public dialog: MatDialog,
  ) {}

  openDialog(id,type,text): void {

    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px',
      data:  {
          dialogText: text,
          dialogType: type
        }
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  ngOnInit() {
    this.jsondataService.getJSON('virtues')
      .subscribe(resJsonData => this.virtues = resJsonData)
  }

}
