import { NgModule, SecurityContext } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';

import { BrowserModule } from '@angular/platform-browser';

import { DatePipe } from '@angular/common';
import { HttpClientModule, HttpClientXsrfModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { DomSanitizer } from '@angular/platform-browser';

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
  MatMenuModule,
  MatRadioModule,
  MatSelectModule,
  MatSlideToggleModule,
  MatTabsModule,
  MatToolbarModule,
} from '@angular/material';

import { FlexLayoutModule } from "@angular/flex-layout";
import { Ng2DropdownModule } from 'ng2-material-dropdown';

import { SplitPaneModule } from 'ng2-split-pane/lib/ng2-split-pane';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';

import { DashboardComponent } from './dashboard/dashboard.component';

import { ConfigComponent } from './config/config.component';
import { ConfigActiveDirTabComponent } from './config/config-activeDir-tab/config-activeDir-tab.component';
import { ConfigSensorsTabComponent } from './config/config-sensors-tab/config-sensors-tab.component';
import { ConfigPrinterTabComponent } from './config/config-printer-tab/config-printer-tab.component';
import { ConfigFileSysTabComponent } from './config/config-fileSys-tab/config-fileSys-tab.component';
import { ConfigSettingsHistoryTabComponent } from './config/config-settingsHistory-tab/config-settingsHistory-tab.component';

import { GenericTableComponent } from './shared/abstracts/gen-table/gen-table.component';

import { UserListComponent } from './users/user-list/user-list.component';
import { UserComponent} from './users/user.component';
import { UserMainTabComponent} from './users/form/main-tab/main-user-tab.component';
import { VirtueModalComponent } from './modals/virtue-modal/virtue-modal.component';

import { VirtueListComponent } from './virtues/virtue-list/virtue-list.component';
import { VirtueInstanceListComponent } from './virtues/virtue-instance-list/virtue-instance-list.component';
import { VirtueComponent } from './virtues/virtue.component';
import { VirtueMainTabComponent } from './virtues/form/main-tab/virtue-main-tab.component';
import { VirtueSettingsTabComponent } from './virtues/form/settings-tab/virtue-settings.component';
import { VirtueUsageTabComponent } from './virtues/form/usage-tab/virtue-usage-tab.component';
// import { VirtueHistoryTabComponent } from './virtues/form/history-tab/virtue-history-tab.component';
import { VmModalComponent } from './modals/vm-modal/vm-modal.component';

import { ColorModalComponent } from './modals/color-picker/color-picker.modal';
import { PrinterModalComponent } from './modals/printer-modal/printer.modal';
import { PrinterSelectionModalComponent } from './modals/printer-modal/printer-selection.modal';
// import { FileSystemModalComponent } from './modals/fileSystem-modal/fileSystem.modal';
import { FileSystemSelectionModalComponent } from './modals/fileSystem-modal/fileSystem-selection.modal';

import { VmListComponent } from './vms/vm-list/vm-list.component';
import { VmInstanceListComponent } from './vms/vm-instance-list/vm-instance-list.component';
import { VmMainTabComponent } from './vms/form/vm-main-tab/vm-main-tab.component';
import { VmUsageTabComponent } from './vms/form/vm-usage-tab/vm-usage-tab.component';
import { VmComponent} from './vms/vm.component';
import { AppsModalComponent } from './modals/apps-modal/apps-modal.component';
import { OSSet } from './vms/os.set';

import { AppsListComponent } from './apps/apps-list/apps-list.component';
import { AddAppComponent } from './apps/add-app/add-app.component';

import { PageNotFoundComponent } from './page-not-found/page-not-found.component';

import { ActiveClassDirective } from './shared/directives/active-class.directive';
import { DialogsComponent } from './dialogs/dialogs.component';


import { ListFilterPipe } from './shared/abstracts/gen-table/list-filter.pipe';

import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';

import { MessageService } from './shared/services/message.service';
import { DataRequestService } from './shared/services/dataRequest.service';
import { RouterService } from './shared/services/router.service';
import { SensingService } from './shared/services/sensing.service';
import { BaseUrlService } from './shared/services/baseUrl.service';

import { AuthGuard } from './shared/authentication/auth.guard';
import { LoginGuard } from './shared/authentication/login.guard';
import { AuthenticationInterceptor } from './shared/authentication/authentication.interceptor';
import { ErrorInterceptor } from './shared/authentication/error.interceptor';
import { BaseUrlInterceptor } from './shared/services/baseUrl.interceptor';
import { AuthenticationService } from './shared/services/authentication.service';
import { LoginComponent } from './shared/authentication/login.component';

@NgModule({
  declarations: [
    AppComponent,
    BreadcrumbsComponent,
    ConfigComponent,
    ConfigActiveDirTabComponent,
    ConfigSensorsTabComponent,
    ConfigPrinterTabComponent,
    ConfigFileSysTabComponent,
    ConfigSettingsHistoryTabComponent,
    FooterComponent,
    HeaderComponent,
    DashboardComponent,

    GenericTableComponent,

    UserListComponent,
    UserComponent,
    UserMainTabComponent,

    VirtueListComponent,
    VirtueInstanceListComponent,
    VirtueMainTabComponent,
    VirtueSettingsTabComponent,
    VirtueUsageTabComponent,
    // VirtueHistoryTabComponent,
    VirtueComponent,

    DialogsComponent,
    VirtueModalComponent,
    VmModalComponent,
    ColorModalComponent,
    PrinterModalComponent,
    PrinterSelectionModalComponent,
    // FileSystemModalComponent,
    FileSystemSelectionModalComponent,

    ListFilterPipe,
    PageNotFoundComponent,

    VmListComponent,
    VmInstanceListComponent,
    VmComponent,
    VmMainTabComponent,
    VmUsageTabComponent,

    ActiveClassDirective,

    AppsListComponent,
    AddAppComponent,
    AppsModalComponent,

    LoginComponent
  ],
  imports: [
    AppRoutingModule,

    BrowserAnimationsModule,
    BrowserModule,
    FlexLayoutModule,
    FormsModule,
    HttpClientModule,
    HttpClientXsrfModule
      .withOptions({
                         cookieName: 'XSRF-TOKEN',
                         headerName: 'X-XSRF-TOKEN',
             }),
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatRadioModule,
    MatSlideToggleModule,
    MatSelectModule,
    MatTabsModule,
    MatToolbarModule,
    Ng2DropdownModule,
    ReactiveFormsModule,
    SplitPaneModule
  ],
  exports: [
    OverlayModule
  ],
  providers: [
    DataRequestService,
    BaseUrlService,
    MessageService,
    OverlayContainer,
    RouterService,
    SensingService,
    OSSet,
    DatePipe,

    AuthGuard,
    LoginGuard,
    AuthenticationService,
    HttpClientXsrfModule,
    // { provide: HTTP_INTERCEPTORS, useExisting: HttpClientXsrfModule, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: BaseUrlInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: AuthenticationInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogsComponent,
    AppsModalComponent,
    VmModalComponent,
    VirtueModalComponent,
    ColorModalComponent,
    // PrinterModalComponent,
    PrinterSelectionModalComponent,
    // FileSystemModalComponent,
    FileSystemSelectionModalComponent
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
