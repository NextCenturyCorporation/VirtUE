import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';
import { VirtueModel } from '../../shared/models/virtue.model';
import { VirtuesService } from '../../shared/services/virtues.service';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css'],
  providers: [ VirtuesService ]
})

export class EditVirtueComponent implements OnInit {


  virtue: { id: string, name: string }[] = [];
  // @Input() virtue: { name: string, vmTemplates: string }
  // @Input() id: number;

  public virtueId : { id: string };

  constructor(
    public dialog: MatDialog,
    private router: ActivatedRoute,
    private virtuesService: VirtuesService
  ) {}

  activateModal(id): void {
    let dialogRef = this.dialog.open(VmModalComponent, {
        width: '960px'
      });

    dialogRef.updatePosition({ top: '5%', left: '20%' });

    dialogRef.afterClosed().subscribe(result => {
      // console.log('This modal was closed');
    });
  }
  deleteVirtue(id): void {
    let dialogRef = this.dialog.open(DialogsComponent, {
        width: '450px'
      });

    dialogRef.updatePosition({ top: '15%', left: '36%' });

    dialogRef.afterClosed().subscribe(result => {
      // console.log('This dialog was closed');
    });
  }
  ngOnInit() {
    this.virtueId = {
      id: this.router.snapshot.params['id']
    };
    // console.log(this.virtueId.id);
    // this.virtue = this.virtuesService.getVirtue(this.virtueId.id);
    // console.log(this.virtue.name);
  }

}
