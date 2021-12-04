import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'gift-list-frontend';
  constructor(private auth : AuthService, private router : Router){}

  isUserLoggedIn() : boolean {
    return this.auth.loggedIn();
  }

  logout() :void {
    localStorage.removeItem('token')
    this.auth.logout()
    this.router.navigate(['/welcome'])
  }

  getLoggedUser(){
    return this.auth.loggedUser
  }
}
