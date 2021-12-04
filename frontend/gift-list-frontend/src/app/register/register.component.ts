import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  errorMessage=""
  registerUserData={ "login":"","password":""}
  constructor(private auth : AuthService, private router : Router) { }

  ngOnInit(): void {
  }

  registerUser() :void {
    this.errorMessage=""
    if(this.registerUserData.login.trim()==="" || this.registerUserData.password.trim()===""){
      this.errorMessage="Merci de renseigner le login et mot de passe"
      return
    }
    if(this.registerUserData.login.trim().indexOf(' ')!=-1){
      this.errorMessage="Merci de ne pas positionner d'espace dans l'identifiant"
      return
    }
    this.registerUserData.login=this.registerUserData.login.trim()
    this.registerUserData.password=this.registerUserData.password.trim()
    this.auth.registerUser(this.registerUserData)
      .subscribe({
        next: (res)=> {
          if(res.status==='SUCCESS'){
            localStorage.setItem('token',btoa(res.account+':'+this.registerUserData.password))
            this.auth.loggedUser=res.account
            this.router.navigate(['/ownedLists'])
          }else if(res.status==='INVALID_DATA'){
            this.errorMessage="Enregistrement impossible. Veuillez renseigner les champs login et mot de passe."
          }
          else if(res.status==='ALREADY_PRESENT'){
            this.errorMessage="Enregistrement impossible. L'utilisateur "+res.account+" est déjà enregistré"
          }else{
            this.errorMessage="Enregistrement impossible : "+res.detail
          }
        },
        error: (err)=>{
          if(err instanceof HttpErrorResponse){
            this.errorMessage="Erreur durant l'enregistrement. Code statut : "+err.status;
          }
        }
      }
      )
  }
}
