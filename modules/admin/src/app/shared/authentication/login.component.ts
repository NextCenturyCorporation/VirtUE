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
    loading = false;
    submitted = false;
    returnUrl: string;
		returnedData: any = {headers: ""};

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

        // reset login status
        // this.authenticationService.logout();

        // get return url from route parameters or default to '/'
        this.returnUrl = this.activatedRoute.snapshot.queryParams['returnUrl'] || '/';
    }

		getMethods(obj) {
		  var result = [];
		  for (var id in obj) {
		    try {
		      if (typeof(obj[id]) == "function") {
		        result.push(id + ": " + obj[id].toString());
		      }
		    } catch (err) {
		      result.push(id + ": inaccessible");
		    }
		  }
		  return result;
		}

    onSubmit() {
        this.submitted = true;

        // stop here if form is invalid
        if (this.loginForm.invalid) {
            return;
        }

        this.loading = true;
        this.authenticationService.login(this.loginForm.controls.username.value, this.loginForm.controls.password.value)
					// .pipe(data => {console.log(data); return data;})
          .subscribe(
            (data: HttpResponse<string>) => {
							this.returnedData = data;
							// console.dir(data.headers.get('Set-Cookie'));
							// console.dir(data.headers.get('X-XSRF-TOKEN'));
							// console.dir(data.headers.keys());
							// console.dir(this.getMethods(data.headers));
							// console.log(this.returnUrl);
              // this.routerService.goToPage(this.returnUrl);
              this.routerService.goToPage('/users');
            },
            error => {
              console.log("in error");
              console.log(error);
              this.loading = false;
            });
  }

	logout() {
		this.authenticationService.logout().subscribe(
				something => {
			console.log(something);
			this.routerService.goToPage('/login');
		});

		// console.log(this.returnedData.headers.keys());
	}
}
