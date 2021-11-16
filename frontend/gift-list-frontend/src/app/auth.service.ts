import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private registerUrl=this.baseUrl+"/account/register"
  private loginUrl=this.baseUrl+"/account/login"
  private logoutUrl=this.baseUrl+"/account/logout"
  constructor(private http : HttpClient, @Inject('BASE_BACKEND_URL') private baseUrl : string) { }

  registerUser(user : any) {    
    return this.http.post<any>(this.registerUrl, user)
  }

  loginUser(user : any){
    return this.http.post<any>(this.loginUrl, user)
  }

  logout() {
    return this.http.post<any>(this.logoutUrl,{})
  }

  loggedIn(){    
    return !!localStorage.getItem('token')
  }
}
