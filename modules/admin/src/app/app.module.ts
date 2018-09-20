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

import { BreadcrumbsComponent } from './breadcrumbs/breadcrumbs.component';

import { BaseUrlService } from './shared/services/baseUrl.service';
import { MessageService } from './shared/services/message.service';

@NgModule({
  declarations: [
    AppComponent,
    BreadcrumbsComponent,
    ConfigActiveDirComponent,
    ConfigComponent,
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
    OverlayContainer,
    BaseUrlService,
    MessageService,
    DatePipe
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogsComponent,
    AppsModalComponent,
    VmModalComponent,
    VirtueModalComponent,
    ColorModalComponent
  ]
})

export class AppModule {
  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer) {
    matIconRegistry.addSvgIconSet(domSanitizer.bypassSecurityTrustResourceUrl('/assets/mdi.svg'));
  }
}
