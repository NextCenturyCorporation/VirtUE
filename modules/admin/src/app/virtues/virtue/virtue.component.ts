import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { DataService } from '../../data/data.service';
import { JsonFilterPipe } from '../../data/json-filter.pipe';
import { CountFilterPipe } from '../../data/count-filter.pipe';

@Component({
  selector: 'app-virtue',
  providers: [ DataService ],
  templateUrl: './virtue.component.html',
  styleUrls: ['./virtue.component.css']
})
export class VirtueComponent implements OnInit {

  virtues = [];
  virtueTotal : number;

  constructor(
    private dataService: DataService,
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
    this.dataService.getData('virtues')
      .subscribe(resJsonData => this.virtues = resJsonData)
  }

}
