import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RegisterComponent } from './register/register.component';
import { LoginComponent } from './login/login.component';
import { OwnedListsComponent } from './owned-lists/owned-lists.component';
import { FriendsListsComponent } from './friends-lists/friends-lists.component';
import { WelcomeComponent } from './welcome/welcome.component';
import { AuthService } from './auth.service';
import { AuthGuard } from './auth.guard';
import { GiftListService } from './services/gift-list.service';
import { AuthInterceptorService } from './services/authInterceptor/auth-interceptor.service';
import { ListDetailComponent } from './list-detail/list-detail.component';
import { GiftOwnerDetailComponent } from './gift-owner-detail/gift-owner-detail.component';
import { StarRatingComponent } from './star-rating/star-rating.component';
import { FriendsListDetailComponent } from './friends-list-detail/friends-list-detail.component';
import { UserFriendsComponent } from './user-friends/user-friends.component';
import { PaginateGiftsComponent } from './paginate-gifts/paginate-gifts.component';

@NgModule({
  declarations: [
    AppComponent,
    RegisterComponent,
    LoginComponent,
    OwnedListsComponent,
    FriendsListsComponent,
    WelcomeComponent,
    ListDetailComponent,
    GiftOwnerDetailComponent,
    StarRatingComponent,
    FriendsListDetailComponent,
    UserFriendsComponent,
    PaginateGiftsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [
    { provide: 'BASE_BACKEND_URL', useValue:'http://gift.freeboxos.fr:33333/api' },
    //{ provide: 'BASE_BACKEND_URL', useValue:'http://localhost:8080/api' },
    {
      provide:HTTP_INTERCEPTORS,
      useClass:AuthInterceptorService,
      multi:true
    },
     AuthService, GiftListService, AuthGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
