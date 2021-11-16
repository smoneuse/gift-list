import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { FriendsListDetailComponent } from './friends-list-detail/friends-list-detail.component';
import { FriendsListsComponent } from './friends-lists/friends-lists.component';
import { GiftOwnerDetailComponent } from './gift-owner-detail/gift-owner-detail.component';
import { ListDetailComponent } from './list-detail/list-detail.component';
import { LoginComponent } from './login/login.component';
import { OwnedListsComponent } from './owned-lists/owned-lists.component';
import { RegisterComponent } from './register/register.component';
import { UserFriendsComponent } from './user-friends/user-friends.component';
import { WelcomeComponent } from './welcome/welcome.component';

const routes: Routes = [
  {
    path:'',
    redirectTo:'/welcome',
    pathMatch:'full'
  },
  {
    path:'welcome',
    component:WelcomeComponent
  },
  {
    path:'ownedLists',
    component: OwnedListsComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'listDetail/:listId',
    component: ListDetailComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'listDetail/:listId/:giftId',
    component: GiftOwnerDetailComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'friendsListsDetail/:listId',
    component:FriendsListDetailComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'friends',
    component:UserFriendsComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'friendsLists',
    component:FriendsListsComponent,
    canActivate:[AuthGuard]
  },
  {
    path:'register',
    component:RegisterComponent
  },
  {
    path:'login',
    component:LoginComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
