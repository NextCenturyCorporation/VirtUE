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
 * Note that nested objects are children of a near-copy of the parent page but without a defined component - if the
 *  parent and the child have a component defined, it seems like both components get loaded/rendered. Or at least the parent's
 *  component does, because that's all you see.
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
        component: DashboardComponent,
        data: {
          breadcrumb: new Breadcrumb('Dashboard', '/dashboard')
        }
      }, {
        path: 'settings',
        component: ConfigComponent,
        data: {
          breadcrumb: new Breadcrumb('Global Settings', '/settings')
        }
      }, {
        path: 'users',
        component: UserListComponent,
        data: {
          breadcrumb: new Breadcrumb('Users', '/users')
        }
      }, {
        path: 'users',
        data: {
          breadcrumb: new Breadcrumb('Users', '/users')
        },
        children: [
          {
          path: 'create',
          component: UserComponent,
          data: {
            breadcrumb: new Breadcrumb('Add User Account', '/create')
          }
          }, {
            path: 'view/:id',
            component: UserComponent,
            data: {
              breadcrumb: new Breadcrumb('View User Account', '/view')
            }
          }, {
            path: 'edit/:id',
            component: UserComponent,
            data: {
              breadcrumb: new Breadcrumb('Edit User Account', '/edit')
            }
          }, {
            path: 'duplicate/:id',
            component: UserComponent,
            data: {
              breadcrumb: new Breadcrumb('Duplicate User Account',  '/duplicate')
            }
          }
        ]
      }, {
        path: 'applications',
        component: AppsListComponent,
        data: {
          breadcrumb: new Breadcrumb('Applications', '/apps')
        }
      }, {
          path: 'applications',
          data: {
            breadcrumb: new Breadcrumb('Applications', '/apps')
          },
        children: [
          {
            path: 'create',
            component: AddAppComponent,
            data: {
              breadcrumb: new Breadcrumb('Install New App', '/create')
            }
          // }, {
          //   path: 'view/:id',
          //   component: AppComponent,
          //   data: {
          //     breadcrumb: new Breadcrumb('View Application',  '/view')
          //   }
          }
        ]
      }, {
        path: 'virtue-instances',
        component: VirtueInstanceListComponent,
        data: {
          breadcrumb: new Breadcrumb('Virtue Instances', '/virtueInstances')
        }
      }, {
        path: 'virtues',
        component: VirtueListComponent,
        data: {
          breadcrumb: new Breadcrumb('Virtue Templates', '/virtues')
        }
      }, {
        path: 'virtues',
        data: {
          breadcrumb: new Breadcrumb('Virtue Templates', '/virtues')
        },
        children: [
          {
            path: 'create',
            component: VirtueComponent,
            data: {
              breadcrumb: new Breadcrumb('Create Virtue Template', '/create')
            }
          }, {
            path: 'view/:id',
            component: VirtueComponent,
            data: {
              breadcrumb: new Breadcrumb('View Virtue', '/view')
            }
          }, {
          path: 'edit/:id',
            component: VirtueComponent,
            data: {
              breadcrumb: new Breadcrumb('Edit Virtue Template', '/edit')
            }
          }, {
            path: 'duplicate/:id',
            component: VirtueComponent,
            data: {
              breadcrumb: new Breadcrumb('Duplicate Virtue Template', '/duplicate')
            }
          }
        ]
      }, {
        path: 'vm-instances',
        component: VmInstanceListComponent,
        data: {
          breadcrumb: new Breadcrumb('Virtual Machine Templates', '/vm-templates')
        }
      }, {
        path: 'vm-templates',
        component: VmListComponent,
        data: {
          breadcrumb: new Breadcrumb('Virtual Machine Templates', '/vm-templates')
        }
      }, {
        path: 'vm-templates',
        data: {
          breadcrumb: new Breadcrumb('Virtual Machine Templates', '/vm-templates')
        },
        children: [
          {
            path: 'create',
            component: VmComponent,
            data: {
              breadcrumb: new Breadcrumb('Build Virtual Machine', '/create')
            }
          }, {
            path: 'view/:id',
            component: VmComponent,
            data: {
              breadcrumb: new Breadcrumb('View Virtual Machine', '/view')
            }
          }, {
            path: 'edit/:id',
            component: VmComponent,
            data: {
              breadcrumb: new Breadcrumb('Edit Virtual Machine', '/edit')
            }
          }, {
            path: 'duplicate/:id',
            component: VmComponent,
            data: {
              breadcrumb: new Breadcrumb('Duplicate Virtual Machine', '/duplicate')
            }
          },
        ]
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
