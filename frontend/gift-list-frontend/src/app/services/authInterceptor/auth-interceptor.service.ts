import { Injectable } from '@angular/core';
import { HttpInterceptor } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthInterceptorService implements HttpInterceptor{

  constructor() { }

  intercept(req :any, next :any){
    let tokenizedRequest = req.clone({
      setHeaders:{
        Authorization : 'Basic '+localStorage.getItem('token')
      }
    })
    return next.handle(tokenizedRequest)
  }
}
