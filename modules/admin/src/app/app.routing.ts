import { Routes } from '@angular/router';

import { DashboardComponent} from './dashboard.component'
import { ConfigComponent} from './config.component'
import { UsersComponent} from './users.component'
import { VirtuesComponent} from './virtues.component'

export const appRoutes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: {breadcrumb: 'Home'}, pathMatch: 'full' },
  { path: 'config', component: ConfigComponent, data: {breadcrumb: 'Configuration'}, pathMatch: 'full' },
  { path: 'users', component: UsersComponent, data: {breadcrumb: 'Users'}, children: [
    { path: '', redirectTo: 'users', pathMatch: 'full' },
    { path: 'add-user', component: AddUserComponent, data: {breadcrumb: 'Add User Account'} },
    { path: 'edit-user/:id', component: EditUserComponent, data: {breadcrumb: 'Edit User Account'} }
  ] },
  { path: 'virtues', component: VirtuesComponent, data: {breadcrumb: 'Virtues'}, children: [
    { path: 'create-virtue', component: CreateVirtueComponent, data: {breadcrumb: 'Create Virtue'} },
    { path: 'edit-virtue', component: EditVirtueComponent, data: {breadcrumb: 'Edit Virtue'} }
  ], pathMatch: 'full' },
  { path: 'virtues/virtue-settings', component: VirtueSettingsComponent, pathMatch: 'full' }
  //{ path: '**', component: PageNotFoundComponent }
];
