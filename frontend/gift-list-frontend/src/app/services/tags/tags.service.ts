import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TagsService {

  private searchTagUrl = this.baseUrl+"/tags/search"

  constructor(private http : HttpClient, @Inject('BASE_BACKEND_URL') private baseUrl : string) { }

  searchTags(literal :string) {    
    return this.http.get<string[]>(this.searchTagUrl+"/"+encodeURIComponent(literal));
  }
}
