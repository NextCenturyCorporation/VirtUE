import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BreadcrumbsModule } from 'ng2-breadcrumbs';
import { SplitPaneModule } from 'ng2-split-pane/lib/ng2-split-pane';
import { MatDialogModule } from '@angular/material';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { HomeComponent } from './home/home.component';
import { ConfigComponent } from './config/config.component';
import { DialogComponent } from './config/dialog/dialog.component';
import { ModalComponent } from './config/modal/modal.component';
import { UsersComponent } from './users/users.component';
import { VirtuesComponent } from './virtues/virtues.component';
import { CreateVirtueComponent } from './virtues/create-virtue/create-virtue.component';
import { EditVirtueComponent } from './virtues/edit-virtue/edit-virtue.component';
import { VirtueSettingsComponent } from './virtues/virtue-settings/virtue-settings.component';
import { FooterComponent } from './footer/footer.component';
import { DialogDemoComponent } from './dialog-demo/dialog-demo.component';

const appRoutes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home',
    component: HomeComponent,
    data: {
      breadcrumb: 'Home'
    }
  },
  {
    path: 'config',
    component: ConfigComponent,
    data: {
      breadcrumb: 'Configuration'
    }
  },
  {
    path: 'users',
    component: UsersComponent,
    data: {
      breadcrumb: 'Users'
    }
  },
  {
    path: 'virtues',
    component: VirtuesComponent,
    data: {
      breadcrumb: 'Virtues'
    },
    children: [
      {
        path: 'create-virtue',
        component: CreateVirtueComponent,
        pathMatch: 'full'
      },
      {
        path: 'edit-virtue',
        component: EditVirtueComponent,
        pathMatch: 'full'
      },
      {
        path: 'settings',
        component: VirtueSettingsComponent,
        pathMatch: 'full'
      }
    ]
  }
  // { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    VirtuesComponent,
    ConfigComponent,
    DialogComponent,
    ModalComponent,
    UsersComponent,
    VirtuesComponent,
    CreateVirtueComponent,
    EditVirtueComponent,
    VirtueSettingsComponent
  ],
  imports: [
    BreadcrumbsModule,
    BrowserModule,
    SplitPaneModule,
    RouterModule.forRoot([
      { path: 'home', component: HomeComponent, data: {breadcrumb: 'Home'} },
      { path: 'config', component: ConfigComponent, data: {breadcrumb: 'Configuration'} },
      { path: 'users', component: UsersComponent, data: {breadcrumb: 'Users'} },
      { path: 'virtues', component: VirtuesComponent, data: {breadcrumb: 'Virtues'} },
      { path: 'virtues/create-virtue', component: CreateVirtueComponent, data: {breadcrumb: 'Create Virtue'} },
      { path: 'virtues/edit-virtue', component: EditVirtueComponent, data: {breadcrumb: 'Edit Virtue'} },
      { path: 'virtues/virtue-settings', component: VirtueSettingsComponent }
    ])
  ],
  providers: [],
  bootstrap: [AppComponent]
})

export class AppModule { }
