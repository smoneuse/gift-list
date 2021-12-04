import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoggedUser } from './model/LoggedUser';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  loggedUser:string="";
  private registerUrl=this.baseUrl+"/account/register"
  private loginUrl=this.baseUrl+"/account/login"
  private logoutUrl=this.baseUrl+"/account/logout"
  constructor(private http : HttpClient, @Inject('BASE_BACKEND_URL') private baseUrl : string) { }

  registerUser(user : any) {    
    return this.http.post<LoggedUser>(this.registerUrl, user)
  }

  loginUser(user : any){
    return this.http.post<LoggedUser>(this.loginUrl, user)
  }

  logout() {
    this.loggedUser="";
    return this.http.post<any>(this.logoutUrl,{})
  }

  loggedIn(){    
    return !!localStorage.getItem('token')
  }
  
}
