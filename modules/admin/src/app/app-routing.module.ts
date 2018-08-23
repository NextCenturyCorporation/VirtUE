import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Breadcrumb} from './shared/models/breadcrumb.model';

import {DashboardComponent} from './dashboard/dashboard.component';
import {ConfigComponent} from './config/config.component';
import {UserListComponent} from './users/user-list/user-list.component';
import {UserComponent} from './users/user.component';

import {VirtueListComponent} from './virtues/virtue-list/virtue-list.component';
import {VirtueComponent} from './virtues/virtue.component';
import {VirtueSettingsComponent} from './virtues/virtue-settings/virtue-settings.component';

import {VmListComponent} from './vms/vm-list/vm-list.component';
import {VmComponent} from './vms/vm.component';

import {AppsListComponent} from './apps/apps-list/apps-list.component';
import {AddAppComponent} from './apps/add-app/add-app.component';

import {PageNotFoundComponent} from './page-not-found/page-not-found.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
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
      breadcrumb: new Breadcrumb('Settings', '/settings')
    }
  }, {
    path: 'users',
    component: UserListComponent,
    data: {
      breadcrumb: new Breadcrumb('Users', '/users')
    },
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
      }
    ]
  }, {
    path: 'virtues',
    component: VirtueListComponent,
    data: {
      breadcrumb: new Breadcrumb('Virtues', '/virtues')
    }
  }, {
    path: 'virtues',
    data: {
      breadcrumb: new Breadcrumb('Virtues', '/virtues')
    },
    children: [
      {
        path: 'create',
        component: VirtueComponent,
        data: {
          breadcrumb: new Breadcrumb('Create Virtue', '/create')
        }
      }, {
      path: 'edit/:id',
        component: VirtueComponent,
        data: {
          breadcrumb: new Breadcrumb('Edit Virtue', '/edit')
        }
      }, {
        path: 'duplicate/:id',
        component: VirtueComponent,
        data: {
          breadcrumb: new Breadcrumb('Duplicate Virtue', '/duplicate')
        }
      }, {
      path: 'virtue-settings',
        component: VirtueSettingsComponent
      }
    ]
  }, {
    path: 'vm-templates',
    component: VmListComponent,
    data: {
      breadcrumb: new Breadcrumb('VM Templates', '/vm-templates')
    }
  }, {
    path: 'vm-templates',
    data: {
      breadcrumb: new Breadcrumb('VM Templates', '/vm-templates')
    },
    children: [
      {
        path: 'create',
        component: VmComponent,
        data: {
          breadcrumb: new Breadcrumb('Build Virtual Machine', '/create')
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
  },
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  // imports: [RouterModule.forRoot(routes, {
  //   useHash: true
  // })],
  exports: [RouterModule],
  declarations: []
})
export class AppRoutingModule {
}
