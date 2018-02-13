import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { VirtuesService } from '../../shared/services/virtues.service';

@Component({
  selector: 'app-create-virtue',
  templateUrl: './create-virtue.component.html',
  styleUrls: ['./create-virtue.component.css'],
  providers: [ VirtuesService ]
})
export class CreateVirtueComponent implements OnInit {

  constructor(
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {}

  activateModal(id): void {

    let dialogRef = this.dialog.open(VmModalComponent, {
      width: '960px'
    });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    dialogRef.afterClosed().subscribe(result => {
      console.log('This modal was closed');
    });
  }

  onSave() {

  }

  ngOnInit() {
  }

}
