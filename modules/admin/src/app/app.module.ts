import { NgModule } from '@angular/core';
import {AppRoutingModule } from './app-routing.module';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

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
import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { CreateVirtueComponent } from './virtues/create-virtue/create-virtue.component';
import { EditVirtueComponent } from './virtues/edit-virtue/edit-virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { VmModalComponent } from './virtues/vm-modal/vm-modal.component';

import { VirtualMachinesComponent } from './virtual-machines/virtual-machines.component';
import { VmListComponent } from './virtual-machines/vm-list/vm-list.component';
import { VmBuildComponent } from './virtual-machines/vm-build/vm-build.component';
import { VmEditComponent } from './virtual-machines/vm-edit/vm-edit.component';

import { DialogsComponent } from './dialogs/dialogs.component';
import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { JsonFilterPipe } from './shared/json-filter.pipe';
import { CountFilterPipe } from './shared/count-filter.pipe';

import { VirtuesService } from './shared/services/virtues.service';
import { JsondataService } from './shared/services/jsondata.service';


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
    VirtueListComponent,
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
    VirtualMachinesComponent,
    VmListComponent,
    VmBuildComponent,
    VmEditComponent,
  ],
  imports: [
    AppRoutingModule,
    BreadcrumbsModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    HttpModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatFormFieldModule,
    MatCheckboxModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule,
    MatToolbarModule,
    BrowserAnimationsModule,
    SplitPaneModule
  ],
  exports: [
    JsonFilterPipe,
    OverlayModule
  ],
  providers: [
    { provide: OverlayContainer, useFactory: () => new AppOverlayContainer() },
    JsondataService,
    VirtuesService
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
