import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Breadcrumb } from './shared/models/breadcrumb.model';

import { DashboardComponent } from './dashboard/dashboard.component';
import { ConfigComponent } from './config/config.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent } from './users/user.component';

import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { VirtueInstanceListComponent } from './virtues/virtue-instance-list/virtue-instance-list.component';
import { VirtueComponent } from './virtues/virtue.component';

import { VmListComponent } from './vms/vm-list/vm-list.component';
import { VmInstanceListComponent } from './vms/vm-instance-list/vm-instance-list.component';
import { VmComponent } from './vms/vm.component';

import { AppsListComponent } from './apps/apps-list/apps-list.component';
import { AddAppComponent } from './apps/add-app/add-app.component';

import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { BasicObjectDetailsComponent } from './basic-object-details/basic-object-details.component';

import { AuthGuard } from './shared/authentication/auth.guard';
import { AuthenticationInterceptor } from './shared/authentication/authentication.interceptor';
import { ErrorInterceptor } from './shared/authentication/error.interceptor';
import { AuthenticationService } from './shared/services/authentication.service';
import { LoginComponent } from './shared/authentication/login.component';
import { LoginGuard } from './shared/authentication/login.guard';

/**
 * This defines all the navigable URLs, what components should be loaded at each, and what data should be passed in to that
 * component.
 * The paths build up in the nested objects.
 *
 */
const routes: Routes = [
  {
    path: '',
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      }, {
        path: 'dashboard',
        component: DashboardComponent
      }, {
        path: 'settings',
        component: ConfigComponent
      }, {
        path: 'users',
        component: UserListComponent
      }, {
        path: 'users',
        children: [
          {
            path: 'create',
            component: UserComponent
          }, {
            path: 'view/:id',
            component: UserComponent
          }, {
            path: 'edit/:id',
            component: UserComponent
          }, {
            path: 'duplicate/:id',
            component: UserComponent
          }
        ]
      }, {
        path: 'applications',
        component: AppsListComponent
      }, {
          path: 'applications',
          children: [
            {
              path: 'create',
              component: AddAppComponent
          // }, {
          //   path: 'view/:id',
          //   component: AppComponent
          }
        ]
      }, {
        path: 'virtue-instances',
        component: VirtueInstanceListComponent
      }, {
        path: 'virtues',
        component: VirtueListComponent
      }, {
        path: 'virtues',
        children: [
          {
            path: 'create',
            component: VirtueComponent
          }, {
            path: 'view/:id',
            component: VirtueComponent
          }, {
          path: 'edit/:id',
            component: VirtueComponent
          }, {
            path: 'duplicate/:id',
            component: VirtueComponent
          }
        ]
      }, {
        path: 'vm-instances',
        component: VmInstanceListComponent
      }, {
        path: 'vm-templates',
        component: VmListComponent
      }, {
        path: 'vm-templates',
        children: [
          {
            path: 'create',
            component: VmComponent
          }, {
            path: 'view/:id',
            component: VmComponent
          }, {
            path: 'edit/:id',
            component: VmComponent
          }, {
            path: 'duplicate/:id',
            component: VmComponent
          },
        ]
      }, {
        path: 'view/:dataset/:id',
        component: BasicObjectDetailsComponent
      }
    ]
  }, {
    path: 'login',
    component: LoginComponent,
    canActivate: [LoginGuard]
  }, {
    path: '**', component: PageNotFoundComponent
  }
];

/**
 * @class
 * This class handles the routing to the main pages of the application.
 */
@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule],
  declarations: []
})
export class AppRoutingModule {
}
