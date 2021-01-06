import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environments/environment';
import {OrderRequest} from './model/orderRequest';
import {Observable} from 'rxjs';
import {OrderInfo} from './model/orderInfo';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly orderUrl = environment.apiUrl + 'api/order/';

  constructor(private http: HttpClient) { }

  public place(request: OrderRequest): Observable<OrderInfo> {
    return this.http.post<OrderInfo>(this.orderUrl + 'place', request);
  }

  public status(id: string): Observable<OrderInfo> {
    return this.http.get<OrderInfo>(this.orderUrl + 'status/' + id);
  }
}
