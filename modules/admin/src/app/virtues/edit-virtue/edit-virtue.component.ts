import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';

@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css']
})

export class EditVirtueComponent implements OnInit {

  constructor(public dialog: MatDialog) {}

  activateModal(id): void {
    let dialogRef = this.dialog.open(VmModalComponent, {
        width: '960px'
      });
    
    dialogRef.updatePosition({ top: '5%', left: '20%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('This modal was closed');
    });
  }
  deleteVirtue(id): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
        width: '450px'
      });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('This dialog was closed');
    });
  }
  ngOnInit() {
  }

}
