import { Component, OnInit } from '@angular/core';
import {OrderRequest} from '../model/orderRequest';
import {ProductEntry} from '../model/productEntry';
import {OrderService} from '../order.service';
import {OrderInfo} from '../model/orderInfo';
import {SaleRequest} from '../model/saleRequest';
import {DeliveryRequest} from '../model/deliveryRequest';
import {PaymentRequest} from '../model/paymentRequest';

class PaymentReq implements PaymentRequest {
  public cardNumber = '';
}

class Product implements ProductEntry {
  public productName = '';
  public price = 0;
}

class SaleReq implements SaleRequest {
  public products: Array<Product> = [];
}

class OrderReq implements OrderRequest {
  public saleRequest: SaleRequest = new SaleReq();
  public deliveryRequest: DeliveryRequest = {
    recipient: {
      firstName: '',
      lastName: ''
    },
    address: {
      address: '',
      zip: '',
      city: '',
      country: ''
    }
  };
  public paymentRequest: PaymentRequest = new PaymentReq();
}

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.scss']
})
export class ShopComponent implements OnInit {

  constructor(private service: OrderService) {
    this.reset();
  }

  public newProductName = '';
  public newProductPrice = 0;

  public request: OrderReq = new OrderReq();

  public orderInfo: OrderInfo | undefined = undefined;

  private static emptyRequest(): OrderReq {
    return new OrderReq();
  }

  ngOnInit(): void {
  }

  public submit(): void {
    this.service.place(this.request).subscribe(
      result => {
        this.orderInfo = result;
        console.log('Order info received');
      },
      error => console.error(error),
      () => this.reset(),
    );
  }

  public reset(): void {
    this.request = ShopComponent.emptyRequest();
  }

  public addNewProduct(): void {
    const newProduct: ProductEntry = {productName: this.newProductName, price: this.newProductPrice};
    this.request.saleRequest?.products?.push(newProduct);
    this.newProductName = '';
    this.newProductPrice = 0;
  }

  public refresh(): void {
    if (!this.orderInfo?.id) {
      console.log('order id unavailable');
      return;
    }
    this.service.status(this.orderInfo?.id).subscribe(
      result => this.orderInfo = result,
      error => console.error(error),
    );
  }

}
