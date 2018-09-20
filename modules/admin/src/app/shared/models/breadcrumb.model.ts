export class Breadcrumb {

  /**
   * just initializes those two fields as attributes
   */
  constructor(
    /** The text string which should show up on the screen as a clickable link */
    public label: string,
    /** the url to be navigated to, if the user clicks the breadcrumb */
    public href: string) {
  }
}
