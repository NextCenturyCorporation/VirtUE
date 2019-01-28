import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { first, map } from 'rxjs/operators';

import { MatDialog } from '@angular/material';

import { AuthenticationService } from '../services/authentication.service';
import { RouterService } from '../services/router.service';
import { GenericPageComponent } from '../abstracts/gen-page/gen-page.component';

@Component({templateUrl: 'login.component.html'})
export class LoginComponent extends GenericPageComponent implements OnInit {
  loginForm: FormGroup;
  submitted = false;
  returnUrl: string;
  returnedData: any = {headers: ""};
  message: string = "";
  loading: boolean = false;

  constructor(
    routerService: RouterService,
    matDialog: MatDialog,
    private activatedRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private authenticationService: AuthenticationService) {
      super(routerService, matDialog);
  }

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // get return url from route parameters or default to '/'
    this.returnUrl = this.activatedRoute.snapshot.queryParams['returnUrl'] || '/';
  }

  onSubmit() {
    this.loading = true;
    this.submitted = true;
    this.message = "";

    // stop here if form is invalid
    if (this.loginForm.invalid) {
      this.loading = false;
      return;
    }

    this.authenticationService.login(this.loginForm.controls.username.value, this.loginForm.controls.password.value)
      .subscribe(
        (data: HttpResponse<string>) => {
          this.returnedData = data;
          this.routerService.goToPage(this.returnUrl);
          this.loading = false;
        },
        error => {
          console.log(error);
          this.message = "Incorrect login information";
          this.loading = false;
          return error;
        });
  }
}
