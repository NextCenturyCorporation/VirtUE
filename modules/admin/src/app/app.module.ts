import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule, Routes } from '@angular/router';

import { BreadcrumbsModule } from 'ng2-breadcrumbs';
import { SplitPaneModule } from 'ng2-split-pane/lib/ng2-split-pane';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ConfigComponent } from './config/config.component';
import { UsersComponent } from './users/users.component';
import { VirtuesComponent } from './virtues/virtues.component';
import { CreateVirtueComponent } from './virtues/create-virtue/create-virtue.component';
import { EditVirtueComponent } from './virtues/edit-virtue/edit-virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { FooterComponent } from './footer/footer.component';
import { AddUserComponent } from './users/add-user/add-user.component';
import { EditUserComponent } from './users/edit-user/edit-user.component';
import { DialogsComponent } from './dialogs/dialogs.component';
import { VmModalComponent } from './virtues/vm-modal/vm-modal.component';

const appRoutes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: {breadcrumb: 'Home'} },
  { path: 'config', component: ConfigComponent, data: {breadcrumb: 'Configuration'} },
  { path: 'users', component: UsersComponent, data: {breadcrumb: 'Users'} },
  { path: 'users/add-user', component: AddUserComponent, data: {breadcrumb: 'Add User Account'} },
  { path: 'users/edit-user', component: EditUserComponent, data: {breadcrumb: 'Edit User Account'} },
  { path: 'virtues', component: VirtuesComponent, data: {breadcrumb: 'Virtues'} },
  { path: 'virtues/create-virtue', component: CreateVirtueComponent, data: {breadcrumb: 'Create Virtue'} },
  { path: 'virtues/edit-virtue', component: EditVirtueComponent, data: {breadcrumb: 'Edit Virtue'} },
  { path: 'virtues/virtue-settings', component: VirtueSettingsComponent }
  // { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    DashboardComponent,
    VirtuesComponent,
    ConfigComponent,
    UsersComponent,
    AddUserComponent,
    EditUserComponent,
    VirtuesComponent,
    VirtueSettingsComponent,
    CreateVirtueComponent,
    EditVirtueComponent,
    DialogsComponent,
    VmModalComponent
  ],
  imports: [
    BreadcrumbsModule,
    BrowserModule,
    FormsModule,
    MatDialogModule,
    BrowserAnimationsModule,
    SplitPaneModule,
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    RouterModule
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [
    VmModalComponent,
    DialogsComponent
  ]
})

export class AppModule { }
