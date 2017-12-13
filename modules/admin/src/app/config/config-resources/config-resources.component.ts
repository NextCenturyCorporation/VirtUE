import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { ResourceModalComponent } from '../resource-modal/resource-modal.component';

@Component({
  selector: 'app-config-resources',
  templateUrl: './config-resources.component.html',
  styleUrls: ['./config-resources.component.css']
})
export class ConfigResourcesComponent implements OnInit {

  screenWidth: any;
  leftPosition: any;
  dialogWidth: any;

  constructor(public dialog: MatDialog) {}

  openDialog(id,type,text): void {

    this.dialogWidth = 450;

    let dialogRef = this.dialog.open(DialogsComponent, {
      width: this.dialogWidth+'px',
      data:  {
          dialogText: text,
          dialogType: type
        },
      hasBackdrop: false
    });

    this.screenWidth = (window.screen.width);
    this.leftPosition = ((window.screen.width)-this.dialogWidth)/2;

    dialogRef.updatePosition({ top: '15%', left: this.leftPosition+'px' });
  }

  activateModal(): void {

    this.dialogWidth = 600;
    this.screenWidth = (window.screen.width);
    this.leftPosition = ((window.screen.width)-this.dialogWidth)/2;

    let dialogRef = this.dialog.open(ResourceModalComponent, {
      width: this.dialogWidth+'px',
    });

    dialogRef.updatePosition({ top: '5%', left: this.leftPosition+'px' });
    
  }

  ngOnInit() {}

}
