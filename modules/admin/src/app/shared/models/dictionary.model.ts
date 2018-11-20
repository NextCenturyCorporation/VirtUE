
/**
 * @class
 * This class represents a merging of a dictionary and a list.
 *
 * It is generic, and useful for containing collections of objects which need to be quickly queried, but also able to be sorted.
 *
 * Note that record replacement is not supported, though it could be.
 *
 * @example // usage:
 *      let dataset = new DictList<Item>();
 *      dataset.add( "username1", new User(...) );
 *
 *      let virtueDataset = new DictList<Virtue>();
 *      let v = new Virtue(...);
 *      virtueDataset.add(v.getID(), v);
 *
 *
 * When written, this was a useful tool. Due to other changes, the times when the dictionary part is actually used have become minimal,
 * and so performance would likely not be impacted much if all this were replaced with regular list operations.
 *
 * If I got rid of it though, I'd have to either write a bunch of search loops, or just replace this class with a list that has a search.
 */
 export class DictList<T> {

  private dict: Dict<T> = {};

  private list: T[] = [];

  keys(): string[] {
    let keys: string[] = [];
    for (let key of Object.keys(this.dict)) {
      keys.push(key);
    }
    return keys;
  }

  /**
   * Note that record replacement is not supported - once a key has been linked to
   * a value, the reference to that value can't change.
   */
  add(key: string, e: T): void {
    if (key in this.dict) {
      console.log("Key ", key + ": ", e, " already in dict.");
      throw new Error("Key " + key + ": " + JSON.stringify(e) + " already in dict.");
      // return;
    }
    this.dict[key] = e;
    this.list.push(e);
  }

  /**
   * @return true iff that key is in dict
   */
  has(key: string): boolean {
    if (key in this.dict) {
      return true;
    }
    return false;
  }

  /**
   * @return the element saved via that key, if one exists.
   */
  get(key: string): T {
    return this.dict[key];
  }

  /**
   * @return the collection as a list. Does no processing.
   */
  asList(): T[] {
    return this.list;
  }

  remove(key: string): void {
    if (!(key in this.dict)) {
      return;
    }
    let index: number = this.list.indexOf(this.dict[key], 0);
    if (index > -1) {
       this.list.splice(index, 1);
    }
    delete this.dict[key];
  }

  /**
   * Clears references to the list and dictionary in this object.
   * Somewhat paranoic attempt to preclude memory leaks.
   * This guarantees there'll be no references to the dict, but the list could still exist somewhere
   * after being passed out via [[asList]].
   */
  clear(): void {
    this.dict = null;
    this.list = null;
  }


  getSubset(keys: string[]): DictList<T> {
    let subset = new DictList<T>();
    for (let key of keys) {
      if (this.has(key)) {
        subset.add(key, this.get(key));
      }
    }
    return subset;
  }

  trimTo(keysToKeep: string[]): void {
    for (let key of this.keys()) {
      if ( keysToKeep.indexOf(key) === -1 ) {
        this.remove(key);
      }
    }
  }
}

/**
 * @class
 * A simple, generic dictionary
 * The key must be a string.
 *
 * @example
 *      // usage:
 *      let dict = new Dict<FooObject>();
 *      dict["key1"] = new FooObject();
 */
export class Dict<T> {
    [key: string]: T;
}
