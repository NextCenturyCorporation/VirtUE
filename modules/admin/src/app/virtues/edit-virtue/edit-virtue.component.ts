import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Virtue } from '../../shared/models/virtue.model';
import { Application } from '../../shared/models/application.model';
import { VirtuesService } from '../../shared/services/virtues.service';
import { VirtualMachineService } from '../../shared/services/vm.service';

import { MatDialog, MatDialogRef } from '@angular/material';
import { VmModalComponent } from '../vm-modal/vm-modal.component';
import { DialogsComponent } from '../../dialogs/dialogs.component';


@Component({
  selector: 'app-edit-virtue',
  templateUrl: './edit-virtue.component.html',
  styleUrls: ['./edit-virtue.component.css'],
  providers: [ VirtuesService, VirtualMachineService ]
})

export class EditVirtueComponent implements OnInit {

  @Input() virtue: Virtue;
  @Input() appVm: Application;
  virtueData = [];
  virtueVmList = [];
  virtueId : { id: string };

  constructor(
    private route: ActivatedRoute,
    private virtuesService: VirtuesService,
    private vmService: VirtualMachineService,
    private location: Location,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
    this.virtueId = {
      id: this.route.snapshot.params['id']
    };
    this.getThisVirtue();
  }

  getThisVirtue() {
    const id = this.virtueId.id;
    this.virtuesService.getVirtue(id).subscribe(
      data => { this.virtueData = data }
    );
    this.virtueVmList = this.virtueData['vmTemplates'];

    console.log(this.virtueVmList);
  }


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
}
