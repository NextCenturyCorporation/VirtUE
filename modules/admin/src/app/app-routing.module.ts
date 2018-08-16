import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {Breadcrumb} from './shared/models/breadcrumb.model';

import {DashboardComponent} from './dashboard/dashboard.component';
import {ConfigComponent} from './config/config.component';
import {UsersWrapperComponent} from './users/users.wrapper.component';
import {UserListComponent} from './users/user-list/user-list.component';
import {UserComponent} from './users/user/user.component';

import {VirtuesWrapperComponent} from './virtues/virtues.wrapper.component';
import {VirtueListComponent} from './virtues/virtue-list/virtue-list.component';
import {VirtueComponent} from './virtues/virtue/virtue.component';
import {VirtueSettingsComponent} from './virtues/virtue-settings/virtue-settings.component';

import {VmsWrapperComponent} from './vms/vms.wrapper.component';
import {VmListComponent} from './vms/vm-list/vm-list.component';
import {VmComponent} from './vms/vm/vm.component';

import {AppsComponent} from './apps/apps.component';
import {AppsListComponent} from './apps/apps-list/apps-list.component';
import {AddAppComponent} from './apps/add-app/add-app.component';

import {PageNotFoundComponent} from './page-not-found/page-not-found.component';

const routes: Routes = [
  {
  path: 'dashboard',
    component: DashboardComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('Dashboard', '/dashboard')
      ]
  }
  }, {
  path: 'settings',
    component: ConfigComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('Settings', '/settings')
      ]
    }
  }, {
    path: 'users',
    component: UsersWrapperComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('Users', '/users')
      ]
    },
    children: [
      {
        path: '',
        component: UserListComponent
      }, {
      path: 'create',
        component: UserComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Add User Account', '/create')
          ]
        }
      }, {
        path: 'edit/:id',
        component: UserComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Edit User Account', '/edit')
          ]
        }
      }, {
        path: 'duplicate/:id',
        component: UserComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Duplicate User Account',  '/duplicate')
          ]
        }
      }
    ]
  }, {
    path: 'applications',
    component: AppsComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('Applications', '/apps')
      ]
    },
    children: [
      {
        path: '',
        component: AppsListComponent
      }, {
        path: 'create',
        component: AddAppComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Install New App', '/create')
          ]
        }
      }
    ]
  }, {
    path: 'virtues',
    component: VirtuesWrapperComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('Virtues', '/virtues')
      ]
    },
    children: [
      {
        path: '',
        component: VirtueListComponent
      }, {
        path: 'create',
        component: VirtueComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Create Virtue', '/create')
          ]
        }
      }, {
      path: 'edit/:id',
        component: VirtueComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Edit Virtue', '/edit')
          ]
        }
      }, {
        path: 'duplicate/:id',
        component: VirtueComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Duplicate Virtue', '/duplicate')
          ]
        }
      }, {
      path: 'virtue-settings',
        component: VirtueSettingsComponent
      }
    ]
  }, {
    path: 'vm-templates',
    component: VmsWrapperComponent,
    data: {
      breadcrumbs: [
        new Breadcrumb('VM Templates', '/vm-templates')
      ]
    },
    children: [
      {
        path: '',
        component: VmListComponent
      }, {
        path: 'create',
        component: VmComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Build Virtual Machine', '/create')
          ]
        }
      }, {
        path: 'edit/:id',
        component: VmComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Edit Virtual Machine', '/edit')
          ]
        }
      }, {
        path: 'duplicate/:id',
        component: VmComponent,
        data: {
          breadcrumbs: [
            new Breadcrumb('Duplicate Virtual Machine', '/duplicate')
          ]
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
