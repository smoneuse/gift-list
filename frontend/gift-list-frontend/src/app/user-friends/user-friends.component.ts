import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-user-friends',
  templateUrl: './user-friends.component.html',
  styleUrls: ['./user-friends.component.css']
})
export class UserFriendsComponent implements OnInit {

  userFriendList :string[]=[]
  errorMessage=""
  friendToAdd=""

  constructor(private giftListService : GiftListService, private router : Router) { }

  ngOnInit(): void {
    this.loadUserFriends()
  }

  loadUserFriends(){
    this.errorMessage=""
    this.userFriendList =[]
    this.giftListService.getFriends()
      .subscribe({
        next:(res)=>{
          this.userFriendList=res
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400 || err.status===404){
              this.errorMessage="Problème lors de la récupération des amis : "+err.error
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  addAFriend(){
    if(this.friendToAdd===""){
      this.errorMessage="Merci de renseigner le login de l'ami à ajouter"
      return
    }    
    this.errorMessage=""
    this.giftListService.addFriend(this.friendToAdd)
      .subscribe({
        next:(res)=>{
          this.friendToAdd=""
          this.loadUserFriends()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400 || err.status===404){
              this.errorMessage="Problème lors de l'ajout d'un ami : "+err.error
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  removeFriend(aFriend : string){
    this.errorMessage=""
    this.giftListService.removeFriend(aFriend)
      .subscribe({
        next:(res)=>{
          this.loadUserFriends()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400 || err.status===404){
              this.errorMessage="Problème lors de la suppression d'un ami : "+err.error
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }
}
