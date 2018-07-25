import { NgModule } from '@angular/core';
import {AppRoutingModule } from './app-routing.module';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { HttpClientModule } from '@angular/common/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { OverlayContainer } from '@angular/cdk/overlay';
import { OverlayModule } from '@angular/cdk/overlay';

import {
  MatAutocompleteModule,
  MatCheckboxModule,
  MatDialogModule,
  MatFormFieldModule,
  MatInputModule,
  MatRadioModule,
  MatSelectModule,
  MatToolbarModule,
} from '@angular/material';

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

import { UsersWrapperComponent } from './users/users.wrapper.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent} from './users/user/user.component';
import { VirtueModalComponent } from './users/virtue-modal/virtue-modal.component';

import { VirtuesWrapperComponent } from './virtues/virtues.wrapper.component';
import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { VirtueComponent} from './virtues/virtue/virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { VmModalComponent } from './virtues/vm-modal/vm-modal.component';

import { VmAppsModalComponent } from './virtual-machines/vm-apps-modal/vm-apps-modal.component';

// import { VirtualMachinesComponent } from './virtual-machines/virtual-machines.component';
import {VmsWrapperComponent} from './virtual-machines/vms.wrapper.component';
import { VmListComponent } from './virtual-machines/vm-list/vm-list.component';
import { VmComponent} from './virtual-machines/vm/vm.component';
// import { VmBuildComponent } from './virtual-machines/vm-build/vm-build.component';
// import { VmEditComponent } from './virtual-machines/vm-edit/vm-edit.component';
// import { VmDuplicateComponent } from './virtual-machines/vm-duplicate/vm-duplicate.component';

import { VmAppsComponent } from './vm-apps/vm-apps.component';
import { VmAppsListComponent } from './vm-apps/vm-apps-list/vm-apps-list.component';
import { AddVmAppComponent } from './vm-apps/add-vm-app/add-vm-app.component';

import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { ActiveClassDirective } from './shared/directives/active-class.directive';
import { DialogsComponent } from './dialogs/dialogs.component';

import {BreadcrumbsComponent} from './breadcrumbs/breadcrumbs.component';

import { ListFilterPipe } from './shared/pipes/list-filter.pipe';
import { JsonFilterPipe } from './shared/pipes/json-filter.pipe';
import { CountFilterPipe } from './shared/pipes/count-filter.pipe';

import { BreadcrumbProvider } from './shared/providers/breadcrumb';

import { BaseUrlService } from './shared/services/baseUrl.service';
import { MessageService } from './shared/services/message.service';
import { VirtuesService } from './shared/services/virtues.service';

@NgModule({
  declarations: [
    AppComponent,
    BreadcrumbsComponent,
    ConfigActiveDirComponent,
    ConfigAppVmComponent,
    ConfigComponent,
    ConfigResourcesComponent,
    FooterComponent,
    HeaderComponent,
    DashboardComponent,

    UsersWrapperComponent,
    UserListComponent,
    UserComponent,

    VirtuesWrapperComponent,
    VirtueListComponent,
    VirtueSettingsComponent,
    VirtueComponent,

    DialogsComponent,
    VirtueModalComponent,
    VmModalComponent,
    ResourceModalComponent,
    FileShareComponent,
    PrintersComponent,
    ConfigSensorsComponent,
    ListFilterPipe,
    JsonFilterPipe,
    CountFilterPipe,
    PageNotFoundComponent,

    // VirtualMachinesComponent,
    VmsWrapperComponent,
    VmListComponent,
    VmComponent,
    // VmBuildComponent,
    // VmEditComponent,
    // VmDuplicateComponent,

    ActiveClassDirective,

    VmAppsComponent,
    VmAppsListComponent,
    AddVmAppComponent,
    VmAppsModalComponent,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FormsModule,
    HttpClientModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule,
    MatToolbarModule,
    ReactiveFormsModule,
    SplitPaneModule
  ],
  exports: [
    OverlayModule
  ],
  providers: [
    BreadcrumbProvider,
    OverlayContainer,
    BaseUrlService,
    MessageService,
    VirtuesService
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogsComponent,
    ResourceModalComponent,
    VmAppsModalComponent,
    VmModalComponent,
    VirtueModalComponent
  ]
})
export class AppModule { }
