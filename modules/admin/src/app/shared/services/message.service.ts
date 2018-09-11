import { Injectable } from '@angular/core';

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class MessageService {
  messages: string[] = [];

  add(message: string) {
    this.messages.push(message);
  }

  clear() {
    this.messages = [];
  }
}
