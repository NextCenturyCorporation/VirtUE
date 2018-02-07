import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { VirtueModel } from '../../shared/models/virtue.model';
import { VirtuesService } from '../../shared/services/virtues.service';
import { JsonFilterPipe } from '../../shared/json-filter.pipe';
import { CountFilterPipe } from '../../shared/count-filter.pipe';

@Component({
  selector: 'app-virtue-list',
  providers: [ VirtuesService ],
  templateUrl: './virtue-list.component.html',
  styleUrls: ['./virtue-list.component.css']
})
export class VirtueListComponent implements OnInit {
  title = 'Virtues';
  virtues = [];
  virtueTotal : number;
  os: Observable<Array<VirtuesService>>;

  constructor(
    private virtuesService: VirtuesService,
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
    this.virtuesService.getVirtues()
      .subscribe(
        virtueList => {this.virtues = virtueList} 
      );
  }


}
