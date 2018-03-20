import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { DashboardComponent } from './dashboard/dashboard.component';
import { ConfigComponent } from './config/config.component';
import { UsersComponent } from './users/users.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { AddUserComponent } from './users/add-user/add-user.component';
import { EditUserComponent } from './users/edit-user/edit-user.component';
import { VirtuesComponent } from './virtues/virtues.component';
import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { CreateVirtueComponent } from './virtues/create-virtue/create-virtue.component';
import { EditVirtueComponent } from './virtues/edit-virtue/edit-virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { VirtualMachinesComponent } from './virtual-machines/virtual-machines.component';
import { VmListComponent } from './virtual-machines/vm-list/vm-list.component';
import { VmBuildComponent } from './virtual-machines/vm-build/vm-build.component';
import { VmEditComponent } from './virtual-machines/vm-edit/vm-edit.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';
import { VmAppsComponent } from './vm-apps/vm-apps.component';
import { VmAppsListComponent } from './vm-apps/vm-apps-list/vm-apps-list.component';
import { AddVmAppComponent } from './vm-apps/add-vm-app/add-vm-app.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: {breadcrumb: 'Dashboard'} },
  { path: 'config', component: ConfigComponent, data: {breadcrumb: 'Settings'} },
  { path: 'users', component: UsersComponent, data: {breadcrumb: 'Users'}, children: [
    { path: '', component:  UserListComponent },
    { path: 'add', component: AddUserComponent, data: {breadcrumb: 'Add User Account'} },
    { path: 'edit/:id', component: EditUserComponent, data: {breadcrumb: 'Edit User Account'} }
  ] },
  { path: 'vm-apps', component: VmAppsComponent, data: {breadcrumb: 'Applications'}, children: [
    { path: '', component: VmAppsListComponent },
    { path: 'add-app', component: AddVmAppComponent, data: {breadcrumb: 'Install New App'} },
  ] },
  { path: 'virtues', component: VirtuesComponent, data: {breadcrumb: 'Virtues'}, children: [
    { path: '', component: VirtueListComponent },
    { path: 'create-virtue', component: CreateVirtueComponent, data: {breadcrumb: 'Create Virtue'} },
    { path: 'edit/:id', component: EditVirtueComponent, data: {breadcrumb: 'Edit Virtue'} },
    { path: 'virtue-settings', component: VirtueSettingsComponent }
  ] },
  { path: 'vm', component: VirtualMachinesComponent, data: {breadcrumb: 'Virtual Machines'}, children: [
    { path: '', component: VmListComponent },
    { path: 'vm-build', component: VmBuildComponent, data: {breadcrumb: 'Build Virtual Machine'} },
    { path: 'edit/:id', component: VmEditComponent, data: {breadcrumb: 'Edit Virtual Machine'} },
  ] },
  { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ],
  declarations: []
})
export class AppRoutingModule { }
