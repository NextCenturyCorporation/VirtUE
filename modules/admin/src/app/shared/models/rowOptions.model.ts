import { Item } from './item.model';

//A list of these is passed into the GenericTable class' constructor. Used to
//specify what links appear under the "Name" column
//Each option will have the text that is actually show, a function that returns
//true iff that options should show up at that certain point, and a function to
// be called when the text is clicked.
export class RowOptions {
  constructor(
    public text: string,
    public shouldAppear: (item: Item) => boolean,
    public action: (item?: Item) => void
  ) {}
}
