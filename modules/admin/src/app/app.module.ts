import { NgModule } from '@angular/core';
import {AppRoutingModule } from './app-routing.module';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';

import { BrowserModule } from '@angular/platform-browser';

import { DatePipe } from '@angular/common';
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

import {MatSlideToggleModule} from '@angular/material/slide-toggle';

import { FlexLayoutModule } from "@angular/flex-layout";

import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';

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

import { GenericTable } from './shared/abstracts/gen-table/gen-table.component';
// import { GenericFormComponent } from './shared/abstracts/gen-form/gen-form.component';
// import { GenericPageComponent } from './shared/abstracts/gen-page/gen-page.component';
// import { GenericModal } from './modals/generic-modal/generic.modal';

import { UsersWrapperComponent } from './users/users.wrapper.component';
import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent} from './users/user/user.component';
import { VirtueModalComponent } from './modals/virtue-modal/virtue-modal.component';

import { VirtuesWrapperComponent } from './virtues/virtues.wrapper.component';
import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { VirtueComponent } from './virtues/virtue/virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { ColorModal } from './modals/color-picker/color-picker.modal';
import { VmModalComponent } from './modals/vm-modal/vm-modal.component';

import { VmsWrapperComponent } from './vms/vms.wrapper.component';
import { VmListComponent } from './vms/vm-list/vm-list.component';
import { VmComponent} from './vms/vm/vm.component';
import { AppsModalComponent } from './modals/apps-modal/apps-modal.component';

import { AppsComponent } from './apps/apps.component';
import { AppsListComponent } from './apps/apps-list/apps-list.component';
import { AddAppComponent } from './apps/add-app/add-app.component';

import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { ActiveClassDirective } from './shared/directives/active-class.directive';
import { DialogsComponent } from './dialogs/dialogs.component';


import { ListFilterPipe } from './shared/pipes/list-filter.pipe';
import { JsonFilterPipe } from './shared/pipes/json-filter.pipe';
import { CountFilterPipe } from './shared/pipes/count-filter.pipe';

import { BreadcrumbProvider } from './shared/providers/breadcrumb';
import {BreadcrumbsComponent} from './breadcrumbs/breadcrumbs.component';

import { BaseUrlService } from './shared/services/baseUrl.service';
import { MessageService } from './shared/services/message.service';

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

    GenericTable,
    // GenericModal,

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
    ColorModal,
    ResourceModalComponent,
    FileShareComponent,
    PrintersComponent,
    ConfigSensorsComponent,
    ListFilterPipe,
    JsonFilterPipe,
    CountFilterPipe,
    PageNotFoundComponent,

    VmsWrapperComponent,
    VmListComponent,
    VmComponent,

    ActiveClassDirective,

    AppsComponent,
    AppsListComponent,
    AddAppComponent,
    AppsModalComponent,
  ],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    FlexLayoutModule,
    FormsModule,
    HttpClientModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatFormFieldModule,
    MatCardModule,
    MatCheckboxModule,
    MatGridListModule,
    MatInputModule,
    MatRadioModule,
    MatSlideToggleModule,
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
    DatePipe
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogsComponent,
    ResourceModalComponent,
    AppsModalComponent,
    VmModalComponent,
    VirtueModalComponent,
    ColorModal
  ]
})
export class AppModule { }
