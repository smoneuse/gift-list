import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  errorMessage ="";
  loginUserData={ "login":"","password":""}
  constructor(private auth : AuthService, private router : Router) { }

  ngOnInit(): void {
  }

  loginUser() : void {
    this.errorMessage="";
    this.auth.loginUser(this.loginUserData)
      .subscribe({
        next: (res)=> {
          localStorage.setItem('token',btoa(this.loginUserData.login+':'+this.loginUserData.password))
          this.router.navigate(['/ownedLists'])
        },
        error: (err)=> {
          if(err instanceof HttpErrorResponse){
            if(err.status === 401){
              this.errorMessage="Connexion impossible. Login ou mot de passe incorrect.";
            }
          }
        }
      }
      )
  }
}