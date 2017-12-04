import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../dialogs/dialogs.component';

@Component({
  selector: 'app-virtues',
  templateUrl: './virtues.component.html',
  styleUrls: ['./virtues.component.css']
})
export class VirtuesComponent implements OnInit {

  constructor(public dialog: MatDialog) {}

  openDialog(id,dialogType): void {

    let dialogRef = this.dialog.open(DialogsComponent, {
      width: '450px'
    });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    if (dialogType == 'delete') {
      console.log('user wants to delete virtue #' + id);
    } else if (dialogType == 'disable'){
      console.log('user wants to disable virtue #' + id);
    }

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  ngOnInit() {
  }

}
