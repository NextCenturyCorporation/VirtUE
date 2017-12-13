import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filter'
})
export class JsonFilterPipe implements PipeTransform {

  transform(value: any, filterString: string, propName: string) {
    if (value.length === 0 || filterString === '') {
      return value;
      console.log(value);
    } else {
    const resultArray = [];
    for (const item of value) {
      console.log(value);
      if (item[propName].indexOf(filterString) >== -1) {
        resultArray.push();
        console.log('true');
      } else {
        cosnsole.log('false');
      }
      console.log(item[propName] + ': ' + filterString);
      return resultArray;
    }
  }
}
