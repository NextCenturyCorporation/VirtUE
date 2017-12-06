import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css']
})
export class CreateVirtueComponent implements OnInit {

  constructor(public dialog: MatDialog) {}

  activateModal(): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '960px'
    });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('This modal was closed');
    });
  }

  ngOnInit() {
  }

}
