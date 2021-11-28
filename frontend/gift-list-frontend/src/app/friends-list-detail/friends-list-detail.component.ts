import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftList } from '../model/giftList';
import { ReserveOrRelaseGiftModel } from '../model/reserveOrReleaseGiftReqModel';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-friends-list-detail',
  templateUrl: './friends-list-detail.component.html',
  styleUrls: ['./friends-list-detail.component.css']
})
export class FriendsListDetailComponent implements OnInit {

  listId=""
  errorMessage=""
  offeringDate : Date =new Date()
  currentList : GiftList =new GiftList()
  giftToReserveOrRelease : Gift = new Gift()
  activePage:number =1
  itemsPerPage =10
  sortingParameter : string ="title"
  sortOrder : number =1
  sortingFunction =this.compareGiftsByTitle.bind(this)

  constructor(private route: ActivatedRoute, private giftListService : GiftListService, private router : Router) { }

  ngOnInit(): void {
    let identifierList = this.route.snapshot.paramMap.get('listId')
    if(identifierList){
      this.listId=identifierList
      this.loadCurrentList()
    }
  }

  loadCurrentList(){
    this.errorMessage=""
    this.giftListService.listDetail(this.listId)
      .subscribe({
        next:(res)=>{
          this.currentList=res
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400){
              this.errorMessage="Problème lors du chargement dle la liste : "+err.error
            }
            else if (err.status===404){
              this.errorMessage="Cette liste n'a pas été trouvée"
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  prepareOffering(aGift : Gift){
    this.errorMessage=""
    this.offeringDate=new Date()
    this.giftToReserveOrRelease=aGift
  }

  reserveGift(){
    this.errorMessage=""
    if(!this.giftToReserveOrRelease || !this.offeringDate){
      this.errorMessage="Aucun cadeau choisi ou date non renseignée"
      return
    }    
    let nowDate = new Date();
    nowDate.setHours(0,0,0,0)
    let realDateObject = new Date(this.offeringDate);
    if(nowDate > realDateObject){
      this.errorMessage="La date renseignée ne doit pas être passée"
      return
    }
    let formattedDate = realDateObject.getDate() + '/' + ((realDateObject.getMonth() + 1)) + '/' + realDateObject.getFullYear()
    //Preparing the request model
    let requestModel : ReserveOrRelaseGiftModel = new ReserveOrRelaseGiftModel()
    requestModel.listId=this.listId
    requestModel.giftId=this.giftToReserveOrRelease.giftId
    requestModel.deliveryDate=formattedDate
    this.giftListService.reserveGift(requestModel)
      .subscribe({
        next:(res)=>{
          this.loadCurrentList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400){
              this.errorMessage="Problème lors de la réservation : "+err.error
            }
            else if (err.status===404){
              this.errorMessage="Ce cadeau n'a pas été trouvé"
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }


  releaseGift(aGift : Gift){
    this.errorMessage=""
    let requestModel : ReserveOrRelaseGiftModel = new ReserveOrRelaseGiftModel()
    requestModel.listId=this.listId
    requestModel.giftId=aGift.giftId
    requestModel.deliveryDate=""
    this.giftListService.releaseGift(requestModel)
      .subscribe({
        next:(res)=>{
          this.loadCurrentList()
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status=== 500 || err.status === 400){
              this.errorMessage="Problème lors de l'annulation : "+err.error
            }
            else if (err.status===404){
              this.errorMessage="Ce cadeau n'a pas été trouvé"
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  //Pagination
  countPages(){
    return new Array(Math.ceil(this.currentList.gifts.length / this.itemsPerPage))
  }
  setActivePage(pageIndex:number, el: HTMLElement){
    this.activePage=pageIndex
    el.scrollIntoView()
  }
  reInitPagination(){    
    this.activePage=1
  }

  compareGiftsByTitle(a :Gift, b: Gift) {
      if ( a.title < b.title ){
        return this.sortOrder*-1;
      }
      if ( a.title > b.title ){
        return this.sortOrder*1;
      }
      return 0;
  }

  compareGiftByRating(a:Gift, b:Gift){
    if ( a.rating < b.rating ){
      return this.sortOrder*-1;
    }
    if ( a.rating > b.rating ){
      return this.sortOrder*1;
    }
    return this.compareGiftsByTitle(a,b);
  }

  compareGiftByDate(a:Gift, b:Gift) {
    let dateInfoA = a.lastUpdate.split('/')
    let dateInfoB = b.lastUpdate.split('/')
    let dateA  : Date= new Date()
    let dateB  : Date= new Date()
    dateA.setDate(+dateInfoA[0])
    dateA.setMonth(+dateInfoA[1])
    dateA.setFullYear(+dateInfoA[2])
    dateB.setDate(+dateInfoB[0])
    dateB.setMonth(+dateInfoB[1])
    dateB.setFullYear(+dateInfoB[2])
    dateA.setHours(0,0,0,0)
    dateB.setHours(0,0,0,0)
    if ( dateA < dateB ){
      return this.sortOrder*-1;
    }
    if ( dateA > dateB ){
      return this.sortOrder*1;
    }
    //Same date : compare by title       
    return this.compareGiftsByTitle(a,b);
  }

  setSortByTitle(){
    if(this.sortingParameter==="title"){
      this.sortOrder *=-1
    }
    else{
      this.sortingParameter="title"
      this.sortOrder =1
      this.sortingFunction=this.compareGiftsByTitle.bind(this)
    }
  }
  setSortByRating(){
    if(this.sortingParameter==="rating"){
      this.sortOrder *=-1
    }
    else{
      this.sortingParameter="rating"
      this.sortOrder =1
      this.sortingFunction=this.compareGiftByRating.bind(this)
    }
  }
  setSortByDate(){
    if(this.sortingParameter==="date"){
      this.sortOrder *=-1
    }
    else{
      this.sortingParameter="date"
      this.sortOrder =1
      this.sortingFunction=this.compareGiftByDate.bind(this)
    }
  }

  paginatedGifts(){
    let allGifts=this.currentList.gifts.sort(this.sortingFunction)
    let currentPageGifts : Gift[]=[]
    let start = (this.activePage-1)*this.itemsPerPage
    let end = ((this.activePage)*this.itemsPerPage)
    if(end > allGifts.length){
      end=allGifts.length
    }

    for(let index =start; index <end; index++){
      currentPageGifts.push(allGifts[index])
    }
    return currentPageGifts
  }
}
