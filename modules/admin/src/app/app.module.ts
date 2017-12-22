import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Routes, RouterModule } from '@angular/router';

import { OverlayContainer } from '@angular/cdk/overlay';
import { AppOverlayContainer } from './appOverlayContainer';
import { OverlayModule } from '@angular/cdk/overlay';

import {
  MatAutocompleteModule,
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatCheckboxModule,
  MatChipsModule,
  MatDatepickerModule,
  MatDialogModule,
  MatExpansionModule,
  MatFormFieldModule,
  MatGridListModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatNativeDateModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatRippleModule,
  MatSelectModule,
  MatSidenavModule,
  MatSliderModule,
  MatSlideToggleModule,
  MatSnackBarModule,
  MatStepperModule,
  MatTableModule,
  MatTabsModule,
  MatToolbarModule,
  MatTooltipModule,
} from '@angular/material';

import { BreadcrumbsModule } from 'ng2-breadcrumbs';
import { SplitPaneModule } from 'ng2-split-pane/lib/ng2-split-pane';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

import { DashboardComponent } from './dashboard/dashboard.component';

import { ConfigComponent } from './config/config.component';
import { ConfigActiveDirComponent } from './config/config-active-dir/config-active-dir.component';
import { ConfigAppVmComponent } from './config/config-app-vm/config-app-vm.component';
import { ConfigResourcesComponent } from './config/config-resources/config-resources.component';
import { ResourceModalComponent } from './config/resource-modal/resource-modal.component';
import { FileShareComponent } from './config/resource-modal/file-share/file-share.component';
import { PrintersComponent } from './config/resource-modal/printers/printers.component';
import { ConfigSensorsComponent } from './config/config-sensors/config-sensors.component';

import { UsersComponent } from './users/users.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { AddUserComponent } from './users/add-user/add-user.component';
import { EditUserComponent } from './users/edit-user/edit-user.component';
import { VirtueModalComponent } from './users/virtue-modal/virtue-modal.component';

import { VirtuesComponent } from './virtues/virtues.component';
import { VirtueComponent } from './virtues/virtue/virtue.component';
import { CreateVirtueComponent } from './virtues/create-virtue/create-virtue.component';
import { EditVirtueComponent } from './virtues/edit-virtue/edit-virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { VmModalComponent } from './virtues/vm-modal/vm-modal.component';

import { DialogsComponent } from './dialogs/dialogs.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { JsonFilterPipe } from './data/json-filter.pipe';
import { CountFilterPipe } from './data/count-filter.pipe';

const appRoutes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: {breadcrumb: 'Dashboard'} },
  { path: 'config', component: ConfigComponent, data: {breadcrumb: 'Configuration'} },
  { path: 'users', component: UsersComponent, data: {breadcrumb: 'Users'}, children: [
    { path: '', component:  UserListComponent },
    { path: 'add', component: AddUserComponent, data: {breadcrumb: 'Add User Account'} },
    { path: 'edit/:id', component: EditUserComponent, data: {breadcrumb: 'Edit User Account'} }
  ] },
  { path: 'virtues', component: VirtuesComponent, data: {breadcrumb: 'Virtues'}, children: [
    { path: '', component: VirtueComponent },
    { path: 'create-virtue', component: CreateVirtueComponent, data: {breadcrumb: 'Create Virtue'} },
    { path: 'edit/:id', component: EditVirtueComponent, data: {breadcrumb: 'Edit Virtue'} },
    { path: 'virtue-settings', component: VirtueSettingsComponent }
  ] },
  { path: '**', component: PageNotFoundComponent }
];


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    DashboardComponent,
    VirtuesComponent,
    ConfigComponent,
    ConfigActiveDirComponent,
    ConfigAppVmComponent,
    ConfigResourcesComponent,
    UsersComponent,
    UserListComponent,
    AddUserComponent,
    EditUserComponent,
    VirtuesComponent,
    VirtueComponent,
    VirtueSettingsComponent,
    CreateVirtueComponent,
    EditVirtueComponent,
    DialogsComponent,
    VirtueModalComponent,
    VmModalComponent,
    ResourceModalComponent,
    FileShareComponent,
    PrintersComponent,
    ConfigSensorsComponent,
    JsonFilterPipe,
    CountFilterPipe,
    PageNotFoundComponent,
  ],
  imports: [
    BreadcrumbsModule,
    BrowserModule,
    FormsModule,
    HttpModule,
    MatDialogModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule,
    MatToolbarModule,
    BrowserAnimationsModule,
    SplitPaneModule,
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    JsonFilterPipe,
    OverlayModule,
    RouterModule
  ],
  providers: [
    { provide: OverlayContainer, useFactory: () => new AppOverlayContainer(),  },
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    VmModalComponent,
    DialogsComponent,
    VirtueModalComponent,
    ResourceModalComponent
  ]
})
export class AppModule { }
