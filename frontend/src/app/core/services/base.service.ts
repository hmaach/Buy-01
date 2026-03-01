import { HttpClient } from "@angular/common/http";
import { inject } from "@angular/core";

export class BaseService {
    public http = inject(HttpClient);
}