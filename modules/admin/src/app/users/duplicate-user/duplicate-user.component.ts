import { Component, Input, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { User } from '../../shared/models/user.model';
import { ApplicationsService } from '../../shared/services/applications.service';
import { BaseUrlService } from '../../shared/services/baseUrl.service';
import { UsersService } from '../../shared/services/users.service';
import { VirtuesService } from '../../shared/services/virtues.service';

import { MatDialog } from '@angular/material';
import { VirtueModalComponent } from '../virtue-modal/virtue-modal.component';

@Component({
  selector: 'app-duplicate-user',
  templateUrl: './duplicate-user.component.html',
  providers: [ ApplicationsService, BaseUrlService, UsersService, VirtuesService ]
})
export class DuplicateUserComponent implements OnInit {
  @Input() user: User;

  baseUrl: string;
  userToEdit: { id: string };
  submitBtn: any;
  fullImagePath: string;
  userRoles = [];
  userData = [];
  storedVirtues = [];
  selVirtues = [];
  virtues = [];
  appsList = [];
  adUserCtrl: FormControl;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private appsService: ApplicationsService,
    private baseUrlService: BaseUrlService,
    private usersService: UsersService,
    private virtuesService: VirtuesService,
    public dialog: MatDialog
  ) {}

  ngOnInit() {
  }

}
