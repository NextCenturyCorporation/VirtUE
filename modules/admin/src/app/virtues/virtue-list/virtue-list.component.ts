import { Component, Input, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { DialogsComponent } from '../../dialogs/dialogs.component';

import { ActiveClassDirective } from '../../shared/directives/active-class.directive';
import { Virtue } from '../../shared/models/virtue.model';
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
  virtue: Virtue[];


  title = 'Virtues';
  virtues = [];
  virtueTotal: number;
  os: Observable<Array<VirtuesService>>;

  constructor(
    private route: ActivatedRoute,
    private virtuesService: VirtuesService,
    public dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.virtuesService.getVirtues()
    .subscribe( virtueList => {
      this.virtues = virtueList;
    });
  }

  openDialog(id, type, text): void {

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

  virtueStatus(id: string, virtue: Virtue): void {
    const virtueObj = this.virtues.filter(data => virtue.id === id);
    virtueObj.map((_, i) => {
      virtueObj[i].enabled ? virtueObj[i].enabled = false : virtueObj[i].enabled = true;
      console.log(virtueObj);
    });
    // this.virtuesService.updateVirtue(id, virtueObj);

  }

}
