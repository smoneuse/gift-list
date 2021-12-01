import { HttpErrorResponse } from '@angular/common/http';
import { elementEventFullName } from '@angular/compiler/src/view_compiler/view_compiler';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Gift } from '../model/gift';
import { GiftList } from '../model/giftList';
import { GiftListService } from '../services/gift-list.service';

@Component({
  selector: 'app-paginate-gifts',
  templateUrl: './paginate-gifts.component.html',
  styleUrls: ['./paginate-gifts.component.css']
})
export class PaginateGiftsComponent implements OnInit {

  @Input('gList') gList : GiftList=new GiftList()
  @Input() errorMessage =""
  @Output('errorMessageChange') errorMessageChange = new EventEmitter<string>();
  @Output('listChange') listChange = new EventEmitter<GiftList>();

  giftToDelete : Gift = new Gift()
  activePage:number =1
  itemsPerPage =10
  sortingParameter="title"
  sortingFunction=this.compareGiftsByTitle.bind(this)
  sortOrder=1
  tagFilter :Set<string> = new Set()

  constructor(private giftListService : GiftListService, private router : Router) { 
  }

  ngOnInit(): void {
  }

  availableGifts() : Gift[]{
    let resultingGifts :Gift[]=[]
    if(this.gList.gifts.length===0){
      return resultingGifts
    }
    for(let aGift of this.gList.gifts){
      if(aGift.status ==="AVAILABLE"){
        resultingGifts.push(aGift)
      }
    }
    resultingGifts= this.filterByTag(resultingGifts)
    return resultingGifts.sort(this.sortingFunction)
  }

  filterByTag( fullList :Gift[] ) : Gift[]{
    if(this.tagFilter.size===0){
      return fullList
    }
    let filteredList :Gift[] =[]
    for(let aGift of fullList){
      for(let aTag of aGift.tags){
        if(this.tagFilter.has(aTag)){
          filteredList.push(aGift)
          break;
        }
      }
    }
    return filteredList
  }

  reInitPagination(){    
    this.activePage=1
  }

  paginatedGifts(){
    let availableGifts=this.availableGifts()
    let currentPageGifts : Gift[]=[]
    let start = (this.activePage-1)*this.itemsPerPage
    let end = ((this.activePage)*this.itemsPerPage)
    if(end > availableGifts.length){
      end=availableGifts.length
    }

    for(let index =start; index <end; index++){
      currentPageGifts.push(availableGifts[index])
    }
    return currentPageGifts
  }

  countPages(){
    return new Array(Math.ceil(this.availableGifts().length / this.itemsPerPage))
  }

  allGiftsTags() : Set<string> {
    let result = new Set<string>()
    for(let aGift of this.gList.gifts){
      for(let aTag of aGift.tags){
        result.add(aTag)
      }
    }
    return result
  }

  updateTagFilter(aTag : string){
    if(this.tagFilter.has(aTag)){
      this.tagFilter.delete(aTag)
    }
    else{
      this.tagFilter.add(aTag)
    }
    this.reInitPagination()
  }

  setActivePage(pageIndex:number, el: HTMLElement){
    this.activePage=pageIndex
    el.scrollIntoView()
  }
  compareGiftsByTitle( a : Gift, b :Gift) {
    if ( a.title < b.title ){
      return this.sortOrder*-1;
    }
    if ( a.title > b.title ){
      return this.sortOrder*1;
    }
    return 0;
  }

  compareGiftsByRating( a : Gift, b :Gift) {
    if ( a.rating < b.rating ){
      return this.sortOrder*-1;
    }
    if ( a.rating > b.rating ){
      return this.sortOrder*1;
    }
    //Same rating : compare by title
    return this.compareGiftsByTitle(a,b);
  }

  compareGiftsByDate( a : Gift, b :Gift) {   
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

  testMe( a : Gift, b :Gift){
    console.log(a)
    console.log(b)
  }

  setSortByTitle(){
    if(this.sortingParameter==="title"){
      this.sortOrder *= -1

    }
    else{
      this.sortingParameter="title"
      this.sortingFunction=this.compareGiftsByTitle.bind(this)
    }
  }

  setSortByRating(){
    if(this.sortingParameter==="rating"){
      this.sortOrder *= -1
    }
    else{
      this.sortOrder=1
      this.sortingParameter="rating"
      this.sortingFunction=this.compareGiftsByRating.bind(this)
    }
  }

  setSortByDate(){
    if(this.sortingParameter==="date"){
      this.sortOrder *= -1
    }
    else{
      this.sortOrder=1
      this.sortingParameter="date"
      this.sortingFunction=this.compareGiftsByDate.bind(this)
    }
  }

  loadList(listId : string){
    this.giftToDelete=new Gift()
    this.giftListService.listDetail(listId)
    .subscribe({
      next:(res) =>{
        this.gList=res
        if(this.gList.gifts.length <=  ((this.activePage-1)*this.itemsPerPage) && this.activePage>1){
          this.activePage=this.activePage-1
        }
        this.listChange.emit(this.gList)
      },
      error: (err)=>{
        if(err instanceof HttpErrorResponse){
          if(err.status===404){
            this.errorMessageChange.emit("La liste n'a pas été trouvée")
          }
          else if(err.status===500 || err.status===400){
            this.errorMessageChange.emit("Problème lors de la récupération de la liste :"+err.error)            
          }
          else if(err.status === 401 || err.status === 403){
            this.router.navigate(['/login'])
          }
        }
      }
    })
  }


  deleteGiftPaginate(){
    this.errorMessageChange.emit("");    
    this.giftListService.deleteGift(this.giftToDelete.listId, this.giftToDelete.giftId)
      .subscribe({
        next:(res)=>{
          console.log(`Gift ${this.giftToDelete.giftId} deleted`)
          this.loadList(this.giftToDelete.listId)          
        },
        error:(err)=>{
          if(err instanceof HttpErrorResponse){
            if(err.status === 500 || err.status === 400){
              this.errorMessageChange.emit("Une erreur est survenue lors de la suppression : "+err.error)
            }
            else if(err.status === 401 || err.status === 403){
              this.router.navigate(['/login'])
            }
          }
        }
      })
  }

  prepareDeleteGifPaginate(gift : Gift){
    this.giftToDelete=gift
  }
}
