import { NgModule, SecurityContext } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';

import { BrowserModule } from '@angular/platform-browser';

import { DatePipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import {DomSanitizer} from '@angular/platform-browser';

import { OverlayContainer } from '@angular/cdk/overlay';
import { OverlayModule } from '@angular/cdk/overlay';

import {
  MatAutocompleteModule,
  MatButtonModule,
  MatCardModule,
  MatCheckboxModule,
  MatDialogModule,
  MatFormFieldModule,
  MatGridListModule,
  MatIconModule,
  MatIconRegistry,
  MatInputModule,
  MatRadioModule,
  MatSelectModule,
  MatSlideToggleModule,
  MatTabsModule,
  MatToolbarModule,
} from '@angular/material';

import { FlexLayoutModule } from "@angular/flex-layout";


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

import { GenericTableComponent } from './shared/abstracts/gen-table/gen-table.component';

import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent} from './users/user.component';
import { UserMainTabComponent} from './users/form/main-tab/main-user-tab.component';
import { VirtueModalComponent } from './modals/virtue-modal/virtue-modal.component';

import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { VirtueComponent } from './virtues/virtue.component';
import { VirtueMainTabComponent } from './virtues/form/main-tab/virtue-main-tab.component';
import { VirtueSettingsTabComponent } from './virtues/form/settings-tab/virtue-settings.component';
import { VirtueUsageTabComponent } from './virtues/form/usage-tab/virtue-usage-tab.component';
// import { VirtueHistoryTabComponent } from './virtues/form/history-tab/virtue-history-tab.component';
import { ColorModalComponent } from './modals/color-picker/color-picker.modal';
import { VmModalComponent } from './modals/vm-modal/vm-modal.component';

import { VmListComponent } from './vms/vm-list/vm-list.component';
import { VmMainTabComponent } from './vms/form/vm-main-tab/vm-main-tab.component';
import { VmUsageTabComponent } from './vms/form/vm-usage-tab/vm-usage-tab.component';
import { VmComponent} from './vms/vm.component';
import { AppsModalComponent } from './modals/apps-modal/apps-modal.component';

import { AppsListComponent } from './apps/apps-list/apps-list.component';
import { AddAppComponent } from './apps/add-app/add-app.component';

import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { ActiveClassDirective } from './shared/directives/active-class.directive';
import { DialogsComponent } from './dialogs/dialogs.component';


import { ListFilterPipe } from './shared/pipes/list-filter.pipe';

import { BreadcrumbProvider } from './shared/providers/breadcrumb';
import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';

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

    GenericTableComponent,

    UserListComponent,
    UserComponent,
    UserMainTabComponent,

    VirtueListComponent,
    VirtueMainTabComponent,
    VirtueSettingsTabComponent,
    VirtueUsageTabComponent,
    // VirtueHistoryTabComponent,
    VirtueComponent,

    DialogsComponent,
    VirtueModalComponent,
    VmModalComponent,
    ColorModalComponent,
    ResourceModalComponent,
    FileShareComponent,
    PrintersComponent,
    ConfigSensorsComponent,
    ListFilterPipe,
    PageNotFoundComponent,

    VmListComponent,
    VmComponent,
    VmMainTabComponent,
    VmUsageTabComponent,

    ActiveClassDirective,

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
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatSelectModule,
    MatTabsModule,
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
    ColorModalComponent
  ]
})

/**
 * This is the main entry point for this angular application.
 * If you want to import an outside class into any file, or if you want angular to load any class automatically,
 * it must be imported here as well, and added to one of the above lists.
 * Any component defined within this project, to be displayed on/as a page, must be imported and added to the 'declarations' list.
 * Any class (generally a service) which you want to use as a Provider, must be added to the 'providers' list.
 * Any class from outside this project you wish to use somewhere, must be imported and added to the 'imports' list.
 *
 * @class AppModule
 */
export class AppModule {

  /**
   * This is only needed to allow the use of Angular Material Icons in this app. All icons are defined in the below svg file.
   * This (bypassing the sanitizer) is apparently the recommended way to load (an) icon(s).
   */
  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {
    matIconRegistry.addSvgIconSet(domSanitizer.bypassSecurityTrustResourceUrl('/assets/mdi.svg'));
  }
}
