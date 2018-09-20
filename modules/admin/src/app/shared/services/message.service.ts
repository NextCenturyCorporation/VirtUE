import { Injectable } from '@angular/core';

/**
 * @class
 * This class allows for the collection of system messages.
 *
 * TODO
 * Currently errors in most item.service files are logged here, but there's no way or place to access them.
 * Not clear if this should be used/kept.
 */
@Injectable()
export class MessageService {

  /** a list of system messages */
  messages: string[] = [];

  /**
   * Record a message in the universal message list
   * @param message the string to be logged
   */
  add(message: string): void {
    this.messages.push(message);
  }

  /**
   * empties the list of messages
   */
  clear(): void {
    this.messages = [];
  }
}
