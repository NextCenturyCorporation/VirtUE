
/**
 * #uncommented
 * @class
 * @extends
 */
 export class DictList<T> {
  private dict: Dict<T> = {};
  private list: T[] = [];

  add(key: string, e: T) {
    if (key in this.dict) {
      console.log("Key ", key + ": ", e, " already in dict.");
      return;
    }
    this.dict[key] = e;
    this.list.push(e);
  }

  has(key: string): boolean {
    if (key in this.dict) {
      return true;
    }
    return false;
  }

  get(key: string): T {
    return this.dict[key];
  }

  asList(): T[] {
    return this.list;
  }

  remove(key: string) {
    if (!(key in this.dict)) {
      return;
    }
    let index: number = this.list.indexOf(this.dict[key], 0);
    if (index > -1) {
       this.list.splice(index, 1);
    }
    delete this.dict[key];
  }

  clear() {
    this.dict = null;
    this.list = null;
  }
}

/**
 * #uncommented
 * @class
 */
export class Dict<T> {
    [key: string]: T;
}
