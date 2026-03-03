import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './pages/register/register.component';
import { HomeComponent } from './pages/authenticaded/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { AuthGuard } from './guards/auth.guard';
import { LayoutComponent } from './layout/layout.component';
import { SettingsComponent } from './pages/authenticaded/settings/settings.component';
import { HistoryComponent } from './pages/authenticaded/history/history.component';
import { NotificationsComponent } from './pages/authenticaded/notifications/notifications.component';
import { ProfileComponent } from './pages/authenticaded/profile/profile.component';
import { TrainComponent } from './pages/authenticaded/train/train.component';
import { CompletedWorkoutComponent } from './pages/authenticaded/completed-workout/completed-workout.component';
import { PlannedWorkoutCreateComponent } from './pages/authenticaded/planned-workout-create/planned-workout-create.component';
import { CatalogManagementComponent } from './pages/authenticaded/catalog-management/catalog-management.component';


export const routes: Routes = [

  // Rotas públicas
  { path: '', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },

  // Rotas protegidas
  {
    path: 'notifications', canActivate: [AuthGuard], component: NotificationsComponent,
  },
  {
    path: 'profile', canActivate: [AuthGuard], component: ProfileComponent,
  },
  {
    path: '',
    canActivate: [AuthGuard],
    component: LayoutComponent,
    children: [
      {
        path: 'home', component: HomeComponent
      },
      {
        path: 'train', component: TrainComponent
      },
      {
        path: 'planned-workout/new', component: PlannedWorkoutCreateComponent
      },
      {
        path: 'planned-workout/:id/edit', component: PlannedWorkoutCreateComponent
      },
      {
        path: 'completed-workout/:id', component: CompletedWorkoutComponent
      },
      {
        path: 'history', component: HistoryComponent
      },
      {
        path: 'settings', component: SettingsComponent
      },
      {
        path: 'settings/exercises',
        component: CatalogManagementComponent,
        data: { mode: 'exercises' }
      },
      {
        path: 'settings/muscles',
        component: CatalogManagementComponent,
        data: { mode: 'muscles' }
      },
      {
        path: 'settings/equipments',
        component: CatalogManagementComponent,
        data: { mode: 'equipments' }
      }
    ]
  },

  // Qualquer rota inválida → redirect
  { path: '**', redirectTo: 'home' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
